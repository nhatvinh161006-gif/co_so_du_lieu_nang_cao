# Frontend Relatio

## Chạy giao diện đã kết nối backend

Mở terminal tại `db-normalization-tool` và chạy `.\mvnw.cmd spring-boot:run`
bằng JDK 25. Mở `http://localhost:8080`; các thao tác phân tích gọi REST API
Java đang chạy trên cùng origin. Không mở `index.html` bằng `file://` để tính
kết quả: frontend không chuyển ngầm sang thuật toán demo khi backend không
khả dụng.

## Công cụ và dữ liệu

- Nhập U/F từ form hoặc file UTF-8 theo định dạng `U: ...` và `F:`; hỗ trợ
  comment `#`, BOM, CRLF, `->`/`→`, tên thuộc tính nhiều ký tự.
- Bao đóng và suy diễn gọi `/api/algorithms/closure` và
  `/api/algorithms/implication`.
- Tìm khóa, phủ tối thiểu, kiểm tra dạng chuẩn và phân rã 3NF gọi lần lượt
  `/api/algorithms/keys`, `/minimal-cover`, `/check-nf`, `/decompose-3nf`.
- Tất cả kết quả đều hiển thị danh sách bước do backend trả về. Lỗi API hoặc
  validation được báo rõ; không fallback sang kết quả JavaScript.
- Mẫu quản lý đào tạo gồm 21 thuộc tính và 10 phụ thuộc hàm chính; không thêm
  phụ thuộc tùy chọn `MaSV, MaHP, HocKy -> MaLHP`.

Hợp đồng JSON, ví dụ response và lỗi HTTP nằm trong
[ALGORITHM_API.md](./ALGORITHM_API.md). Trang giao diện, stylesheet và parser
nằm trong `db-normalization-tool/src/main/resources/static`.

## Kiểm thử frontend

Từ thư mục `db-normalization-tool`, chạy:

```powershell
node --test frontend-tests/core.test.cjs
node --check src/main/resources/static/app.js
node --check src/main/resources/static/core.js
```

Kiểm thử Java:

```powershell
.\mvnw.cmd test
```
