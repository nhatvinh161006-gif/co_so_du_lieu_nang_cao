# Hướng Dẫn Cài Đặt & Triển Khai ObjectDB Bằng Maven Trong Java (Né Lỗi 1013 & 1033)

Tài liệu này hướng dẫn chi tiết cách cấu hình Maven, khắc phục triệt để lỗi kết nối Cloudflare (**Error 1013** và **Error 1033 Tunnel Error**) của máy chủ ObjectDB, đồng thời triển khai trọn bộ 5 lớp Entity và chương trình tạo file CSDL `db/daotao.odb` theo chuẩn **Đề tài 10: Quản Lý Đào Tạo (Tuần 1)**.

---

## 1. Nguyên Nhân Gây Lỗi Cloudflare (1013 & 1033) & Cách Xử Lý

* **Lỗi Cloudflare 1013:** Khi truy cập trang web `objectdb.com/download` từ Việt Nam hoặc mạng trường, Cloudflare nhận diện lệch SNI/IP hoặc chặn IP theo vùng địa lý.
* **Lỗi Cloudflare 1033 (Cloudflare Tunnel error):** Khi Maven kết nối trực tiếp đến kho `https://m2.objectdb.com`, kết nối bị ngắt do Cloudflare Tunnel của server ObjectDB gặp sự cố tạm thời.

### 👉 2 Cách khắc phục nhanh nhất:
1. **Cách 1 (Khuyên dùng nhất - Lấy file trực tiếp từ lớp):**
   * Trong nhóm Zalo / Teams / MS Classroom của lớp môn **Cơ sở dữ liệu nâng cao**, hãy tải file `objectdb.jar` (hoặc giải nén từ file `objectdb-2.x.zip` do các bạn cùng lớp chia sẻ).
   * Copy file `objectdb.jar` vào thư mục dự án:
     ```
     db-normalization-tool/libs/objectdb.jar
     ```
2. **Cách 2 (Tải từ trang chủ bằng VPN):**
   * Bật **1.1.1.1 WARP** (tải miễn phí tại `1.1.1.1`) hoặc VPN bất kỳ (chuyển vùng US/Singapore).
   * Mở trình duyệt truy cập: `https://www.objectdb.com/download`
   * Nhấp tải gói **ObjectDB SDK 2.9.5** (file zip dung lượng ~7.8 MB).
   * Giải nén file zip, vào thư mục `bin/` lấy file `objectdb.jar` và copy vào thư mục `libs/` của dự án.

---

## 2. Cấu Trúc Dự Án (db-normalization-tool)

```text
db-normalization-tool/
├── libs/
│   ├── README.txt
│   └── objectdb.jar            <-- Đặt file objectdb.jar tại đây
├── src/
│   └── main/
│       └── java/
│           └── com/doan10/db_normalization_tool/
│               ├── entity/
│               │   ├── Nguoi.java          (Lớp cha - OID, Kế thừa JOINED)
│               │   ├── SinhVien.java       (Lớp con - Kế thừa, Tập hợp @OneToMany)
│               │   ├── GiangVien.java      (Lớp con - Kế thừa, Học vị & Khoa)
│               │   ├── HocPhan.java        (Môn học - OID)
│               │   └── DangKy.java         (Liên kết @ManyToOne, Điểm số)
│               └── daotao/
│                   └── Main.java           (Chương trình kết nối, lưu & truy vấn JPQL)
├── pom.xml
└── mvnw / mvnw.cmd
```

---

## 3. Cấu Hình File `pom.xml`

Trong file `pom.xml`, các dependency JPA đã được cấu hình sẵn:

```xml
<!-- JPA Specification API (javax.persistence) -->
<dependency>
    <groupId>javax.persistence</groupId>
    <artifactId>javax.persistence-api</artifactId>
    <version>2.2</version>
</dependency>

<!-- Khi đã copy file objectdb.jar vào thư mục libs/, bạn mở comment dependency bên dưới: -->
<dependency>
    <groupId>com.objectdb</groupId>
    <artifactId>objectdb</artifactId>
    <version>2.9.5</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/objectdb.jar</systemPath>
</dependency>
```

---

## 4. Chi Tiết Các Lớp Thực Thể (Entity)

Hệ thống được thiết kế đầy đủ **4 tiêu chí cốt lõi** theo yêu cầu môn học:
1. **OID (Object Identifier):** Sinh tự động bằng `@Id` và `@GeneratedValue` trên các bảng.
2. **Kế thừa (Inheritance):** `@Inheritance(strategy = InheritanceType.JOINED)` với lớp cha `Nguoi`, các lớp con `SinhVien`, `GiangVien`.
3. **Thuộc tính tập hợp (Collection):** `List<DangKy> dsDangKy` trong `SinhVien` với cấu hình `@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)`.
4. **Phương thức xử lý nghiệp vụ:** `themDangKy(DangKy dk)` trực tiếp bên trong lớp `SinhVien`.

---

## 5. Chạy Và Nghiệm Thu Sản Phẩm Tuần 1

### Bước 1: Kích hoạt thư viện ObjectDB
Sau khi thả file `objectdb.jar` vào thư mục `libs/`, mở file `pom.xml` và bỏ dấu comment `<!-- ... -->` của thẻ dependency `com.objectdb:objectdb`.

### Bước 2: Chạy file `Main.java`
* **Trong IntelliJ IDEA:** Nhấp chuột phải vào file `com.doan10.db_normalization_tool.daotao.Main.java` $\rightarrow$ chọn **Run 'Main.main()'**.
* **Bằng dòng lệnh Terminal:**
  ```powershell
  cd D:\CSDL_NC\co_so_du_lieu_nang_cao\db-normalization-tool
  .\mvnw.cmd compile
  java -cp "target/classes;libs/objectdb.jar" com.doan10.db_normalization_tool.daotao.Main
  ```

### Bước 3: Tiêu chuẩn nghiệm thu
1. Trong thư mục dự án tự động sinh ra thư mục `db/` chứa file CSDL hướng đối tượng **`db/daotao.odb`**.
2. Cửa sổ Console hiển thị thông báo kết nối thành công và in ra danh sách sinh viên, đăng ký học phần, giảng viên qua câu truy vấn JPQL.
