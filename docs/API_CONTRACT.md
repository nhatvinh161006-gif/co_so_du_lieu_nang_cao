# TÀI LIỆU ĐẶC TẢ REST API CHO FRONTEND (API CONTRACT)
**Đề tài 10: Phân tích phụ thuộc hàm và chuẩn hóa quan hệ**  
**Base URL:** `http://localhost:8080/api/algorithms`  
**CORS Policy:** Đã bật `@CrossOrigin(origins = "*")` (cho phép Frontend gọi từ mọi port).

---

## 1. Cấu trúc dữ liệu chung (Common Models)

### Phụ thuộc hàm (Dependency Object):
Hỗ trợ cả 2 định dạng khóa:
```json
{
  "left": ["MaSV"],
  "right": ["TenSV", "NgaySinh"]
}
```
*(Hoặc dùng `"vt"`, `"vp"`)*

### Bản ghi bước giải (StepLog Object):
Mỗi bước giải trong danh sách `steps` có định dạng:
```json
{
  "stepNumber": 1,
  "title": "Tên bước giải (ví dụ: Phân loại thuộc tính)",
  "description": "Mô tả chi tiết giải thích cho người học",
  "snapshot": "Trạng thái dữ liệu thu được sau bước này"
}
```

---

## 2. Chi tiết các Endpoint

### 2.1. Tìm tất cả các khóa tối thiểu
- **URL:** `POST /api/algorithms/keys`
- **Mục đích:** Tìm tất cả các khóa tối thiểu, phân nhóm L/R/B/N và ghi vết từng bước giải.
- **Request Body:**
```json
{
  "universe": ["A", "B", "C", "D", "E"],
  "dependencies": [
    { "left": ["A", "B"], "right": ["C"] },
    { "left": ["C"], "right": ["D"] },
    { "left": ["D"], "right": ["E"] }
  ]
}
```
- **Response (200 OK):**
```json
{
  "universe": ["A", "B", "C", "D", "E"],
  "keys": [
    ["A", "B"]
  ],
  "primeAttributes": ["A", "B"],
  "nonPrimeAttributes": ["C", "D", "E"],
  "attributeGroups": {
    "L": ["A", "B"],
    "R": ["E"],
    "B": ["C", "D"],
    "N": []
  },
  "steps": [
    {
      "stepNumber": 1,
      "title": "Phân loại thuộc tính",
      "description": "Phân chia tập U thành 4 nhóm: L (chỉ vế trái), R (chỉ vế phải), B (cả hai vế), N (không xuất hiện).",
      "snapshot": "L = [A, B], R = [E], B = [C, D], N = []"
    },
    {
      "stepNumber": 2,
      "title": "Xác định tập nguồn cơ sở K_min",
      "description": "Tập K_min = L ∪ N bắt buộc phải có mặt trong mọi khóa vì các thuộc tính này không thể được suy diễn từ thuộc tính khác.",
      "snapshot": "K_min = [A, B]"
    },
    {
      "stepNumber": 3,
      "title": "Kiểm tra bao đóng (K_min)+",
      "description": "Vì (K_min)+ = U (5 thuộc tính), K_min chính là KHÓA DUY NHẤT của lược đồ quan hệ.",
      "snapshot": "(K_min)+ = U -> Khóa duy nhất: [A, B]"
    }
  ]
}
```

---

### 2.2. Tìm phủ tối thiểu (Minimal Cover)
- **URL:** `POST /api/algorithms/minimal-cover`
- **Mục đích:** Tách vế phải, rút gọn vế trái, loại bỏ phụ thuộc hàm dư thừa.
- **Request Body:**
```json
{
  "dependencies": [
    { "left": ["A"], "right": ["B"] },
    { "left": ["B"], "right": ["C"] },
    { "left": ["A"], "right": ["C"] },
    { "left": ["A", "B"], "right": ["D"] }
  ]
}
```
- **Response (200 OK):**
```json
{
  "originalDependencies": [
    { "left": ["A"], "right": ["B"] },
    { "left": ["B"], "right": ["C"] },
    { "left": ["A"], "right": ["C"] },
    { "left": ["A", "B"], "right": ["D"] }
  ],
  "minimalCover": [
    { "left": ["A"], "right": ["B"] },
    { "left": ["B"], "right": ["C"] },
    { "left": ["A"], "right": ["D"] }
  ],
  "steps": [
    {
      "stepNumber": 1,
      "title": "Bước 1: Tách vế phải",
      "description": "Phân rã tất cả các phụ thuộc hàm có vế phải nhiều thuộc tính thành các phụ thuộc hàm có vế phải đơn lẻ.",
      "snapshot": "[A -> B; B -> C; A -> C; A, B -> D]"
    },
    {
      "stepNumber": 2,
      "title": "Bước 2: Rút gọn vế trái",
      "description": "Trong FD [A, B] -> D: Loại bỏ thuộc tính dư thừa 'B' vì ([A])+ = [A, B, C, D] đã chứa 'D'",
      "snapshot": "[A -> B; B -> C; A -> C; A -> D]"
    },
    {
      "stepNumber": 3,
      "title": "Bước 3: Loại bỏ phụ thuộc hàm dư thừa",
      "description": "Loại bỏ FD dư thừa [A -> C] vì vế trái [A] có bao đóng trên F còn lại là [A, B, C, D] (đã chứa 'C')",
      "snapshot": "[A -> B; B -> C; A -> D]"
    }
  ]
}
```

---

### 2.3. Kiểm tra dạng chuẩn (Normal Form Checking)
- **URL:** `POST /api/algorithms/check-nf`
- **Mục đích:** Xác định dạng chuẩn cao nhất (1NF, 2NF, 3NF, BCNF) và giải thích chi tiết các vi phạm.
- **Request Body:**
```json
{
  "universe": ["A", "B", "C", "D"],
  "dependencies": [
    { "left": ["A", "B"], "right": ["C"] },
    { "left": ["C"], "right": ["D"] },
    { "left": ["D"], "right": ["B"] }
  ]
}
```
- **Response (200 OK):**
```json
{
  "highestNormalForm": "3NF",
  "is2NF": true,
  "is3NF": true,
  "isBCNF": false,
  "violations2NF": [],
  "violations3NF": [],
  "violationsBCNF": [
    "FD [C] -> D vi phạm BCNF: Vế trái [C] không phải siêu khóa (([C])+ != U)",
    "FD [D] -> B vi phạm BCNF: Vế trái [D] không phải siêu khóa (([D])+ != U)"
  ],
  "steps": [
    {
      "stepNumber": 1,
      "title": "Bước 1: Xác định tập khóa và các loại thuộc tính",
      "description": "Các khóa tìm được: [[A, B], [A, C], [A, D]]. Thuộc tính khóa (Prime): [A, B, C, D]. Thuộc tính không khóa: [].",
      "snapshot": "Khóa: [[A, B], [A, C], [A, D]]"
    },
    {
      "stepNumber": 2,
      "title": "Bước 2: Kiểm tra dạng chuẩn 2NF",
      "description": "Lược đồ đạt chuẩn 2NF: Mọi thuộc tính không khóa đều phụ thuộc đầy đủ vào các khóa chính.",
      "snapshot": "ĐẠT 2NF"
    },
    {
      "stepNumber": 3,
      "title": "Bước 3: Kiểm tra dạng chuẩn 3NF",
      "description": "Lược đồ đạt chuẩn 3NF: Với mọi X -> A, hoặc X là siêu khóa hoặc A là thuộc tính khóa.",
      "snapshot": "ĐẠT 3NF"
    },
    {
      "stepNumber": 4,
      "title": "Bước 4: Kiểm tra dạng chuẩn BCNF",
      "description": "Lược đồ KHÔNG đạt BCNF do có 2 phụ thuộc hàm mà vế trái không phải siêu khóa.",
      "snapshot": "VI PHẠM BCNF"
    },
    {
      "stepNumber": 5,
      "title": "Kết luận dạng chuẩn cao nhất",
      "description": "Dạng chuẩn cao nhất của lược đồ là: 3NF",
      "snapshot": "3NF"
    }
  ]
}
```

---

### 2.4. Phân rã về 3NF (3NF Decomposition)
- **URL:** `POST /api/algorithms/decompose-3nf`
- **Mục đích:** Phân rã lược đồ về các quan hệ con đạt 3NF, bảo toàn phụ thuộc hàm và không mất mát thông tin (Lossless Join).
- **Request Body:**
```json
{
  "universe": ["A", "B", "C", "D"],
  "dependencies": [
    { "left": ["A"], "right": ["B"] },
    { "left": ["B"], "right": ["C"] },
    { "left": ["C"], "right": ["D"] }
  ]
}
```
- **Response (200 OK):**
```json
{
  "subSchemas": [
    ["A", "B"],
    ["B", "C"],
    ["C", "D"]
  ],
  "minimalCover": [
    { "left": ["A"], "right": ["B"] },
    { "left": ["B"], "right": ["C"] },
    { "left": ["C"], "right": ["D"] }
  ],
  "steps": [
    {
      "stepNumber": 1,
      "title": "Bước 1: Tìm tập phủ tối thiểu F_min",
      "description": "Thu được phủ tối thiểu gồm 3 phụ thuộc hàm làm cơ sở phân rã bảo toàn phụ thuộc.",
      "snapshot": "[{A} -> {B}, {B} -> {C}, {C} -> {D}]"
    },
    {
      "stepNumber": 2,
      "title": "Bước 2: Gom các phụ thuộc hàm cùng vế trái tạo các quan hệ con R_i",
      "description": "Tạo được 3 quan hệ con từ các nhóm vế trái của F_min.",
      "snapshot": "[[A, B], [B, C], [C, D]]"
    },
    {
      "stepNumber": 3,
      "title": "Bước 3: Kiểm tra tính bảo toàn thông tin (Lossless Join)",
      "description": "Đã có ít nhất một quan hệ con chứa trọn vẹn khóa của U -> Đảm bảo phép nối không mất mát thông tin.",
      "snapshot": "Đã thỏa mãn Lossless Join"
    },
    {
      "stepNumber": 4,
      "title": "Bước 4: Loại bỏ quan hệ con dư thừa (R_i ⊆ R_j)",
      "description": "Sau khi loại bỏ các tập con dư thừa, thu được 3 quan hệ con chuẩn 3NF.",
      "snapshot": "[[A, B], [B, C], [C, D]]"
    }
  ]
}
```
