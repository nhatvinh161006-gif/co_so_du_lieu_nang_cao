# Algorithm REST API

The Spring Boot application exposes JSON endpoints under `/api/algorithms`.
All requests use `Content-Type: application/json`. Attribute names are
case-sensitive strings, and functional dependencies use `left` and `right`
arrays. Empty `left` is allowed for dependencies such as `∅ -> A`; `right`
must contain at least one attribute.

## Shared request

```json
{
  "universe": ["MaSV", "TenSV", "MaLHP", "Diem"],
  "dependencies": [
    {"left": ["MaSV"], "right": ["TenSV"]},
    {"left": ["MaSV", "MaLHP"], "right": ["Diem"]}
  ]
}
```

Every attribute in a dependency must occur in `universe`. Duplicate or
malformed names are rejected. `atomicValues` is optional and defaults to
`true`; set it to `false` when the values are not atomic to report that the
relation does not satisfy 1NF.

## Danh Sách Endpoints

| Method | Path | Mục đích | Yêu cầu đầu vào |
|---|---|---|---|
| POST | `/api/algorithms/closure` | Tính bao đóng thuộc tính $X^+$ | Cần `universe`, `dependencies`, `x` |
| POST | `/api/algorithms/implication` | Kiểm tra suy diễn $F \models X \to Y$ | Cần `universe`, `dependencies`, `x`, `y` |
| POST | `/api/algorithms/keys` | Tìm tất cả khóa ứng viên | Cần `universe`, `dependencies` |
| POST | `/api/algorithms/minimal-cover` | Tìm phủ tối thiểu của $F$ | Cần `universe`, `dependencies` |
| POST | `/api/algorithms/check-nf` | Xác định dạng chuẩn cao nhất (1NF-BCNF) | Cần `universe`, `dependencies`, tuỳ chọn `atomicValues` |
| POST | `/api/algorithms/decompose-3nf` | Phân rã 3NF tổng hợp bảo toàn PTH và lossless | Cần `universe`, `dependencies` |
| POST | `/api/algorithms/decompose-bcnf` | Phân rã BCNF (đảm bảo nối không mất mát) | Cần `universe`, `dependencies` |
| POST | `/api/algorithms/equivalence` | Kiểm tra tương đương 2 tập PTH ($F_1 \equiv F_2$) | Cần `dependencies` ($F_1$), `dependencies2` ($F_2$), tuỳ chọn `universe` |
| POST | `/api/algorithms/superkeys` | Tìm & đếm tất cả siêu khóa bằng công thức PIE | Cần `universe`, `dependencies`, tuỳ chọn `limit` |
| POST | `/api/algorithms/chase` | Kiểm tra nối không mất mát thông tin (Chase) | Cần `universe`, `dependencies`, tuỳ chọn `relations` |
| POST | `/api/algorithms/check-preservation` | Kiểm tra bảo toàn phụ thuộc qua phân rã | Cần `universe`, `dependencies`, tuỳ chọn `relations` |
| POST | `/api/algorithms/full-analysis` | Phân tích toàn diện 1-click (khóa, phủ, chuẩn, 3NF, BCNF) | Cần `universe`, `dependencies` |
| POST | `/api/algorithms/compare-3nf-bcnf` | So sánh chi tiết thuật toán chuẩn hóa 3NF vs BCNF | Cần `universe`, `dependencies` |
| POST | `/api/algorithms/armstrong` | Minh họa áp dụng tiên đề Armstrong (phản xạ, tăng trưởng, bắc cầu...) | Cần `universe`, `dependencies`, `axiomType`, tuỳ chọn `x`, `y`, `z`, `w` |
| POST | `/api/algorithms/benchmark` | Đo lường hiệu năng thực thi thuật toán theo số thuộc tính | Không bắt buộc tham số |
| POST | `/api/algorithms/generate-ddl` | Sinh mã SQL DDL (PostgreSQL, Oracle) từ lược đồ phân rã | Cần `universe`, `dependencies`, tuỳ chọn `relations` |

---

## Chi Tiết Các Endpoint Mới (Mức Độ Cao)

### 1. Phân Rã BCNF (`/api/algorithms/decompose-bcnf`)
Tách đệ quy các lược đồ vi phạm điều kiện BCNF (vế trái của phụ thuộc không tầm thường không phải siêu khóa).

**Response shape:**
```json
{
  "relations": [
    {
      "name": "R1",
      "attributes": ["MaSV", "TenSV", "NgaySinh", "MaNganh"],
      "candidateKeys": [["MaSV"]]
    }
  ],
  "violations": [
    {
      "relation": ["A", "B", "C"],
      "left": ["B"],
      "right": ["C"],
      "r1": ["B", "C"],
      "r2": ["A", "B"],
      "leftClosure": ["B", "C"]
    }
  ],
  "steps": [...]
}
```

### 2. Kiểm Tra Tương Đương Hai Tập PTH (`/api/algorithms/equivalence`)
Chứng minh $F_1 \models F_2$ và $F_2 \models F_1$.

**Request:**
```json
{
  "dependencies": [
    {"left": ["A"], "right": ["B"]},
    {"left": ["B"], "right": ["C"]}
  ],
  "dependencies2": [
    {"left": ["A"], "right": ["B"]},
    {"left": ["B"], "right": ["C"]},
    {"left": ["A"], "right": ["C"]}
  ]
}
```

**Response shape:**
```json
{
  "equivalent": true,
  "f1ImpliesF2": true,
  "f2ImpliesF1": true,
  "f1ImpliesF2Details": [...],
  "f2ImpliesF1Details": [...],
  "steps": [...]
}
```

### 3. Tìm Tất Cả Siêu Khóa (`/api/algorithms/superkeys`)
Tính chính xác tổng số siêu khóa bằng **Nguyên lý Bù trừ (Principle of Inclusion-Exclusion)** và phân bố số lượng theo từng kích thước.

**Request:**
```json
{
  "universe": ["A", "B", "C"],
  "dependencies": [
    {"left": ["A"], "right": ["B"]},
    {"left": ["B"], "right": ["A"]}
  ],
  "limit": 50
}
```

**Response shape:**
```json
{
  "candidateKeys": [["A", "C"], ["B", "C"]],
  "totalCount": "3",
  "countBySize": {
    "2": "2",
    "3": "1"
  },
  "sampleSuperkeys": [
    ["A", "C"],
    ["B", "C"],
    ["A", "B", "C"]
  ],
  "allListed": true,
  "steps": [...]
}
```

### 4. Thuật Toán Chase — Kiểm Tra Lossless Join (`/api/algorithms/chase`)
Kiểm tra nối không mất mát thông tin bằng phương pháp ma trận bảng Chase ($k$ hàng $\times n$ cột).
Nếu không cung cấp `relations`, hệ thống tự động kiểm tra trên kết quả phân rã 3NF.

**Request:**
```json
{
  "universe": ["A", "B", "C"],
  "dependencies": [{"left": ["A"], "right": ["B"]}],
  "relations": [
    ["A", "B"],
    ["A", "C"]
  ]
}
```

**Response shape:**
```json
{
  "lossless": true,
  "satisfyingRow": 1,
  "attributes": ["A", "B", "C"],
  "initialTableau": [...],
  "finalTableau": [...],
  "steps": [...]
}
```

### 5. Kiểm Tra Bảo Toàn Phụ Thuộc (`/api/algorithms/check-preservation`)
Kiểm tra từng phụ thuộc hàm trong $F$ có được bảo toàn qua thuật toán lan truyền đa thức bao đóng $Z$.

**Request:**
```json
{
  "universe": ["A", "B", "C"],
  "dependencies": [
    {"left": ["A"], "right": ["B"]},
    {"left": ["B"], "right": ["C"]}
  ],
  "relations": [
    ["A", "B"],
    ["B", "C"]
  ]
}
```

**Response shape:**
```json
{
  "preserved": true,
  "details": [
    {
      "dependency": {"left": ["A"], "right": ["B"]},
      "preserved": true,
      "propagatedClosure": ["A", "B"],
      "note": "Được bảo toàn trực tiếp: Toàn bộ X ∪ Y nằm trọn trong lược đồ con R1."
    }
  ],
  "lostDependencies": [],
  "steps": [...]
}
```

```

---

## Chi Tiết Các Endpoint Bổ Sung (Mức Độ Trung Bình & Thấp)

### 6. Phân Tích Tổng Hợp 1-Click (`/api/algorithms/full-analysis`)
Thực hiện toàn bộ quy trình phân tích: Khóa ứng viên, Phủ tối thiểu, Dạng chuẩn cao nhất, Phân rã 3NF (lossless + dependency preserving), Phân rã BCNF (lossless).

**Response shape:**
```json
{
  "keys": { "candidateKeys": [...], ... },
  "minimalCover": { "minimalCover": [...], ... },
  "normalForm": { "normalForm": "3NF", ... },
  "decomposition3NF": { "relations": [...], ... },
  "decompositionBCNF": { "relations": [...], ... },
  "steps": [...]
}
```

### 7. So Sánh 3NF vs BCNF (`/api/algorithms/compare-3nf-bcnf`)
Đối chiếu bảng so sánh ưu nhược điểm, số lược đồ con, tính chất bảo toàn phụ thuộc và nối không mất mát thông tin giữa phân rã 3NF và BCNF trên cùng lược đồ.

### 8. Minh Họa Tiên Đề Armstrong (`/api/algorithms/armstrong`)
Minh họa trực quan các luật dẫn xuất Armstrong: Reflexivity, Augmentation, Transitivity, Union, Decomposition, Pseudo-transitivity.

**Request:**
```json
{
  "universe": ["A", "B", "C"],
  "axiomType": "transitivity",
  "x": ["A"],
  "y": ["B"],
  "z": ["C"]
}
```

### 9. Đo Hiệu Năng Thuật Toán (`/api/algorithms/benchmark`)
Chạy benchmark đo thời gian thực thi tính bao đóng, khóa và chuẩn hóa với các kích thước $N = 5, 8, 12, 16, 20$ thuộc tính.

### 10. Sinh Mã SQL DDL (`/api/algorithms/generate-ddl`)
Sinh tự động câu lệnh `CREATE TABLE`, khóa chính (`PRIMARY KEY`), khóa duy nhất (`UNIQUE`) và suy luận kiểu dữ liệu theo chuẩn PostgreSQL và Oracle.

**Request:**
```json
{
  "universe": ["MaSV", "TenSV", "MaKhoa", "TenKhoa"],
  "dependencies": [
    {"left": ["MaSV"], "right": ["TenSV", "MaKhoa"]},
    {"left": ["MaKhoa"], "right": ["TenKhoa"]}
  ],
  "relations": [
    ["MaSV", "TenSV", "MaKhoa"],
    ["MaKhoa", "TenKhoa"]
  ]
}
```

---

## Xử Lý Lỗi

Đầu vào không hợp lệ trả về HTTP 400 Bad Request:
```json
{
  "message": "[B] không thuộc universe.",
  "field": "dependencies[0].right"
}
```
