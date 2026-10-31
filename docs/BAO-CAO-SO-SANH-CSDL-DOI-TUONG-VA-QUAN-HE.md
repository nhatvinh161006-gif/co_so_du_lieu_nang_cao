# BÁO CÁO KỸ THUẬT: SO SÁNH CSDL HƯỚNG ĐỐI TƯỢNG (OODBMS) VÀ CSDL QUAN HỆ (RDBMS)
**Áp dụng trong:** Hệ thống Quản lý Đào tạo Đại học & Công cụ Chuẩn hóa CSDL (Đề tài 10)  
**Phân hệ phụ trách:** CSDL Hướng đối tượng (ObjectDB) - Người 4 (`MaGiaVy`)  
**Tài liệu căn cứ:**
- [Công việc.docx](file:///d:/CSDL_NC/Công việc.docx) (Mục 4.5: Thuyết minh ưu điểm mô hình đối tượng so với mô hình quan hệ)
- [goi-sv-huong-dan-cac-de-tai-csdl-nang-cao.md](file:///d:/CSDL_NC/goi-sv-huong-dan-cac-de-tai-csdl-nang-cao.md) (Mục 1 & Đề tài 5, Đề tài 9: So sánh OODBMS và RDBMS, trở ngại trở kháng)

---

## I. ĐẶT VẤN ĐỀ VÀ BỐI CẢNH BÀI TOÁN

Trong bài toán **Quản lý Đào tạo Đại học**, dữ liệu có cấu trúc phân cấp, đa hình và các mối liên kết chằng chịt giữa: Con người (`Nguoi` $\rightarrow$ `SinhVien`, `GiangVien`), Chương trình đào tạo (`HocPhan` $\rightarrow$ `LopHocPhan`), Cơ sở vật chất (`PhongHoc`) và Kết quả học tập (`DangKyMon`). 

Bên cạnh đó, phân hệ **Công cụ Chuẩn hóa CSDL** đòi hỏi lưu trữ các cấu trúc giải thuật động (tập thuộc tính $U$, tập phụ thuộc hàm $F$, các bước phân rã theo cây nhị phân...).

Việc áp dụng **Mô hình CSDL Hướng đối tượng (OODBMS - cụ thể là ObjectDB)** bên cạnh **Mô hình CSDL Quan hệ truyền thống (RDBMS - PostgreSQL / SQL Server)** mang lại những góc nhìn đối chiếu sâu sắc về mặt lý thuyết và hiệu quả thực nghiệm kỹ thuật. Báo cáo này phân tích chi tiết các điểm vượt trội của OODBMS trong bài toán Đề tài 10.

---

## II. SO SÁNH CHI TIẾT CÁC ĐẶC TRƯNG CỐT LÕI

### 1. Định danh Đối tượng (OID) vs Khóa chính / Khóa ngoại (PK / FK)

| Tiêu chí | CSDL Quan hệ (RDBMS) | CSDL Hướng đối tượng (OODBMS - ObjectDB) |
| :--- | :--- | :--- |
| **Bản chất định danh** | Dựa trên **giá trị dữ liệu thuộc tính** (Data-dependent). Sử dụng cột khóa tự nhiên (Số CCCD, Mã SV) hoặc số tự tăng nhân tạo (`SERIAL`, `IDENTITY`). | Dựa trên **định danh vật lý bất biến** (System-generated OID). Do ObjectDB tự động cấp phát khi lưu vào đĩa, hoàn toàn độc lập với giá trị thuộc tính. |
| **Quan hệ liên kết** | Khóa ngoại (Foreign Key) lưu trữ giá trị bản sao của khóa chính bảng cha. Bắt buộc phải trùng khớp kiểu dữ liệu và giá trị. | Con trỏ đối tượng trực tiếp (Object Reference). Trường liên kết giữ trực tiếp địa chỉ vùng nhớ/OID của đối tượng đích. |
| **Khi dữ liệu thay đổi** | Khi cập nhật giá trị khóa chính (ví dụ: đổi mã sinh viên, cập nhật số CCCD), phải kích hoạt cơ chế `ON UPDATE CASCADE` trên toàn bộ các bảng con, tiềm ẩn nguy cơ khóa bảng (table locking) hoặc lỗi toàn vẹn. | OID là bất biến trong suốt vòng đời đối tượng. Người dùng có thể sửa đổi bất kỳ thuộc tính nào (`maSV`, `hoTen`...) mà không ảnh hưởng đến bất kỳ liên kết tham chiếu nào. |
| **Tính toàn vẹn tham chiếu** | Dễ gặp hiện tượng bản ghi mồ côi (Orphaned records) hoặc lỗi ràng buộc khi xóa bản ghi cha nếu cấu hình FK không chặt chẽ. | Mối quan hệ giữa các đối tượng được duy trì tự nhiên qua đồ thị đối tượng. Khi xóa đối tượng, JPA Cascade tự động dọn dẹp hoặc gán `null` an toàn. |

> **Minh chứng mã nguồn:**
> Trong lớp [LopHocPhan.java](file:///d:/CSDL_NC/co_so_du_lieu_nang_cao/db-normalization-tool/src/main/java/com/doan10/db_normalization_tool/entity/LopHocPhan.java):
> - **RDBMS:** Lưu `ma_hp VARCHAR`, `ma_gv VARCHAR`, `ma_phong VARCHAR` làm khóa ngoại. Khi truy vấn phải join ngược lại.
> - **OODBMS:** Lưu trực tiếp `@ManyToOne private HocPhan hocPhan;`, `@ManyToOne private GiangVien giangVien;`, `@ManyToOne private PhongHoc phongHoc;`. Không tồn tại khái niệm "khóa ngoại" dạng chuỗi hay số trong mô hình đối tượng.

---

### 2. Điều hướng Đường dẫn (Path Navigation) vs Phép nối Quan hệ (SQL JOIN)

Trong nghiệp vụ đào tạo, nhu cầu truy vấn thông tin xuyên suốt nhiều thực thể liên kết xảy ra rất thường xuyên.

#### Xét bài toán nghiệp vụ cụ thể:
*"Tìm danh sách họ tên sinh viên và tên môn học được xếp lịch học tại các phòng thuộc Tòa A."*

* **Cách tiếp cận trong CSDL Quan hệ (RDBMS - SQL):**
  Bắt buộc phải thực hiện **3 phép nối bảng (INNER JOIN)**:
  ```sql
  SELECT sv.ho_ten, hp.ten_hp, ph.ma_phong
  FROM DANG_KY dk
  JOIN SINH_VIEN sv ON dk.sinh_vien_id = sv.id
  JOIN LOP_HOC_PHAN lhp ON dk.lop_hoc_phan_id = lhp.id
  JOIN PHONG_HOC ph ON lhp.phong_hoc_id = ph.id
  JOIN HOC_PHAN hp ON lhp.hoc_phan_id = hp.id
  WHERE ph.toa_nha = 'Tòa A';
  ```
  - *Hạn chế của RDBMS:* Câu lệnh dài dòng, lập trình viên phải nhớ rõ từng cặp cột khóa ngoại để nối. Về mặt vật lý, hệ quản trị phải quét chỉ mục (Index Scan), tạo bảng tạm (Temp Table) và tính tích Descartes cục bộ (Nested Loop / Hash Join), tốn rất nhiều tài nguyên CPU và RAM.

* **Cách tiếp cận trong CSDL Hướng đối tượng (OODBMS - OQL / JPQL):**
  Sử dụng **Điều hướng đường dẫn (Path Navigation)** bằng toán tử dấu chấm `.`:
  ```sql
  SELECT d.sinhVien.hoTen, d.lopHocPhan.hocPhan.tenHP, d.lopHocPhan.phongHoc.maPhong
  FROM DangKyMon d
  WHERE d.lopHocPhan.phongHoc.toaNha = 'Tòa A'
  ```
  - *Ưu việt của OODBMS:*
    1. **Cú pháp trong sáng, trực quan:** Lập trình viên tư duy đúng theo cây quan hệ đối tượng (`d` $\rightarrow$ `lopHocPhan` $\rightarrow$ `phongHoc` $\rightarrow$ `toaNha`), không cần bất kỳ từ khóa `JOIN` nào.
    2. **Độ phức tạp thuật toán:** Thay vì phép nối $O(N \log M)$ hay $O(N \times M)$ như RDBMS, ObjectDB chỉ thực hiện thao tác **Dereference con trỏ trực tiếp** với chi phí $O(1)$ cho mỗi liên kết đối tượng, tốc độ đọc vượt trội khi dữ liệu phân tán nhiều tầng.

---

### 3. Đóng gói Hành vi (Encapsulation) vs Bảng dữ liệu Thụ động

* **CSDL Quan hệ (RDBMS):**
  - Các bảng trong RDBMS chỉ là các cấu trúc dữ liệu phẳng và **hoàn toàn thụ động** (Dumb Data Holders). Bảng chỉ chứa dữ liệu, không có khả năng tự xử lý.
  - Mọi logic nghiệp vụ (như tính điểm trung bình tích lũy theo số tín chỉ, kiểm tra lịch học bị trùng, kiểm tra môn tiên quyết) buộc phải:
    + Hoặc viết phân tán ở tầng ứng dụng (Java, C#).
    + Hoặc viết bằng Stored Procedure / Trigger phức tạp trong database, gây khó khăn cho việc bảo trì, debug và phụ thuộc chặt chẽ vào một hệ quản trị cụ thể (Vendor Lock-in).

* **CSDL Hướng đối tượng (OODBMS):**
  - Tuân thủ nguyên lý nền tảng của Lập trình Hướng đối tượng: **Đóng gói dữ liệu cùng với các phương thức xử lý (Data + Behaviors)**.
  - Các quy tắc nghiệp vụ đại học được cài đặt ngay bên trong bản thân Entity:
    1. `SinhVien.kiemTraTrungLich(lopMoi)`: Tự kiểm tra lịch học mới có bị đụng giờ với các lớp đã đăng ký hay không.
    2. `SinhVien.daDatMonTienQuyet(hp)`: Tự duyệt lịch sử học tập để đối soát môn tiên quyết.
    3. `SinhVien.tinhDiemTrungBinh()`: Tự động tính điểm trung bình có trọng số tín chỉ:
       $$\text{ĐTB} = \frac{\sum (\text{Điểm TK} \times \text{Số TC})}{\sum \text{Số TC}}$$
    4. `DangKyMon.tinhDiemTongKet()`: Tự tính điểm tổng kết từ 3 đầu điểm:
       $$\text{Điểm TK} = 10\% \times \text{CC} + 30\% \times \text{GK} + 60\% \times \text{CK}$$
  - Khi đối tượng được tải từ ObjectDB vào bộ nhớ, nó là một đối tượng sống động (Living Object) với đầy đủ chức năng nghiệp vụ, không cần viết thêm các hàm tính toán bên ngoài.

---

### 4. Giải quyết triệt để Trở ngại Trở kháng (Object-Relational Impedance Mismatch)

* **Trở ngại trở kháng là gì?**
  Là sự bất tương thích cố hữu giữa mô hình hướng đối tượng (OOP) của ngôn ngữ lập trình và mô hình quan hệ bảng 2 chiều (Relational) của RDBMS:
  1. *Kế thừa (Inheritance):* Java có `class SinhVien extends Nguoi`, nhưng SQL không có khái niệm kế thừa bảng chuẩn mực, phải ánh xạ ép buộc qua các chiến lược `SINGLE_TABLE`, `JOINED` hay `TABLE_PER_CLASS`.
  2. *Dữ liệu tập hợp (Collections):* Một sinh viên có danh sách nhiều môn đăng ký (`List<DangKyMon>`). RDBMS bắt buộc chuẩn 1NF (nguyên tử hóa) nên phải tách ra bảng phụ nhân tạo và dùng khóa ngoại để liên kết.
  3. *Tầng trung gian ORM (Hibernate):* Để ánh xạ giữa hai mô hình đối nghịch này, các hệ thống bắt buộc phải sử dụng các framework ORM nặng nề (Hibernate, EclipseLink). Tầng này phát sinh hàng trăm truy vấn ẩn (vấn đề $N+1$ query), tốn dung lượng bộ nhớ cache và làm chậm hiệu năng hệ thống.

* **Giải pháp của OODBMS (ObjectDB):**
  - **Xóa bỏ hoàn toàn Trở ngại trở kháng:** Đối tượng Java trong bộ nhớ như thế nào thì được lưu nguyên vẹn vào đĩa như thế đó (Object Graph Persistence).
  - Không cần bộ chuyển đổi ORM phức tạp, không mất công mapping giữa thuộc tính và cột bảng, loại bỏ 100% rủi ro bất tương thích kiểu dữ liệu.

---

### 5. Khả năng Lưu trữ Lịch sử Thuật toán Động (`LichSuChuanHoa`)

* Trong phân hệ Chuẩn hóa CSDL (Đề tài 10), mỗi lượt chạy thuật toán sinh ra các kết quả rất đa dạng:
  - Thuật toán Bao đóng: trả về tập thuộc tính.
  - Thuật toán Phủ tối tiểu: trả về tập các phụ thuộc hàm sau khi rút gọn.
  - Thuật toán Phân rã 3NF / BCNF: sinh ra một cây phân rã lược đồ gồm nhiều bảng con và các bước chứng minh bảo toàn phụ thuộc hàm / nối không mất mát thông tin.
* **Nếu dùng RDBMS:** Phải thiết kế hàng loạt bảng quan hệ phức tạp, bảng cha - con lồng nhau để lưu từng bước giải thuật, hoặc phải ép kiểu lưu dạng chuỗi text cứng nhắc.
* **Khi dùng OODBMS:** Lớp [LichSuChuanHoa.java](file:///d:/CSDL_NC/co_so_du_lieu_nang_cao/db-normalization-tool/src/main/java/com/doan10/db_normalization_tool/entity/LichSuChuanHoa.java) lưu trữ tự nhiên các trường tham số, kèm trường `@Lob private String ketQuaChiTiet` lưu giữ toàn bộ cây phân tích giải thuật một cách linh hoạt, truy xuất cực nhanh theo thời gian thực.

---

## III. BẢNG TỔNG HỢP SO SÁNH TRỰC QUAN OODBMS VS RDBMS

| Tiêu chí Đánh giá | CSDL Hướng đối tượng (OODBMS / ObjectDB) | CSDL Quan hệ (RDBMS / PostgreSQL) | Đánh giá Ưu/Nhược điểm |
| :--- | :--- | :--- | :--- |
| **Mô hình dữ liệu cốt lõi** | Đồ thị đối tượng (Object Graph) gồm Lớp, Thuộc tính, Phương thức | Tập hợp các bảng 2 chiều (Tables) gồm Cột (Columns) và Hàng (Rows) | OODBMS tự nhiên và trực quan hơn cho lập trình viên OOP |
| **Định danh dữ liệu** | OID vật lý bất biến do hệ thống cấp | Khóa chính (PK) tự tăng hoặc khóa tự nhiên | OID an toàn hơn khi sửa đổi dữ liệu thuộc tính |
| **Hỗ trợ Kế thừa (OOP)** | Hỗ trợ tự nhiên (`extends`), hỗ trợ truy vấn Đa hình (Polymorphism) | Không hỗ trợ tự nhiên, phải giả lập qua bảng nối hoặc trường phân loại | OODBMS vượt trội hoàn toàn khi có phân cấp lớp |
| **Hỗ trợ Tập hợp (Collections)** | Lưu trực tiếp `List`, `Set`, `Map` lồng trong đối tượng | Không hỗ trợ (vi phạm 1NF), bắt buộc tách bảng và dùng Foreign Key | OODBMS tránh được việc phân mảnh bảng |
| **Thao tác liên kết dữ liệu** | Path Navigation (`.`) duyệt con trỏ trực tiếp ($O(1)$) | Phép nối `JOIN` quét bảng chéo ($O(N \log M)$) | OODBMS nhanh hơn rõ rệt khi truy vết liên cấp |
| **Đóng gói nghiệp vụ** | Đóng gói phương thức xử lý trực tiếp bên trong Entity | Bảng thụ động, logic phải viết ở Stored Procedure hoặc ứng dụng | OODBMS tuân thủ nguyên lý thiết kế hướng đối tượng chuẩn |
| **Ngôn ngữ truy vấn** | OQL (ODMG 3.0) / JPQL (JPA) | SQL (ANSI SQL) | SQL phổ biến hơn; OQL biểu cảm hơn trên cấu trúc đối tượng |
| **Xử lý báo cáo bảng phẳng** | Cần duyệt đối tượng trước khi xuất bảng | Rất mạnh mẽ với các hàm cửa sổ (Window functions), Pivot | RDBMS mạnh hơn ở các báo cáo tài chính/kế toán bảng phẳng |
| **Tính toàn vẹn giao dịch (ACID)** | Hỗ trợ đầy đủ Transaction Commit & Rollback (JPA Transaction) | Hỗ trợ đầy đủ Transaction với nhiều mức cô lập (Isolation levels) | Cả hai đều đạt chuẩn công nghiệp về độ tin cậy |

---

## IV. KHUNG SƯỜN BẢNG ĐỐI CHIẾU THỰC THỂ QUAN HỆ (NGƯỜI 1) $\leftrightarrow$ LỚP ĐỐI TƯỢNG (NGƯỜI 4)

Khung sườn dưới đây được thiết lập sẵn sàng để đối chiếu song song thiết kế giữa hai phân hệ trong đồ án:

| STT | Bảng trong CSDL Quan hệ (Người 1 - PostgreSQL) | Lớp trong CSDL Hướng đối tượng (Người 4 - ObjectDB) | Cơ chế Khóa ngoại (Người 1) $\leftrightarrow$ Tham chiếu Đối tượng (Người 4) |
| :---: | :--- | :--- | :--- |
| 1 | Bảng `NGUOI` (hoặc tách `SINH_VIEN`, `GIANG_VIEN`) | Lớp cha `Nguoi` & 2 lớp con kế thừa `SinhVien`, `GiangVien` | RDBMS dùng bảng nối hoặc cờ phân loại $\leftrightarrow$ OODBMS dùng quan hệ kế thừa `@Inheritance(JOINED)`. |
| 2 | Bảng `HOC_PHAN` | Lớp `HocPhan` | RDBMS lưu `ma_tien_quyet VARCHAR` $\leftrightarrow$ OODBMS trỏ con trỏ trực tiếp `@ManyToOne private HocPhan hocPhanTienQuyet`. |
| 3 | Bảng `PHONG_HOC` | Lớp `PhongHoc` | RDBMS định danh qua `ma_phong` $\leftrightarrow$ OODBMS định danh qua OID tự sinh. |
| 4 | Bảng `LOP_HOC_PHAN` | Lớp `LopHocPhan` | RDBMS chứa 3 khóa ngoại `ma_hp`, `ma_gv`, `ma_phong` $\leftrightarrow$ OODBMS chứa 3 con trỏ đối tượng `hocPhan`, `giangVien`, `phongHoc`. |
| 5 | Bảng `DANG_KY` | Lớp liên kết `DangKyMon` | RDBMS là bảng trung gian chứa 2 FK `ma_sv`, `ma_lhp` $\leftrightarrow$ OODBMS là thực thể liên kết hai chiều có tính toán điểm số. |
| 6 | Bảng `LICH_SU_CHUAN_HOA` (nếu có) | Lớp `LichSuChuanHoa` | RDBMS lưu dữ liệu dạng quan hệ $\leftrightarrow$ OODBMS lưu dạng Document/Object Graph linh hoạt. |

---

## V. KẾT LUẬN

1. **Tính tương thích:** Việc áp dụng CSDL Hướng đối tượng ObjectDB cho bài toán Quản lý Đào tạo Đại học đã giải quyết triệt để vấn đề trở ngại trở kháng, cho phép mô hình hóa tự nhiên và giữ trọn vẹn sức mạnh của lập trình hướng đối tượng.
2. **Hiệu năng & Khả năng mở rộng:** Với các thao tác điều hướng xuyên suốt nhiều lớp đối tượng (như kiểm tra điều kiện tiên quyết, đối soát lịch học, thống kê học lực), OODBMS cho tốc độ truy xuất nhanh hơn và mã nguồn ngắn gọn, dễ bảo trì hơn so với các câu truy vấn SQL JOIN phức tạp.
3. **Phối hợp hệ thống:** Phân hệ ObjectDB đóng vai trò hoàn hảo vừa là cơ sở dữ liệu nghiệp vụ đào tạo, vừa là kho lưu trữ lịch sử các lượt chạy thuật toán chuẩn hóa của cả nhóm, đáp ứng xuất sắc mọi tiêu chí đánh giá trong đồ án môn học.
