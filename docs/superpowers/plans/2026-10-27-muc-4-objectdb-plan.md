# Kế Hoạch Triển Khai Mục 4: CSDL Hướng Đối Tượng (ObjectDB) - Đề Tài 10

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Hoàn thiện 100% nhiệm vụ Người 4 (Mục 4 - CSDL Hướng đối tượng ObjectDB) trong Đồ án 10 "Hệ thống Quản lý Đào tạo Đại học & Công cụ Chuẩn hóa CSDL", bao gồm mô hình Entity, bộ 5 truy vấn OQL/JPQL, transaction, sườn lưu trữ lịch sử thuật toán chuẩn hóa tích hợp với Người 2 & 3, và tài liệu kỹ thuật.

**Architecture:** Sử dụng kiến trúc hướng đối tượng thuần trên nền ObjectDB (JPA 2.0). Dữ liệu được lưu trữ trực tiếp dưới dạng Object Graph Persistence vào file CSDL nhúng `daotao.odb` thông qua `EntityManager`. Tích hợp REST API controller bằng Spring Boot để cung cấp endpoint lưu trữ và truy vấn lịch sử chuẩn hóa cho module thuật toán của Người 2 và frontend của Người 3.

**Tech Stack:** Java 17, ObjectDB 2.9.5, JPA (Jakarta Persistence API / `javax.persistence`), Spring Boot 3.x, Maven Wrapper.

**Spec:** 
- [Công việc.docx](file:///d:/CSDL_NC/Công việc.docx)
- [goi-sv-huong-dan-cac-de-tai-csdl-nang-cao.md](file:///d:/CSDL_NC/goi-sv-huong-dan-cac-de-tai-csdl-nang-cao.md)

## Global Constraints

- Java Version: Java 17 LTS.
- Database: ObjectDB 2.9.5 (`libs/objectdb.jar` cài đặt trong `.m2`).
- Framework JPA: Các annotation chuẩn `@Entity`, `@Id`, `@GeneratedValue`, `@Transient`, `@Lob`.
- Đường dẫn file CSDL: `db/daotao.odb`.
- Mã nguồn đặt trong package: `com.doan10.db_normalization_tool`.

## Review Focus

1. Không dùng Foreign Key dạng `long` hay `String` cho các quan hệ đối tượng: Phải dùng con trỏ đối tượng trực tiếp (`phongHoc`, `giangVien`, `hocPhan`).
2. Quan hệ hai chiều (Bidirectional) phải đồng bộ dữ liệu: Khi add một `DangKy` vào `LopHocPhan`, `dsDangKy` và `sinhVien` phải nhất quán.
3. Cú pháp JPQL của ObjectDB với thuộc tính tập hợp: Sử dụng toán tử `SIZE(...)` hoặc `IS NOT EMPTY` thay vì SQL join table.
4. Xử lý Transaction Rollback: Khi xảy ra exception trong quá trình persist, transaction phải được rollback an toàn mà không làm hỏng file CSDL `.odb`.
5. Độc lập triển khai: Module lịch sử chuẩn hóa (`LichSuChuanHoa`) có sẵn REST API độc lập để khi Người 2 và Người 3 gửi code sang thì kết nối ngay mà không cần sửa core logic.

---

### Task 1: Nâng cấp và Hoàn thiện Mô hình Entity Hướng Đối Tượng

**Files:**
- Create: `src/main/java/com/doan10/db_normalization_tool/entity/PhongHoc.java`
- Modify: `src/main/java/com/doan10/db_normalization_tool/entity/Nguoi.java`
- Modify: `src/main/java/com/doan10/db_normalization_tool/entity/DangKy.java`
- Modify: `src/main/java/com/doan10/db_normalization_tool/entity/LopHocPhan.java`
- Modify: `src/main/java/com/doan10/db_normalization_tool/entity/HocPhan.java`

**Interfaces:**
- Consumes: Cấu trúc lớp hiện có từ Tuần 1.
- Produces: 
  - `PhongHoc`: `oid`, `maPhong`, `toaNha`, `sucChua`.
  - `Nguoi`: thuộc tính `gioiTinh`.
  - `DangKy`: thuộc tính `diemChuyenCan`, `diemGiuaKy`, `diemCuoiKy`, phương thức `tinhDiemTongKet()`.
  - `LopHocPhan`: tham chiếu `PhongHoc phongHoc`, tập hợp `List<DangKy> dsDangKy`.
  - `HocPhan`: tập hợp `List<LopHocPhan> dsLopHocPhan`.

- [ ] **Step 1: Tạo mới Entity `PhongHoc.java`**
  - Chứa `@Entity`, `@Id @GeneratedValue private long oid`, các trường `maPhong`, `toaNha`, `sucChua`.

- [ ] **Step 2: Thêm thuộc tính `gioiTinh` vào `Nguoi.java`**
  - Thêm getter/setter và cập nhật constructor của `Nguoi`, `SinhVien`, `GiangVien`.

- [ ] **Step 3: Cập nhật `DangKy.java` với 3 đầu điểm và hàm tính điểm tổng kết**
  - Thêm `diemChuyenCan`, `diemGiuaKy`, `diemCuoiKy`.
  - Hàm `tinhDiemTongKet()`: $0.1 \times \text{CC} + 0.3 \times \text{GK} + 0.6 \times \text{CK}$.

- [ ] **Step 4: Cập nhật `LopHocPhan.java` và `HocPhan.java` với quan hệ đối tượng & tập hợp 2 chiều**
  - Gán `private PhongHoc phongHoc;` và `List<DangKy> dsDangKy = new ArrayList<>()`.
  - Trong `HocPhan`: thêm `List<LopHocPhan> dsLopHocPhan = new ArrayList<>()`.

- [ ] **Step 5: Kiểm tra biên dịch**
  - Chạy `.\mvnw.cmd compile` và xác nhận build thành công.

---

### Task 2: Cập nhật Dữ liệu Mẫu & Kiểm thử Transaction ACID trên ObjectDB

**Files:**
- Modify: `src/main/java/com/doan10/db_normalization_tool/daotao/Main.java`

**Interfaces:**
- Consumes: Các Entity đã hoàn thiện từ Task 1.
- Produces: File CSDL `db/daotao.odb` có đầy đủ `PhongHoc`, sinh viên, giảng viên, điểm 3 thành phần, cùng kịch bản kiểm thử Rollback.

- [ ] **Step 1: Viết logic sinh dữ liệu mẫu đầy đủ trong `Main.java`**
  - Tạo 3 phòng học (`A101`, `A102`, `B201`).
  - Gán phòng học cho các `LopHocPhan`.
  - Nhập 3 đầu điểm cho các sinh viên trong `DangKy`.

- [ ] **Step 2: Viết kịch bản kiểm thử Transaction Rollback**
  - Mở transaction, thêm đối tượng hợp lệ, cố ý ném `RuntimeException` hoặc insert lỗi, gọi `rollback()`.
  - Kiểm tra số lượng bản ghi trước và sau khi rollback để chứng minh tính nguyên tử (Atomicity).

- [ ] **Step 3: Chạy thực thi `Main.java`**
  - Chạy `.\mvnw.cmd exec:java "-Dexec.mainClass=com.doan10.db_normalization_tool.daotao.Main"`
  - Xác nhận console hiển thị commit thành công và rollback thành công.

---

### Task 3: Bộ 4 Truy Vấn OQL / JPQL Bắt Buộc & Bảng Đối Chiếu Cú Pháp

**Files:**
- Create: `src/main/java/com/doan10/db_normalization_tool/daotao/ObjectDbQueryDemo.java`
- Create: `docs/BANG-DOI-CHIEU-OQL-JPQL.md`

**Interfaces:**
- Consumes: CSDL `daotao.odb` đã có dữ liệu.
- Produces: 4 phương thức truy vấn JPQL mẫu thực thi và in kết quả ra console; 1 bảng so sánh cú pháp ODMG OQL $\leftrightarrow$ ObjectDB JPQL.

- [ ] **Step 1: Viết 4 truy vấn trong `ObjectDbQueryDemo.java`**
  - Query 1: Đa hình trên `Nguoi` (`SELECT p FROM Nguoi p WHERE p.gioiTinh = 'Nữ'`).
  - Query 2: Path Navigation (`SELECT d.sinhVien.hoTen, d.lopHocPhan.phongHoc.maPhong FROM DangKy d WHERE d.lopHocPhan.phongHoc.toaNha = 'A'`).
  - Query 3: Thao tác tập hợp (`SELECT l FROM LopHocPhan l WHERE SIZE(l.dsDangKy) >= 2`).
  - Query 4: Lọc nâng cao theo điểm tổng kết môn học cụ thể.

- [ ] **Step 2: Viết bảng đối chiếu OQL $\leftrightarrow$ JPQL trong `BANG-DOI-CHIEU-OQL-JPQL.md`**
  - Trình bày đối sánh cú pháp chi tiết giữa ODMG 3.0 OQL và JPQL ObjectDB cho cả 4 truy vấn.

- [ ] **Step 3: Thực thi kiểm thử bộ truy vấn**
  - Chạy `ObjectDbQueryDemo` và xác nhận kết quả trả về đúng dữ liệu thực tế.

---

### Task 4: Xây dựng Module Lưu Trữ Lịch Sử Chuẩn Hóa (Sườn Tích Hợp Người 2 & 3)

**Files:**
- Create: `src/main/java/com/doan10/db_normalization_tool/entity/LichSuChuanHoa.java`
- Create: `src/main/java/com/doan10/db_normalization_tool/service/LichSuChuanHoaService.java`
- Create: `src/main/java/com/doan10/db_normalization_tool/controller/LichSuChuanHoaController.java`
- Create: `docs/HUONG-DAN-TICH-HOP-API-LICHSU.md`

**Interfaces:**
- Consumes: ObjectDB `EntityManagerFactory`.
- Produces: 
  - `LichSuChuanHoa`: OID, thời gian chạy, tập thuộc tính $U$, tập phụ thuộc hàm $F$, loại thuật toán, chi tiết các bước giải thuật.
  - REST endpoints: `POST /api/history`, `GET /api/history`, `GET /api/history/{id}`.

- [ ] **Step 1: Tạo Entity `LichSuChuanHoa.java`**
  - Đánh dấu `@Entity`, `@Lob private String ketQuaChiTiet`.

- [ ] **Step 2: Viết `LichSuChuanHoaService.java`**
  - Thực hiện lưu và lấy danh sách lịch sử thông qua transaction của ObjectDB.

- [ ] **Step 3: Viết REST Controller `LichSuChuanHoaController.java`**
  - Cung cấp các endpoint REST JSON cho Người 2 (Backend) và Người 3 (Frontend).

- [ ] **Step 4: Viết tài liệu tích hợp `HUONG-DAN-TICH-HOP-API-LICHSU.md`**
  - Hướng dẫn định dạng JSON gửi lên và dữ liệu trả về để Người 2 và Người 3 chỉ cần đọc là tích hợp được ngay.

---

### Task 5: Tài Liệu Kỹ Thuật, Sơ Đồ Lớp & Báo Cáo So Sánh

**Files:**
- Create: `docs/SO-DO-CLASS-DIAGRAM.md`
- Create: `docs/BAO-CAO-SO-SANH-CSDL-DOI-TUONG-VA-QUAN-HE.md`

**Interfaces:**
- Consumes: Thiết kế hoàn chỉnh của các Entity và kiến thức CSDL nâng cao.
- Produces: Sơ đồ UML trực quan bằng Mermaid; Báo cáo so sánh lý thuyết và thực tiễn giữa OODBMS và RDBMS.

- [ ] **Step 1: Dựng sơ đồ UML Class Diagram bằng Mermaid trong `docs/SO-DO-CLASS-DIAGRAM.md`**
  - Thể hiện rõ các thuộc tính, phương thức, OID, quan hệ kế thừa và quan hệ kết tập.

- [ ] **Step 2: Soạn thảo Báo cáo So sánh OODBMS vs RDBMS trong `docs/BAO-CAO-SO-SANH-CSDL-DOI-TUONG-VA-QUAN-HE.md`**
  - So sánh chi tiết: OID vs Khóa chính/Khóa ngoại; Path Navigation vs SQL JOIN; Giải quyết trở ngại trở kháng (Impedance Mismatch).

- [ ] **Step 3: Commit toàn bộ công việc lên nhánh git `vinh`**
  - Chạy `git add .`, `git commit`, `git push origin vinh`.
