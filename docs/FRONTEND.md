# Frontend tuần 1 — Relatio

## Chạy giao diện

Mở `db-normalization-tool/src/main/resources/static/index.html` trực tiếp trong trình duyệt. Không cần npm, cài thư viện, build hay backend. Nút nạp mẫu, nhập/xuất file và tính minh họa đều chạy ở chế độ này. Nếu clipboard không khả dụng, nút sao chép sẽ tải kết quả thành file.

Khi chạy dự án Spring Boot, các tài nguyên này được phục vụ từ `/` (thường là `http://localhost:8080`). Frontend không thay đổi mã Java, Maven hoặc cấu hình backend hiện có.

## Phần đã làm

- Giao diện tiếng Việt, responsive: menu điều hướng, form U/F, chọn công cụ, form X/Y, khu vực kết quả và lời giải từng bước.
- Nạp dữ liệu thành viên 1: đúng 21 thuộc tính, 10 phụ thuộc; không thêm phụ thuộc tùy chọn.
- Nhập file UTF-8 theo định dạng U_F, bỏ dòng trống/comment, hỗ trợ BOM/CRLF. Giới hạn file nhập 1 MB.
- Kiểm tra tên thuộc tính, thuộc tính lặp, cú pháp phụ thuộc, thuộc tính ngoài U và phân biệt hoa thường. Hỗ trợ `->`/`→`, tập rỗng biểu diễn bằng `∅`, F rỗng.
- Xuất U/F, xóa form, sao chép/tải kết quả. Thay đổi dữ liệu hoặc công cụ sẽ xóa kết quả cũ để tránh hiểu nhầm.
- Minh họa bao đóng và suy diễn bằng JavaScript, ghi rõ chưa kết nối backend. Lời giải hiển thị từng phụ thuộc áp dụng, thuộc tính mới và bao đóng sau bước đó. Không tuyên bố siêu khóa là khóa ứng viên khi chưa kiểm tra tối tiểu.
- Trang bộ dữ liệu với EER gốc, giả định nghiệp vụ và khóa mong đợi; trang hướng dẫn sử dụng.
- Các chức năng nâng cao được liệt kê ở mục chờ tích hợp, không có kết quả giả.

Không lưu dữ liệu qua lần tải lại trang; người dùng xuất file để giữ dữ liệu. Không gửi dữ liệu lên dịch vụ ngoài. Đây là phần frontend trong repo Java của nhóm, chưa triển khai hosting.

## Cấu trúc

- `index.html`: bố cục và nội dung.
- `styles.css`: giao diện, breakpoint cho điện thoại/máy tính bảng/desktop; không tải font hoặc thư viện từ CDN.
- `core.js`: parser và thuật toán minh họa độc lập với DOM, có thể kiểm thử bằng Node.
- `app.js`: sự kiện, trạng thái form, nhập/xuất file và render an toàn bằng textContent.
- `U_F.txt`, `eer.jpg`: bản sao dữ liệu nguồn thành viên 1.

Kiểm tra: từ thư mục `db-normalization-tool`, chạy `node --test frontend-tests/core.test.cjs`. Bộ kiểm thử đối chiếu 7 bao đóng, 7 suy diễn trong file mong đợi, bước giải, dữ liệu lỗi và tính nhất quán của mẫu UI.

Kết quả kiểm tra ngày 23/10/2026: 18/18 test đạt; `node --check` đạt cho cả hai file JavaScript. Chưa xác minh trực quan hoặc thao tác end-to-end trong trình duyệt: Chrome headless trên môi trường hiện tại dừng vì GPU process không khởi động được. Cần kiểm tra hiển thị desktop/mobile và thao tác nhập/xuất bằng trình duyệt thông thường khi bàn giao nhóm.

## Đề xuất hợp đồng với backend — chưa được thống nhất

Đây là đề xuất trao đổi với thành viên 2, **không phải API hiện có**. Frontend chưa gọi endpoint nào. Cần xác nhận tên trường khi serialize lớp Java `PhuThuocHam` (getter `getVT/getVP`) bằng DTO rõ ràng, tránh suy đoán tên JSON.

Đề xuất `POST /api/analysis/closure` và `POST /api/analysis/implication`, JSON UTF-8:

```json
{
  "universe": ["MaSV", "TenSV", "MaLHP", "Diem"],
  "dependencies": [
    {"left": ["MaSV"], "right": ["TenSV"]},
    {"left": ["MaSV", "MaLHP"], "right": ["Diem"]}
  ],
  "x": ["MaSV"],
  "y": ["TenSV"]
}
```

`y` chỉ cần cho suy diễn. Backend ánh xạ `left/right` vào `vT/vP`. Mảng biểu diễn tập, giữ tên nhiều ký tự, phân biệt hoa thường; không nối thuộc tính thành chuỗi một ký tự. Mảng rỗng biểu diễn tập rỗng. Validate toàn bộ thuộc tính thuộc U ở cả frontend và backend.

Response đề xuất cho ví dụ suy diễn:

```json
{
  "closure": ["MaSV", "TenSV"],
  "implied": true,
  "steps": [
    {
      "dependencyIndex": 0,
      "left": ["MaSV"],
      "right": ["TenSV"],
      "added": ["TenSV"],
      "closure": ["MaSV", "TenSV"]
    }
  ]
}
```

`dependencyIndex` bắt đầu từ 0; steps chỉ chứa bước thực sự thêm thuộc tính, bước khởi tạo do UI hiển thị riêng. Kết quả bao đóng có thể bỏ `implied`. Backend trả HTTP 400 cùng `{ "message": "...", "field": "dependencies" }` khi dữ liệu sai. Khi tích hợp, thêm trạng thái đang tải/lỗi mạng, thay lời gọi `Relatio.closure` bằng adapter API, và chuyển nhãn chế độ sang đúng trạng thái đã kết nối. Không tự fallback sang demo nếu API lỗi.

Các công cụ tương đương, phủ tối tiểu, khóa, dạng chuẩn, phân rã và chase cần hợp đồng riêng trước khi triển khai. Không coi mốc kết quả thành viên 1 là response cho một lược đồ bất kỳ.
