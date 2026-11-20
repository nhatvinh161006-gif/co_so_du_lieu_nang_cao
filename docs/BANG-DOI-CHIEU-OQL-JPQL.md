# BẢNG ĐỐI CHIẾU CÚ PHÁP TRUY VẤN OQL (ODMG 3.0) VÀ JPQL (OBJECTDB JPA)

**Học phần:** Cơ sở dữ liệu nâng cao  
**Đề tài 10:** Xây dựng công cụ hỗ trợ phân tích phụ thuộc hàm và chuẩn hóa, áp dụng cho CSDL quản lý đào tạo  
**Phân hệ phụ trách:** CSDL Hướng đối tượng (OODBMS)  
**Nhánh thực hiện:** `MaGiaVy`  
**Tài liệu tham chiếu:**
- [Công việc.docx](file:///d:/CSDL_NC/Công việc.docx) (Mục 4.3: Viết truy vấn OQL / JPQL)
- [goi-sv-huong-dan-cac-de-tai-csdl-nang-cao.md](file:///d:/CSDL_NC/goi-sv-huong-dan-cac-de-tai-csdl-nang-cao.md) (Mục 1 & Mục 4: Yêu cầu phân biệt OQL lý thuyết và bảng ánh xạ sang JPQL)

---

## 1. TỔNG QUAN VỀ OQL VÀ JPQL TRONG CSDL HƯỚNG ĐỐI TƯỢNG

Trong nghiên cứu và triển khai CSDL Hướng đối tượng (OODBMS), sinh viên cần phân biệt rõ giữa **mô hình lý thuyết** và **công cụ thực nghiệm**:

1. **OQL (Object Query Language):**
   - Là chuẩn ngôn ngữ truy vấn hướng đối tượng do tổ chức **ODMG (Object Data Management Group - chuẩn ODMG 3.0)** ban hành.
   - OQL được thiết kế dựa trên cú pháp khai báo giống SQL nhưng mở rộng để thao tác trực tiếp với các khái niệm hướng đối tượng: định danh đối tượng (OID), quan hệ kế thừa (inheritance), điều hướng dấu chấm (path navigation), và các kiểu tập hợp đa trị (`Set`, `List`, `Bag`, `Array`).
   - Đây là ngôn ngữ được giảng dạy trong giáo trình lý thuyết chuẩn (*Fundamentals of Database Systems - Elmasri & Navathe*).

2. **JPQL (Java Persistence Query Language):**
   - Là ngôn ngữ truy vấn tiêu chuẩn của Java EE / Jakarta EE (JPA) được công cụ **ObjectDB** hiện thực hóa trực tiếp trên file CSDL `.odb`.
   - JPQL cho phép lập trình viên viết câu truy vấn dựa trên các lớp Java Entity thay vì các bảng quan hệ dưới CSDL.

3. **Mục đích của Bảng đối chiếu:**
   - Cung cấp minh chứng lý thuyết chuẩn xác cho Thầy khi chấm điểm Báo cáo & Slide bảo vệ.
   - Cung cấp mã nguồn thực tế chạy được 100% trong chương trình Java kết nối ObjectDB.

---

## 2. BẢNG ĐỐI CHIẾU CHI TIẾT BỘ 10 CÂU TRUY VẤN ĐẶC TRƯNG

| STT | Loại Truy Vấn & Ý Nghĩa Nghiệp Vụ | Cú pháp OQL Lý Thuyết (ODMG 3.0) | Cú pháp JPQL Thực Tế (ObjectDB JPA) | So Sánh & Điểm Ưu Việt Của OODBMS So Với SQL Quan Hệ (RDBMS) |
| :---: | :--- | :--- | :--- | :--- |
| **1** | **Truy vấn Đa hình (Polymorphism Query)**<br>*Mục tiêu:* Truy vấn từ lớp cha `Nguoi` để lấy danh sách nhân sự/sinh viên có giới tính là "Nữ". Kết quả trả về đối tượng của cả 2 lớp con kế thừa (`GiangVien` và `SinhVien`). | ```sql<br>SELECT p<br>FROM Nguoi p<br>WHERE p.gioiTinh = 'Nữ'<br>``` | ```sql<br>SELECT p<br>FROM Nguoi p<br>WHERE p.gioiTinh = 'Nữ'<br>``` | • **RDBMS:** Bắt buộc phải tạo 2 bảng riêng `SinhVien` và `GiangVien`, sau đó dùng lệnh `UNION ALL` hoặc thực hiện 2 câu query riêng.<br>• **OODBMS:** Quét trực tiếp trên Extent của lớp cha `Nguoi`, tự động trả về thể hiện đa hình của các lớp con trong bộ nhớ mà không cần `UNION`. |
| **2** | **Điều hướng Đường dẫn (Path Navigation)**<br>*Mục tiêu:* Liệt kê sinh viên và môn học được xếp lịch học tại các phòng thuộc **"Tòa A"**. Truy vết liên cấp: `DangKyMon` $\rightarrow$ `LopHocPhan` $\rightarrow$ `PhongHoc`. | ```sql<br>SELECT d.sinhVien.hoTen,<br>       d.lopHocPhan.hocPhan.tenHP,<br>       d.lopHocPhan.phongHoc.maPhong,<br>       d.lopHocPhan.phongHoc.toaNha<br>FROM DangKyMon d<br>WHERE d.lopHocPhan.phongHoc.toaNha = 'Tòa A'<br>``` | ```sql<br>SELECT d.sinhVien.hoTen,<br>       d.lopHocPhan.hocPhan.tenHP,<br>       d.lopHocPhan.phongHoc.maPhong,<br>       d.lopHocPhan.phongHoc.toaNha<br>FROM DangKyMon d<br>WHERE d.lopHocPhan.phongHoc.toaNha = 'Tòa A'<br>``` | • **RDBMS:** Phải viết 3 phép nối khóa ngoại: `DangKy JOIN LopHocPhan ON ... JOIN PhongHoc ON ... JOIN HocPhan ON ...` rất dài dòng và tốn chi phí thực thi (quét bảng chéo).<br>• **OODBMS:** Duyệt con trỏ tham chiếu đối tượng trực tiếp bằng toán tử dấu chấm `.`, **loại bỏ hoàn toàn các lệnh JOIN**. |
| **3** | **Thao tác trên Thuộc tính Tập hợp (Collection Operation)**<br>*Mục tiêu:* Tìm các lớp học phần có sĩ số đăng ký đông (từ 3 sinh viên trở lên) dựa trên thuộc tính tập hợp `List<DangKyMon> dsDangKy` lồng trong `LopHocPhan`. | ```sql<br>SELECT l<br>FROM LopHocPhan l<br>WHERE count(l.dsDangKy) >= 3<br>``` | ```sql<br>SELECT l<br>FROM LopHocPhan l<br>WHERE SIZE(l.dsDangKy) >= 3<br>``` | • **RDBMS:** Do vi phạm chuẩn 1NF (thuộc tính không được đa trị), RDBMS phải tách bảng riêng và dùng `GROUP BY l.maLHP HAVING COUNT(*) >= 3`.<br>• **OODBMS:** Lưu trực tiếp cấu trúc danh sách lồng trong đối tượng. OQL dùng hàm `count(collection)`, JPQL dùng hàm `SIZE(collection)`. |
| **4** | **Truy vấn Lọc Nâng cao Theo Nghiệp vụ**<br>*Mục tiêu:* Tìm sinh viên học môn "Cơ Sở Dữ Liệu Nâng Cao" (`CSDL201`) đạt kết quả loại Giỏi/Xuất sắc (Điểm tổng kết $\ge 8.0$). Điểm tổng kết tính theo trọng số: $10\% \text{CC} + 30\% \text{GK} + 60\% \text{CK}$. | ```sql<br>SELECT d<br>FROM DangKyMon d<br>WHERE d.lopHocPhan.hocPhan.maHP = 'CSDL201'<br>  AND (d.diemChuyenCan * 0.1 +<br>       d.diemGiuaKy * 0.3 +<br>       d.diemCuoiKy * 0.6) >= 8.0<br>``` | ```sql<br>SELECT d<br>FROM DangKyMon d<br>WHERE d.lopHocPhan.hocPhan.maHP = 'CSDL201'<br>  AND (d.diemChuyenCan * 0.1 +<br>       d.diemGiuaKy * 0.3 +<br>       d.diemCuoiKy * 0.6) >= 8.0<br>``` | • **RDBMS:** Phải tính toán trên các cột của bảng đăng ký và nối với bảng môn học.<br>• **OODBMS:** Cho phép tính toán công thức đại số ngay trong mệnh đề `SELECT` và `WHERE`, trả về trực tiếp đối tượng thực thể `DangKyMon`. |
| **5** | **Truy vấn Phân cấp & Điều kiện Tiên quyết (Prerequisite Query)**<br>*Mục tiêu:* Tìm tất cả các môn học có yêu cầu môn học tiên quyết và lấy ra mã môn, tên môn học tiên quyết tương ứng. | ```sql<br>SELECT h<br>FROM HocPhan h<br>WHERE h.hocPhanTienQuyet IS NOT NULL<br>``` | ```sql<br>SELECT h<br>FROM HocPhan h<br>WHERE h.hocPhanTienQuyet IS NOT NULL<br>``` | • **RDBMS:** Bắt buộc phải dùng kỹ thuật **Self-Join** (tự nối bảng `HOCPHAN h1 JOIN HOCPHAN h2 ON h1.MaTienQuyet = h2.MaHP`).<br>• **OODBMS:** Biểu diễn tự nhiên dạng con trỏ đối tượng tự liên kết (Self-reference), chỉ cần `h.hocPhanTienQuyet`. |
| **6** | **Kiểm tra Thuộc tính Tập hợp Rỗng (Collection IS EMPTY)**<br>*Mục tiêu:* Tìm các môn học chưa mở bất kỳ lớp học phần nào trong học kỳ này để phòng đào tạo có kế hoạch xếp thời khóa biểu. | ```sql<br>SELECT h<br>FROM HocPhan h<br>WHERE is_empty(h.dsLopHocPhan)<br>``` | ```sql<br>SELECT h<br>FROM HocPhan h<br>WHERE h.dsLopHocPhan IS EMPTY<br>``` | • **RDBMS:** Phải dùng phép nối ngoài `LEFT JOIN LOPHOCPHAN l ON ... WHERE l.MaLHP IS NULL` phức tạp.<br>• **OODBMS:** Toán tử `IS EMPTY` kiểm tra trực tiếp trạng thái rỗng của thuộc tính tập hợp `List<LopHocPhan>` cực kỳ trực quan. |
| **7** | **Thống kê Kết gộp Phân tích Điểm (Aggregation)**<br>*Mục tiêu:* Thống kê điểm thi kết thúc học phần trung bình (`AVG`), điểm nhỏ nhất (`MIN`) và lớn nhất (`MAX`) theo từng lớp học phần. | ```sql<br>SELECT d.lopHocPhan.maLHP,<br>       d.lopHocPhan.hocPhan.tenHP,<br>       avg(d.diemCuoiKy),<br>       min(d.diemCuoiKy),<br>       max(d.diemCuoiKy)<br>FROM DangKyMon d<br>GROUP BY d.lopHocPhan.maLHP,<br>         d.lopHocPhan.hocPhan.tenHP<br>``` | ```sql<br>SELECT d.lopHocPhan.maLHP,<br>       d.lopHocPhan.hocPhan.tenHP,<br>       AVG(d.diemCuoiKy),<br>       MIN(d.diemCuoiKy),<br>       MAX(d.diemCuoiKy)<br>FROM DangKyMon d<br>GROUP BY d.lopHocPhan.maLHP,<br>         d.lopHocPhan.hocPhan.tenHP<br>``` | • **RDBMS & OODBMS:** Cả hai đều hỗ trợ kết gộp thống kê theo nhóm (`GROUP BY`), nhưng OODBMS cho phép nhóm theo thuộc tính của các đối tượng liên kết mà không cần lệnh `JOIN` tường minh. |
| **8** | **Truy vấn Phân công Giảng dạy Từ Giảng viên**<br>*Mục tiêu:* Liệt kê thời khóa biểu, phòng học và môn học được phân công của từng Giảng viên bằng cách duyệt từ đối tượng Giảng viên sang tập hợp lớp phụ trách. | ```sql<br>SELECT g.hoTen, g.hocVi,<br>       l.maLHP, l.hocPhan.tenHP,<br>       l.phongHoc.maPhong<br>FROM GiangVien g, g.dsLopGiangDay l<br>``` | ```sql<br>SELECT g.hoTen, g.hocVi,<br>       l.maLHP, l.hocPhan.tenHP,<br>       l.phongHoc.maPhong<br>FROM GiangVien g JOIN g.dsLopGiangDay l<br>``` | • **RDBMS:** Phải nối bảng `GIANGVIEN` với bảng `LOPHOCPHAN` thông qua khóa ngoại `MaGV`.<br>• **OODBMS:** Duyệt trực tiếp thuộc tính tập hợp `g.dsLopGiangDay` lồng trong đối tượng Giảng viên. |
| **9** | **Truy vấn Cảnh báo Học vụ (Academic Warning)**<br>*Mục tiêu:* Rà soát các sinh viên có nguy cơ bị cảnh cáo học vụ do rớt môn (Điểm tổng kết $< 4.0$) hoặc điểm chuyên cần bị điểm liệt ($< 5.0$). | ```sql<br>SELECT d<br>FROM DangKyMon d<br>WHERE (d.diemChuyenCan * 0.1 +<br>       d.diemGiuaKy * 0.3 +<br>       d.diemCuoiKy * 0.6) < 4.0<br>   OR d.diemChuyenCan < 5.0<br>``` | ```sql<br>SELECT d<br>FROM DangKyMon d<br>WHERE (d.diemChuyenCan * 0.1 +<br>       d.diemGiuaKy * 0.3 +<br>       d.diemCuoiKy * 0.6) < 4.0<br>   OR d.diemChuyenCan < 5.0<br>``` | • **RDBMS:** Viết biểu thức số học trên nhiều cột kết hợp mệnh đề `OR`.<br>• **OODBMS:** Trả về đối tượng `DangKyMon` hoàn chỉnh, từ đó truy xuất được toàn bộ hồ sơ sinh viên, thông tin liên lạc và thông tin môn học. |
| **10** | **Kiểm tra Đối soát Sức chứa Phòng học (Capacity Check)**<br>*Mục tiêu:* Thống kê sĩ số thực tế đối chiếu với sức chứa tối đa của phòng học để tính tỷ lệ lấp đầy chỗ ngồi và kịp thời điều chuyển phòng. | ```sql<br>SELECT l<br>FROM LopHocPhan l<br>WHERE count(l.dsDangKy) > 0<br>ORDER BY l.maLHP<br>``` | ```sql<br>SELECT l<br>FROM LopHocPhan l<br>WHERE SIZE(l.dsDangKy) > 0<br>ORDER BY l.maLHP<br>``` | • **RDBMS:** Bắt buộc phải viết `SELECT ... COUNT(*)` kết hợp `GROUP BY` và nối bảng `PhongHoc`.<br>• **OODBMS:** Lấy đối tượng `LopHocPhan` và tự động gọi phương thức nghiệp vụ nội tại `l.getSiSoHienTai()` kết hợp `l.getPhongHoc().getSucChua()` tính toán thời gian thực. |

---

## 3. BẢNG TỔNG KẾT QUY TẮC ÁNH XẠ CÚ PHÁP OQL $\leftrightarrow$ JPQL

| Thành phần cú pháp | Chuẩn OQL (ODMG 3.0) | Chuẩn JPQL (ObjectDB JPA) | Chú thích kỹ thuật |
| :--- | :--- | :--- | :--- |
| **Độ dài tập hợp** | `count(collection)` | `SIZE(collection)` | Dùng trong mệnh đề `WHERE` để đếm phần tử tập hợp |
| **Kiểm tra rỗng** | `is_empty(collection)` | `collection IS EMPTY` | Kiểm tra danh sách có phần tử hay không |
| **Kiểm tra không rỗng** | `is_not_empty(collection)` | `collection IS NOT EMPTY` | Kiểm tra danh sách có ít nhất 1 phần tử |
| **Điều hướng thuộc tính** | Toán tử dấu chấm `.` (vd: `a.b.c`) | Toán tử dấu chấm `.` (vd: `a.b.c`) | Hoàn toàn giống nhau, thay thế phép `JOIN` |
| **Toán tử so sánh** | `=`, `<>`, `<`, `>`, `<=`, `>=` | `=`, `<>`, `<`, `>`, `<=`, `>=` | Giống nhau |
| **Phép toán logic** | `AND`, `OR`, `NOT` | `AND`, `OR`, `NOT` | Giống nhau |
| **Kiểm tra phần tử** | `x IN collection` | `x MEMBER OF collection` | Kiểm tra một đối tượng có nằm trong tập hợp |
| **Truy vấn đa hình** | `SELECT o FROM ParentClass o` | `SELECT o FROM ParentClass o` | Cả 2 đều tự động quét các lớp con kế thừa |
| **Duyệt tập hợp con** | `FROM Parent o, o.childCollection c` | `FROM Parent o JOIN o.childCollection c` | Duyệt qua các phần tử của thuộc tính tập hợp |

---

## 4. HƯỚNG DẪN CHẠY KIỂM THỬ THỰC TẾ

Bạn có thể chạy kiểm thử toàn bộ 10 câu truy vấn trên mã nguồn Java bất kỳ lúc nào:

1. **Chạy qua Maven Wrapper:**
   ```bash
   cd d:\CSDL_NC\co_so_du_lieu_nang_cao\db-normalization-tool
   .\mvnw.cmd exec:java "-Dexec.mainClass=com.doan10.db_normalization_tool.daotao.ObjectDbQueryDemo"
   ```

2. **Chạy qua file Batch:**
   Double click vào file [run-test.bat](file:///d:/CSDL_NC/co_so_du_lieu_nang_cao/db-normalization-tool/run-test.bat) để chương trình tự động làm mới CSDL, nạp dữ liệu, kiểm thử transaction và in kết quả đẹp mắt của cả 10 câu truy vấn ra màn hình.
