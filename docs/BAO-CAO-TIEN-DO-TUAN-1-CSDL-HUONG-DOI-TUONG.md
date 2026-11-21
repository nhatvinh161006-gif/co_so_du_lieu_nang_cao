# BÁO CÁO TIẾN ĐỘ TUẦN 1 - PHÂN HỆ CSDL HƯỚNG ĐỐI TƯỢNG (OBJECTDB & JAVA JPA)

* **Học phần:** Cơ sở dữ liệu nâng cao
* **Đề tài 10:** Xây dựng công cụ hỗ trợ phân tích phụ thuộc hàm và chuẩn hóa, áp dụng cho CSDL quản lý đào tạo
* **Phân hệ phụ trách:** Cơ sở dữ liệu Hướng đối tượng (OODBMS)

---

## 1. Đánh Giá Tiến Độ Tuần 1 So Với Tài Liệu Hướng Dẫn Môn Học

Căn cứ theo tài liệu hướng dẫn chính thức của giảng viên (`goi-sv-huong-dan-cac-de-tai-csdl-nang-cao.md`):
* **Tiến độ chung toàn khóa:** Tuần 1–2 yêu cầu xác định đề tài, phạm vi, phân công và cài đặt môi trường.
* **Nhiệm vụ riêng của mảng CSDL Hướng đối tượng:** 
  1. Cài đặt thành công công cụ OODBMS (ObjectDB) và kết nối với dự án Java $\rightarrow$ **ĐÃ HOÀN THÀNH 100%**.
  2. Trích xuất phân hệ đối tượng từ sơ đồ EER (quy mô 4–6 lớp) $\rightarrow$ **ĐÃ HOÀN THÀNH 100%**.
  3. Lập trình các lớp Entity thể hiện rõ 4 thành phần core: OID, Kế thừa, Thuộc tính tập hợp và Phương thức xử lý $\rightarrow$ **ĐÃ HOÀN THÀNH 100%**.
  4. Thực thi khởi tạo CSDL mẫu và kiểm tra kết nối sinh ra file `.odb` $\rightarrow$ **ĐÃ HOÀN THÀNH 100%**.

---

## 2. Trích Xuất Phân Hệ Đối Tượng Từ Sơ Đồ EER

### 2.1. Phân tích sơ đồ EER tổng thể của Đề tài 10
Sơ đồ EER của hệ thống gồm 8 thực thể: `KHOA`, `NGANH`, `NGUOI` (chuyên biệt hóa thành `SINHVIEN`, `GIANGVIEN`), `HOCPHAN`, `PHONGHOC`, `LOPHOCPHAN` và mối kết hợp nhiều - nhiều `DANG_KY`.

### 2.2. Lý do trích xuất phân hệ hạt nhân (5 lớp core)
Quy định chuẩn đầu ra của môn học yêu cầu phân hệ hướng đối tượng có từ **4–6 lớp** để tập trung làm rõ tính ưu việt của mô hình hướng đối tượng so với mô hình quan hệ bảng phẳng. Do đó, nhóm trích xuất phân hệ hạt nhân **"Quản lý đào tạo & Đăng ký học phần"** gồm 5 lớp thực thể chính:

| STT | Thực thể trên sơ đồ EER | Lớp Entity Java OODBMS | Vai trò & Đặc điểm |
| :---: | :--- | :--- | :--- |
| 1 | `NGUOI` | `Nguoi` | **Lớp cha**: Chứa OID tự sinh, `hoTen`, `ngaySinh`. Kế thừa `@Inheritance(JOINED)`. |
| 2 | `SINHVIEN` | `SinhVien` | **Lớp con**: Kế thừa `Nguoi`, thêm `maSV`, tập hợp đăng ký và các hàm nghiệp vụ tính điểm, xếp loại. |
| 3 | `GIANGVIEN` | `GiangVien` | **Lớp con**: Kế thừa `Nguoi`, thêm `maGV`, `hocVi` và danh sách các lớp học phần được phân công giảng dạy. |
| 4 | `HOCPHAN` | `HocPhan` | **Thực thể môn học**: OID, `maHP`, `tenHP`, `soTC`. |
| 5 | `LOPHOCPHAN` | `LopHocPhan` | **Thực thể lớp học**: OID, `maLHP`, `hocKy`, `thu`, `tietBD`, liên kết `@ManyToOne` với `HocPhan` và `GiangVien`. |
| * | `DANG_KY` | `DangKy` | **Lớp liên kết**: Kết nối `SinhVien` với `LopHocPhan`, lưu trữ `diem` và quy đổi điểm chữ. |

---

## 3. Thuyết Minh 4 Thành Phần Cốt Lõi (Core Components) Trong Mã Nguồn

### 3.1. Định danh đối tượng (Object Identifier - OID)
Trong CSDL Hướng đối tượng, mỗi đối tượng có một định danh duy nhất (OID) bất biến, độc lập với giá trị các thuộc tính nghiệp vụ.
* **Cài đặt JPA:** Sử dụng annotation `@Id` và `@GeneratedValue` để ObjectDB tự động quản lý OID cho từng đối tượng trong file dữ liệu.
```java
@Id
@GeneratedValue
private Long id; // OID định danh duy nhất
```

### 3.2. Quan hệ kế thừa (Inheritance)
Khác với CSDL quan hệ phải chuẩn hóa thành các bảng nối khóa ngoại phức tạp, OODBMS hỗ trợ kế thừa tự nhiên:
* Lớp cha `Nguoi` được định nghĩa `@Inheritance(strategy = InheritanceType.JOINED)`.
* Các lớp con `SinhVien` và `GiangVien` sử dụng từ khóa `extends Nguoi` kế thừa toàn bộ thuộc tính và định danh của lớp cha.
```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Nguoi implements Serializable { ... }

@Entity
public class SinhVien extends Nguoi { ... }

@Entity
public class GiangVien extends Nguoi { ... }
```

### 3.3. Thuộc tính tập hợp (Collection: List / Set)
OODBMS cho phép một thuộc tính chứa trực tiếp một danh sách/tập hợp các đối tượng liên kết mà không cần tạo bảng trung gian nhân tạo:
* Trong `SinhVien`: Sử dụng `@OneToMany` với `List<DangKy> dsDangKy` để chứa tất cả các học phần sinh viên đã đăng ký.
* Trong `GiangVien`: Sử dụng `@OneToMany` với `List<LopHocPhan> dsLopGiangDay` để chứa danh sách lớp phụ trách.
```java
@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<DangKy> dsDangKy = new ArrayList<>();
```

### 3.4. Phương thức xử lý nghiệp vụ (Business Methods - Đóng gói hành vi)
Đây là **đặc trưng quan trọng nhất** phân biệt OODBMS với RDBMS:
* Trong CSDL quan hệ: Các bảng chỉ là cấu trúc dữ liệu thụ động, mọi xử lý logic phải viết ở tầng ứng dụng hoặc Trigger/Stored Procedure.
* Trong CSDL hướng đối tượng: Đối tượng vừa lưu trữ dữ liệu, vừa **đóng gói sẵn các phương thức nghiệp vụ** ngay bên trong Entity:
  1. `SinhVien.kiemTraTrungLich(LopHocPhan lopMoi)`: Duyệt danh sách các lớp đã đăng ký, đối chiếu Thứ và Tiết bắt đầu để ngăn chặn việc sinh viên đăng ký trùng giờ.
  2. `SinhVien.tinhDiemTrungBinh()`: Tự động tính Điểm trung bình tích lũy theo trọng số số tín chỉ của từng học phần.
  3. `SinhVien.tongTinChiTichLuy()`: Tự động cộng dồn số tín chỉ của các môn có điểm tổng kết $\ge 4.0$ (đạt).
  4. `SinhVien.xepLoaiHocLuc()`: Tự động xếp loại học lực (Xuất sắc, Giỏi, Khá, Trung bình, Yếu) dựa trên ĐTB tích lũy.
  5. `DangKy.getDiemChu()`: Tự động quy đổi điểm hệ 10 sang điểm chữ (A, B, C, D, F).

---

## 4. Minh Chứng Thực Nghiệm & Kết Quả Chạy Chương Trình

Khi chạy chương trình [`Main.java`](file:///d:/CSDL_NC/co_so_du_lieu_nang_cao/db-normalization-tool/src/main/java/com/doan10/db_normalization_tool/daotao/Main.java), hệ thống tự động:
1. Tạo file CSDL hướng đối tượng: **`db/daotao.odb`** (dung lượng 262 KB).
2. Lưu trữ đầy đủ dữ liệu mẫu và in kết quả nghiệm thu ra Console:

```text
======================================================================
  KHỞI TẠO VÀ THỰC THI CSDL HƯỚNG ĐỐI TƯỢNG (OBJECTDB) - ĐỀ TÀI 10
======================================================================
>>> ĐÃ LƯU THÀNH CÔNG VÀO FILE: db/daotao.odb

----------------------------------------------------------------------
>>> KIỂM THỬ PHƯƠNG THỨC XỬ LÝ NGHIỆP VỤ TRONG ENTITY:
----------------------------------------------------------------------
1. Sinh viên SV001 đăng ký thử môn mới:
   + Lớp LHP03 (Thứ 2, Tiết 1 (HK1-2026)) -> Trùng lịch: true (Đã chặn trùng lịch)
   + Lớp LHP04 (Thứ 5, Tiết 4 (HK1-2026)) -> Trùng lịch: false (Hợp lệ để đăng ký)

2. Tính toán kết quả học tập của Sinh viên SV001:
   + Tổng tín chỉ tích lũy: 7 TC
   + Điểm trung bình tích lũy: 8.79 / 10
   + Xếp loại học lực: Giỏi

----------------------------------------------------------------------
>>> TRUY VẤN DỮ LIỆU ĐỐI TƯỢNG BẰNG JPQL (OQL MAPPING):
----------------------------------------------------------------------

[Sinh Viên] Mã SV: SV001 | Họ Tên: Nguyen Van A | Ngày Sinh: 2004-11-15
  -> ĐTB: 8.79 | Học Lực: Giỏi | Tích Lũy: 7 TC
     * Lớp: LHP01 - Môn: Cơ Sở Dữ Liệu Nâng Cao (3 TC) | Điểm: 8.5 (A) | GV: TS. Tran Van B
     * Lớp: LHP02 - Môn: Công Nghệ Phần Mềm (4 TC) | Điểm: 9.0 (A) | GV: ThS. Le Thi C

[Sinh Viên] Mã SV: SV002 | Họ Tên: Tran Thi Hoa | Ngày Sinh: 2004-03-22
  -> ĐTB: 7.0 | Học Lực: Khá | Tích Lũy: 3 TC
     * Lớp: LHP01 - Môn: Cơ Sở Dữ Liệu Nâng Cao (3 TC) | Điểm: 7.0 (B) | GV: TS. Tran Van B

[Giảng Viên]
  + Mã GV: GV001 | Họ Tên: TS. Tran Van B | Học Vị: Tiến sĩ | Đang phụ trách: 1 lớp
  + Mã GV: GV002 | Họ Tên: ThS. Le Thi C | Học Vị: Thạc sĩ | Đang phụ trách: 1 lớp

======================================================================
>>> KẾT LUẬN: NGHIỆM THU TUẦN 1 CSDL HƯỚNG ĐỐI TƯỢNG ĐÃ THÀNH CÔNG 100%!
======================================================================
```

---

## 5. Kết Luận
Phần việc phụ trách CSDL Hướng đối tượng của bạn trong **Tuần 1 đã hoàn thành xuất sắc 100%**, vượt tiến độ so với kế hoạch chung của đề tài (vừa có sơ đồ trích xuất, vừa có mã nguồn JPA đầy đủ 4 tiêu chí cốt lõi, vừa tạo lập và truy vấn thành công file CSDL thực tế).
