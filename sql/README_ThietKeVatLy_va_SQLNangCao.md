# BÁO CÁO THIẾT KẾ VẬT LÝ VÀ SQL NÂNG CAO
## Đề tài: Hệ thống Quản lý Đào tạo Đại học
**Nhóm:** N10 — Cơ sở Dữ liệu Nâng cao  
**Hệ quản trị CSDL mục tiêu:** PostgreSQL 15+ / 16+

---

## 1. TỔNG QUAN VÀ ÁNH XẠ TỪ CHUẨN HÓA SANG THIẾT KẾ VẬT LÝ

Sau khi thực hiện phân rã chuẩn hóa (3NF / BCNF) từ lược đồ quan hệ tổng quát $U$ với 21 thuộc tính và tập phụ thuộc hàm $F$, hệ thống được chia thành **8 quan hệ con độc lập, không còn dư thừa dữ liệu**:

| STT | Bảng (Table) | Khóa chính (PK) | Khóa ngoại (FK) | Ý nghĩa nghiệp vụ |
|:---:|:---|:---|:---|:---|
| 1 | `Khoa` | `MaKhoa` | — | Lưu danh mục khoa |
| 2 | `Nganh` | `MaNganh` | `MaKhoa` $\to$ `Khoa` | Ngành đào tạo thuộc một khoa |
| 3 | `GiangVien` | `MaGV` | `MaKhoa` $\to$ `Khoa` | Giảng viên và học vị |
| 4 | `Phong` | `MaPhong` | — | Phòng học, tòa nhà và sức chứa |
| 5 | `HocPhan` | `MaHP` | — | Môn học và số tín chỉ |
| 6 | `SinhVien` | `MaSV` | `MaNganh` $\to$ `Nganh` | Sinh viên thuộc ngành đào tạo |
| 7 | `LopHocPhan` | `MaLHP` | `MaHP`, `MaGV`, `MaPhong` | Lớp môn học mở trong một học kỳ |
| 8 | `DangKy` | (`MaSV`, `MaLHP`) | `MaSV`, `MaLHP` | Đăng ký môn và kết quả điểm số |

---

## 2. CÁC QUYẾT ĐỊNH THIẾT KẾ VẬT LÝ (PHYSICAL DESIGN)

### 2.1. Lựa chọn kiểu dữ liệu tối ưu
- **Khóa chính định danh mã:** Dùng `CHAR(10)` hoặc `CHAR(15)` cho các mã có độ dài cố định/chuẩn hóa (`MaSV`, `MaGV`, `MaHP`, `MaLHP`) để tiết kiệm bộ nhớ và tối ưu B-Tree indexing.
- **Chuỗi ký tự biến đổi:** Dùng `VARCHAR(100)` cho họ tên, tên môn, địa chỉ để không lãng phí byte đệm trống.
- **Số nguyên nhỏ:** Dùng `SMALLINT` cho `HocKy` (1–3), `Thu` (2–7), `TietBD` (1–12), `SoTC` (1–10) để chỉ chiếm 2 bytes thay vì 4 bytes (`INT`).
- **Điểm số:** Dùng `NUMERIC(4,2)` thay vì `FLOAT` để tránh sai số dấu phẩy động trong xếp loại học lực.

### 2.2. Chiến lược chỉ mục (Indexing Strategy)
Ngoài các chỉ mục B-Tree mặc định trên Khóa chính:
1. **Foreign Key Index:** Đánh chỉ mục trên tất cả các cột khóa ngoại (`MaNganh`, `MaHP`, `MaGV`, `MaPhong`, `MaSV`, `MaLHP`) nhằm triệt tiêu hiện tượng *Sequential Scan* khi thực hiện `JOIN`.
2. **Composite Index:** Tạo chỉ mục tổ hợp `(HocKy, NamHoc)` trên bảng `LopHocPhan` để tối ưu tra cứu thời khóa biểu từng kỳ.
3. **Partial Index:** Tạo chỉ mục có điều kiện `idx_dangky_diem ON DangKy(Diem) WHERE Diem IS NOT NULL;` giúp tăng tốc độ tính điểm trung bình mà không cần lưu trữ các bản ghi sinh viên chưa có điểm.

---

## 3. CƠ CẤU BỘ MÃ NGUỒN SQL ĐÃ XÂY DỰNG

Toàn bộ mã nguồn nằm tại thư mục `sql/` gồm 7 tệp thực thi tuần tự:

```text
sql/
├── 01_DDL_thiet_ke_vat_ly.sql          # Tạo schema, 8 bảng, PK, FK, CHECK, INDEX
├── 02_DML_du_lieu_mau.sql              # Nạp dữ liệu mẫu thực tế
├── 03_SQL_nang_cao_truy_van.sql        # 10 truy vấn phức tạp (Window fn, CTE, Recursive)
├── 04_View_va_MaterializedView.sql     # View chi tiết & Materialized View xếp hạng GPA
├── 05_Stored_Procedures_va_Functions.sql # Hàm tính GPA, kiểm tra trùng lịch, SP đăng ký
├── 06_Triggers_va_RangBuocNangCao.sql  # Trigger kiểm soát sĩ số, chống trùng giờ, Audit Log
├── 07_Transactions_va_Concurrency.sql  # ACID, Savepoint, Locking, Chuyển lớp nguyên tử
└── README_ThietKeVatLy_va_SQLNangCao.md # Tài liệu hướng dẫn & giải trình kỹ thuật
```

---

## 4. CHI TIẾT CÁC TÍNH NĂNG SQL NÂNG CAO NỔI BẬT

### 4.1. Truy vấn nâng cao (Advanced Queries)
- **Truy vấn 5 (Window Function):** Dùng `RANK() OVER (PARTITION BY MaNganh ORDER BY AVG(Diem) DESC)` để xếp hạng sinh viên trong từng ngành học.
- **Truy vấn 8 (Self-Join Conflict Detection):** Tự kết nối bảng `LopHocPhan` để phát hiện giảng viên bị phân công trùng tiết dạy.
- **Truy vấn 10 (Recursive CTE):** Phân tích đồ thị phụ thuộc môn học tiên quyết (Cây lộ trình đào tạo).

### 4.2. Materialized View & Hiệu năng
- `MV_BangXepHangSinhVien` tính toán sẵn GPA có trọng số tín chỉ và xếp loại (Xuất sắc/Giỏi/Khá/Trung bình/Yếu) cho hàng chục nghìn sinh viên.
- Có chỉ mục `UNIQUE INDEX idx_mv_bxh_masv` hỗ trợ câu lệnh:
  ```sql
  REFRESH MATERIALIZED VIEW CONCURRENTLY qldt.MV_BangXepHangSinhVien;
  ```
  giúp làm mới dữ liệu trong nền mà không làm khóa bảng đọc của người dùng.

### 4.3. Thủ tục lưu trữ & Điều khiển tương tranh (PL/pgSQL)
- Thủ tục `sp_DangKyHocPhan`:
  - Khóa bi quan `SELECT SiSoToiDa FROM LopHocPhan WHERE MaLHP = ... FOR UPDATE;` để giải quyết triệt để lỗi **Race Condition** khi nhiều sinh viên cùng nhấn đăng ký suất cuối cùng.
  - Tích hợp hàm `fn_KiemTraXungDotLichSV` để tự động ngăn chặn sinh viên đăng ký hai môn trùng giờ học.

### 4.4. Trigger ràng buộc toàn vẹn & Kiểm toán (Audit Logging)
- `tg_KiemTraSiSoDangKy`: Ngăn chặn mọi lệnh `INSERT` vào bảng `DangKy` nếu sĩ số vượt ngưỡng.
- `tg_KiemTraLichLopHocPhan`: Đảm bảo 1 phòng học hoặc 1 giảng viên không bao giờ tồn tại ở 2 lớp cùng tiết.
- `tg_GhiLichSuSuaDiem`: Tự động ghi lại toàn bộ lịch sử ai đã sửa điểm, điểm cũ, điểm mới và mốc thời gian vào bảng `LichSuThayDoiDiem`.

---

## 5. HƯỚNG DẪN CHẠY THỬ NGHIỆM

Bạn có thể chạy toàn bộ các file trên PostgreSQL thông qua **pgAdmin**, **DBeaver** hoặc công cụ dòng lệnh **psql**:

```bash
# 1. Kết nối và tạo CSDL
createdb -U postgres qldt_db

# 2. Chạy lần lượt các script
psql -U postgres -d qldt_db -f sql/01_DDL_thiet_ke_vat_ly.sql
psql -U postgres -d qldt_db -f sql/02_DML_du_lieu_mau.sql
psql -U postgres -d qldt_db -f sql/03_SQL_nang_cao_truy_van.sql
psql -U postgres -d qldt_db -f sql/04_View_va_MaterializedView.sql
psql -U postgres -d qldt_db -f sql/05_Stored_Procedures_va_Functions.sql
psql -U postgres -d qldt_db -f sql/06_Triggers_va_RangBuocNangCao.sql
psql -U postgres -d qldt_db -f sql/07_Transactions_va_Concurrency.sql
```
