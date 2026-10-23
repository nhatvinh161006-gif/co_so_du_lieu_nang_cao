# HƯỚNG DẪN CÁC ĐỀ TÀI ĐỒ ÁN CƠ SỞ DỮ LIỆU NÂNG CAO

---

## PHẦN 1: QUY ĐỊNH VÀ YÊU CẦU CHUNG CHO CÁC ĐỒ ÁN

### 1. Chuẩn đầu ra yêu cầu chung
Mỗi nhóm sinh viên thực hiện đồ án đều phải đáp ứng các chuẩn đầu ra yêu cầu chung sau đây:
* **Phân tích và thiết kế:** Mô tả nghiệp vụ, quy tắc dữ liệu, sơ đồ EER (Extended Entity-Relationship) có chuyên biệt hóa / tổng quát hóa, lược đồ quan hệ và từ điển dữ liệu.
* **Chuẩn hóa:** Xác định phụ thuộc hàm; trình bày bao đóng, khóa, phủ tối tiểu; giải thích dạng chuẩn 3NF / BCNF, chứng minh tính nối không mất mát thông tin và bảo toàn phụ thuộc hàm. Phân tích 4NF khi có phụ thuộc đa trị.
* **SQL nâng cao:** 10–12 truy vấn / nghiệp vụ phức tạp, truy vấn con, phép nối, kết gộp, ràng buộc, thủ tục/hàm, SQL động, truy vấn đệ quy, SQL nhúng.
* **CSDL hướng đối tượng (OODBMS):** Phân hệ 4–6 lớp, OID, kế thừa, tập hợp (List, Set, Bag, Array), quan hệ và phương thức; tối thiểu 5 truy vấn OQL.
* **Giao dịch (Transaction):** Kịch bản thành công, lỗi giữa chừng (rollback), và tranh chấp đồng thời giữa ít nhất hai phiên giao dịch.
* **Thực nghiệm & Kiểm thử:** Dữ liệu mẫu, script khởi tạo, test-case có thể chạy lại; phân biệt kết quả đo thực tế (EXPLAIN) với lý thuyết.

---

### 2. Gợi ý tổ chức nghiên cứu và Tiến độ thực hiện (12 tuần)
* **Quy mô nhóm:** 1–5 sinh viên / nhóm. Mỗi nhóm chọn 1 đề tài.
* **Tiến độ chi tiết theo tuần:**
  - Tuần 1–2: Chọn đề tài, xác định câu hỏi nghiên cứu, phạm vi, tài liệu, phân công.
  - Tuần 3: Nộp đặc tả nghiệp vụ, sơ đồ EER và các quy tắc/ràng buộc.
  - Tuần 4–5: Nộp phân tích phụ thuộc hàm, khóa, chuẩn hóa và lược đồ quan hệ.
  - Tuần 6–8: Hoàn thành CSDL quan hệ, SQL nâng cao, dữ liệu mẫu, kiểm thử.
  - Tuần 9–10: Hoàn thành thiết kế hướng đối tượng, truy vấn OQL và thực nghiệm.
  - Tuần 11: Hoàn thành kiểm thử Transaction, thực nghiệm so sánh và phân tích.
  - Tuần 12: Nộp báo cáo, slide, demo và bảo vệ đồ án.

### 3. Bộ sản phẩm nộp và Đánh giá
* **Bộ sản phẩm:** Báo cáo viết (20–30 trang), Mã nguồn/Script CSDL lên GitHub, Slide thuyết trình, Biên bản phân công & nhật ký nhóm.
* **Thang điểm đánh giá (10 điểm):**
  - Phân tích yêu cầu và thiết kế sơ đồ EER: 1,5
  - Phụ thuộc hàm, chuẩn hóa và thiết kế lược đồ quan hệ: 2,0
  - SQL nâng cao và thiết kế mức vật lý: 2,0
  - Thiết kế CSDL hướng đối tượng và truy vấn OQL: 1,5
  - Transaction, thực nghiệm và phân tích kết quả: 2,0
  - Báo cáo, khả năng tái lập kết quả và bảo vệ cá nhân: 1,0

### 4. Hướng dẫn về Công cụ Cài đặt
* **CSDL Quan hệ:** PostgreSQL (hoặc SQL Server).
* **CSDL Hướng đối tượng:** Phân biệt OQL lý thuyết với JPQL/JDOQL của công cụ triển khai (ObjectDB, ZODB, Object-Relational trên PostgreSQL). Nếu dùng ObjectDB, cần trình bày riêng OQL và bảng ánh xạ sang JPQL.

### 5. Tài liệu nền dùng chung
* Elmasri & Navathe, Fundamentals of Database Systems (Bản 7).
* Silberschatz, Korth, Sudarshan, Database System Concepts (Bản 7) - https://www.db-book.com/
* Connolly & Begg, Database Systems (Bản 6).
* Tài liệu PostgreSQL: https://www.postgresql.org/docs/current/
* Khóa CMU 15-445/645 Database Systems: https://15445.courses.cs.cmu.edu/
* Công cụ vẽ mô hình: diagrams.net, dbdiagram.io, MySQL Workbench.

---

## PHẦN 2: CHI TIẾT NỘI DUNG 15 ĐỀ TÀI ĐỒ ÁN

### ĐỒ ÁN 1: Thiết kế CSDL hệ thống đăng ký học phần và kiểm tra điều kiện tiên quyết
* **Mục tiêu:** Xây dựng CSDL phục vụ mở lớp, đăng ký/hủy học phần, kiểm tra điều kiện học tập và kiểm soát sĩ số.
* **Câu hỏi nghiên cứu:** Làm thế nào biểu diễn quan hệ tiên quyết nhiều cấp và bảo đảm lớp không vượt sĩ số khi nhiều sinh viên đăng ký đồng thời?
* **Phạm vi gợi ý:** Sinh viên, giảng viên, học phần, học kỳ, lớp học phần, lịch học, kết quả học tập, đăng ký, quan hệ tiên quyết.
* **Các bước nghiên cứu:**
  1. Xác định nghiệp vụ đăng ký và điều kiện.
  2. Xây dựng EER (Nguoi -> SinhVien, GiangVien; tự liên kết HocPhan).
  3. Thiết kế và chuẩn hóa dữ liệu đăng ký, lớp học phần, kết quả.
  4. SQL nâng cao: Truy vấn đệ quy tìm tiên quyết trực tiếp/gián tiếp, phát hiện chu trình.
  5. Transaction đăng ký nguyên tử, kiểm tra cơ chế khóa/mức cô lập.
  6. Thực nghiệm đồng thời & Mô hình đối tượng SinhVien, HocPhan, LopHocPhan, DangKy, OQL.
* **Minh chứng cần có:** Truy vấn đệ quy đúng; phát hiện chu trình; kịch bản đồng thời không vượt sĩ số.
* **Tài liệu tham khảo:** Database System Concepts, PostgreSQL WITH & Explicit Locking.

### ĐỒ ÁN 2: Thiết kế và đánh giá CSDL quản lý bán hàng, tồn kho tại nhiều kho
* **Mục tiêu:** Thiết kế CSDL quản lý đơn hàng, nhập/xuất/chuyển kho, bảo đảm số lượng tồn và trạng thái đơn hàng nhất quán.
* **Câu hỏi nghiên cứu:** Chuẩn hóa và tổ chức transaction ảnh hưởng như thế nào đến tính đúng đắn và hiệu quả nghiệp vụ bán hàng?
* **Phạm vi gợi ý:** Sản phẩm, danh mục, nhà cung cấp, khách hàng, kho, tồn kho, đơn hàng, chi tiết đơn, phiếu nhập/xuất/chuyển kho.
* **Các bước nghiên cứu:**
  1. Quy tắc tồn kho thực tế, giữ hàng, hủy đơn/hoàn kho.
  2. Phân tích dữ liệu dư thừa ban đầu và chỉ ra bất thường.
  3. Chuẩn hóa 3NF/BCNF; lưu giá bán thời điểm đặt riêng với giá hiện tại.
  4. SQL: Thủ tục đặt/hủy đơn, đệ quy danh mục, SQL động báo cáo.
  5. Transaction gộp tạo đơn/chi tiết/cập nhật tồn, thử rollback & mua đồng thời.
  6. Đánh giá chỉ mục & Phân hệ đối tượng DonHang-ChiTietDonHang-SanPham, OQL.
* **Minh chứng cần có:** Không tồn âm; rollback không để lại chi tiết thiếu; phân tích EXPLAIN.
* **Tài liệu tham khảo:** Elmasri & Navathe Ch14-15, 20-22; PostgreSQL PL/pgSQL & EXPLAIN.

### ĐỒ ÁN 3: Thiết kế CSDL thư viện học liệu đa phương tiện
* **Mục tiêu:** Quản lý thống nhất sách, luận văn, video bài giảng, tài liệu điện tử; nghiên cứu kế thừa và dữ liệu đa trị.
* **Câu hỏi nghiên cứu:** Cách biểu diễn các loại học liệu và thuộc tính đa trị ảnh hưởng thế nào đến dư thừa dữ liệu, truy vấn và mở rộng?
* **Phạm vi gợi ý:** Học liệu, tác giả, chủ đề, bản sao, tệp tài nguyên, bạn đọc, phiếu mượn, quyền truy cập.
* **Các bước nghiên cứu:**
  1. Khảo sát thuộc tính dùng chung và riêng.
  2. EER: Lớp cha HocLieu và các lớp con (rời nhau/chồng lấp).
  3. Phụ thuộc đa trị HocLieu-TacGia-ChuDe & phân rã 4NF.
  4. So sánh ánh xạ kế thừa (bảng chung vs bảng cha-con).
  5. SQL & Transaction mượn bản sao cuối.
  6. Mô hình đối tượng, kế thừa, tập hợp & OQL.
* **Minh chứng cần có:** Dữ liệu chứng minh dư thừa trước 4NF; nối khôi phục đúng; bảng so sánh ánh xạ kế thừa.
* **Tài liệu tham khảo:** Fundamentals of Database Systems Ch4, 12, 15; ObjectDB Guide.

### ĐỒ ÁN 4: Thiết kế CSDL đặt phòng và nghiên cứu kiểm soát giao dịch đồng thời
* **Mục tiêu:** Xây dựng CSDL đặt phòng theo khoảng thời gian và đánh giá các cách ngăn đặt trùng.
* **Câu hỏi nghiên cứu:** Cơ chế nào bảo đảm một phòng không có hai đặt chỗ hiệu lực chồng lấn khi nhiều người đặt đồng thời?
* **Phạm vi gợi ý:** Khách hàng, phòng, loại phòng, đặt chỗ, khách lưu trú, dịch vụ, thanh toán.
* **Các bước nghiên cứu:**
  1. Quy tắc khoảng lưu trú [ngày nhận, ngày trả).
  2. EER & Chuẩn hóa: Đặt chỗ vs lưu trú thực tế.
  3. SQL nghiệp vụ: Tìm phòng trống, đệ quy cơ cấu cơ sở-tòa-tầng.
  4. Kịch bản tranh chấp 2 phiên đặt trùng.
  5. So sánh giải pháp: Khóa bản ghi, SERIALIZABLE, exclusion constraints.
  6. Kiểm thử lỗi & Mô hình đối tượng Phong, DatCho, KhachHang, DichVu, OQL.
* **Minh chứng cần có:** Re-produce kịch bản đặt trùng; giải pháp ngăn lỗi; đo hiệu năng đồng thời.
* **Tài liệu tham khảo:** PostgreSQL Transaction Isolation, Locking, Exclusion Constraints.

### ĐỒ ÁN 5: So sánh CSDL quan hệ và CSDL hướng đối tượng trong quản lý cấu trúc sản phẩm
* **Mục tiêu:** Biểu diễn sản phẩm gồm nhiều cụm và linh kiện; so sánh hai mô hình CSDL trên cùng bài toán.
* **Câu hỏi nghiên cứu:** Với cấu trúc sản phẩm nhiều cấp, mô hình nào thuận tiện hơn cho biểu diễn, truy vấn và thay đổi cấu trúc?
* **Phạm vi gợi ý:** Sản phẩm, cụm lắp ráp, linh kiện cơ khí/điện tử, quan hệ cấu thành, số lượng, phiên bản.
* **Các bước nghiên cứu:**
  1. Ngữ nghĩa cấu trúc nhiều cấp, ngăn chu trình.
  2. Thiết kế mô hình quan hệ & chuẩn hóa.
  3. Thiết kế mô hình đối tượng ThanhPhan, CumLapRap, LinhKien.
  4. Truy vấn tương đương SQL & OQL (tính tổng linh kiện, chi phí).
  5. Hiện thực PostgreSQL & ObjectDB (JPQL/JDOQL).
  6. Thực nghiệm thay đổi độ sâu/số nút & đo thời gian.
* **Minh chứng cần có:** Bộ truy vấn đối chiếu cho cùng kết quả; bảng so sánh biểu diễn & hiệu năng.
* **Tài liệu tham khảo:** Fundamentals of Database Systems Ch12; PostgreSQL Recursive Queries; ObjectDB Guide.

### ĐỒ ÁN 6: Thiết kế CSDL quản lý bệnh viện bằng mô hình EER và chuẩn hóa
* **Bài toán:** Quản lý bệnh viện (bác sĩ, điều dưỡng, kỹ thuật viên, bệnh nhân nội/ngoại trú, lượt khám, đơn thuốc, xét nghiệm, giường bệnh, BHYT).
* **Yêu cầu chính:** Sơ đồ EER đầy đủ (disjoint/overlap, total/partial, thực thể yếu, mối kết hợp bậc 3, đa trị/phức hợp); Ánh xạ EER sang quan hệ (so sánh các phương án); Phụ thuộc hàm, khóa, BCNF, 4NF; Cài đặt PostgreSQL với ~10.000 bản ghi mẫu.
* **Tài liệu tham khảo:** Elmasri & Navathe Ch3-4, 9, 14-15; P. Chen (1976); W. Kent (5 Normal Forms).

### ĐỒ ÁN 7: CSDL thương mại điện tử và lập trình SQL nâng cao
* **Bài toán:** Sàn TMĐT (khách hàng, người bán, sản phẩm biến thể, danh mục phân cấp, giỏ hàng, đơn hàng, khuyến mãi, kho).
* **Yêu cầu chính:** Thiết kế & chuẩn hóa; 20+ truy vấn phức tạp (subquery, EXISTS, phép chia, window functions, EXPLAIN ANALYZE); Trigger, Stored Procedure, SQL động (chống SQL Injection), SQL nhúng (JDBC/ECPG), SQL đệ quy danh mục ().
* **Tài liệu tham khảo:** Elmasri & Navathe Ch7, 10; Silberschatz Ch5; OWASP SQLi Cheat Sheet.

### ĐỒ ÁN 8: Transaction và điều khiển đồng thời trong hệ thống đặt vé hoặc ngân hàng
* **Bài toán:** Đặt vé máy bay/xem phim (nhiều người tranh chấp 1 ghế) hoặc chuyển khoản ngân hàng.
* **Yêu cầu chính:** Lý thuyết Transaction (ACID, serializability, 2PL, MVCC, log); Thực nghiệm tái hiện dirty read, non-repeatable read, phantom, lost update, write skew ở các mức cô lập trên PostgreSQL & MySQL; Simulation ~100 concurrent users (, optimistic locking, SERIALIZABLE retry).
* **Tài liệu tham khảo:** Elmasri & Navathe Ch20-22; Berenson et al. (ANSI SQL Isolation); Hermitage suite; Ports & Grittner (SSI in Postgres).

### ĐỒ ÁN 9: Thiết kế CSDL hướng đối tượng cho hệ thống bảo tàng hoặc thư viện số
* **Bài toán:** Quản lý hiện vật & tài liệu số phức tạp ( -> , , ).
* **Yêu cầu chính:** UML Class Diagram & ODL chuẩn ODMG (lớp, kế thừa, set/list/bag, inverse relationship, extent); 10-15 truy vấn OQL; Cài đặt trên ObjectDB/ZODB hoặc Object-Relational PostgreSQL (, table inheritance, JSONB); So sánh với CSDL quan hệ thuần (impedance mismatch).
* **Tài liệu tham khảo:** Elmasri & Navathe Ch12; Atkinson et al. (OO Database Manifesto); PostgreSQL Inheritance docs.

### ĐỒ ÁN 10: Xây dựng công cụ hỗ trợ phân tích phụ thuộc hàm và chuẩn hóa, áp dụng cho CSDL quản lý đào tạo
* **Bài toán:** Phần mềm (Web/Desktop) tự động tính toán lý thuyết CSDL (bao đóng ^+$, phủ tối tiểu, tất cả khóa, dạng chuẩn 1NF-BCNF, phân rã 3NF/BCNF, kiểm tra lossless join bằng chase). Áp dụng vào CSDL Quản lý đào tạo đại học.
* **Tài liệu tham khảo:** Elmasri & Navathe Ch14-15; E.F. Codd (1970); W. Kent.

### ĐỒ ÁN 11: Thiết kế CSDL quản lý đề tài nghiên cứu khoa học và quá trình nghiệm thu
* **Phạm vi:** Đề tài, nhà nghiên cứu, thành viên, nhiệm vụ, tiến độ, sản phẩm khoa học, hội đồng, phiếu đánh giá.
* **Yêu cầu chính:** EER (vai trò theo từng đề tài/hội đồng); Phụ thuộc hàm & 3NF/BCNF; SQL nâng cao (đệ quy cây đơn vị); Transaction nghiệm thu nguyên tử; Mô hình đối tượng DeTai, NhiemVu, SanPhamKhoaHoc, OQL.

### ĐỒ ÁN 12: Thiết kế CSDL ngân hàng câu hỏi và tổ chức thi trực tuyến có quản lý phiên bản
* **Phạm vi:** Môn học, chủ đề, câu hỏi, phiên bản câu hỏi, đề thi, lượt thi, bài làm, kết quả.
* **Yêu cầu chính:** Quản lý phiên bản bất biến / snapshot; Transaction nộp bài đồng thời; Mô hình đối tượng CauHoi & các lớp con, OQL.

### ĐỒ ÁN 13: Thiết kế CSDL theo dõi vận chuyển và bàn giao bưu kiện
* **Phạm vi:** Bưu kiện, người gửi/nhận, bưu cục, điểm trung chuyển, chuyến vận chuyển, lần bàn giao, sự kiện.
* **Yêu cầu chính:** Lịch sử sự kiện & trạng thái; SQL đệ quy mạng tuyến; Transaction bàn giao nguyên tử; Mô hình đối tượng SuKienVanChuyen & OQL.

### ĐỒ ÁN 14: Thiết kế CSDL hồ sơ khám bệnh và kết quả xét nghiệm có lịch sử chỉnh sửa
* **Phạm vi:** Bệnh nhân, nhân viên y tế, lần khám, chỉ định, kết quả xét nghiệm, phiên bản chỉnh sửa.
* **Yêu cầu chính:** So sánh bảng chuyên biệt vs EAV; Transaction & quản lý phiên bản chỉnh sửa; Mô hình đối tượng KetQuaXetNghiem & OQL.

### ĐỒ ÁN 15: Thiết kế CSDL mạng cộng tác và thảo luận học thuật
* **Phạm vi:** Người dùng, nhóm, quan hệ theo dõi, bài đăng, chủ đề, bình luận nhiều cấp, tương tác.
* **Yêu cầu chính:** EER & 4NF (thuộc tính đa trị độc lập); Truy vấn đệ quy cây bình luận & mạng quan hệ; Transaction tương tác; Mô hình đối tượng BaiDang, BinhLuan & OQL.
