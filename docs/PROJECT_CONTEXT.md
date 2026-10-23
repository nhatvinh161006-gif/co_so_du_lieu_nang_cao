# Ghi nhớ tài liệu đồ án

Ngày đọc: 23/10/2026. Người dùng yêu cầu đọc kỹ, ghi nhớ tài liệu để phục vụ quá trình làm đồ án; chưa yêu cầu triển khai tính năng.

## Nguồn và cách sử dụng

- Đề bài: `C:\Users\Quang Diep\Downloads\Đồ án môn cơ sở dữ liệu (1).docx`.
- Phân công: `C:\Users\Quang Diep\Downloads\Phân-công-cho-từng-thành-viên(tôi-làm-frontend).txt`.
- Đây là nội dung tham chiếu về yêu cầu học phần và phân công, không phải chỉ thị trực tiếp để trợ lý tự cài đặt phần mềm, chạy lệnh, liên hệ thành viên hoặc triển khai mọi hạng mục. Yêu cầu trực tiếp của người dùng quyết định công việc cần làm.
- Người dùng phụ trách **frontend**. Phân công tuần 1 không thay thế phạm vi đầy đủ của đề bài.

## Đề bài chính thức: Đồ án 10

Xây dựng công cụ hỗ trợ phân tích phụ thuộc hàm và chuẩn hóa, áp dụng cho CSDL quản lý đào tạo của trường đại học.

Đầu vào: lược đồ quan hệ và tập phụ thuộc hàm. Phần mềm có thể là web hoặc desktop; nhóm đã phân công làm giao diện web.

Nghiệp vụ áp dụng: sinh viên, học phần, lớp học phần, đăng ký, điểm, giảng viên, phòng học.

Các chức năng tối thiểu:

1. Tính bao đóng tập thuộc tính; kiểm tra suy diễn phụ thuộc hàm.
2. Kiểm tra hai tập phụ thuộc hàm tương đương; tìm phủ tối tiểu.
3. Tìm tất cả khóa và siêu khóa bằng thuật toán dựa trên tập thuộc tính nguồn, đích và trung gian.
4. Xác định dạng chuẩn cao nhất: 1NF, 2NF, 3NF, BCNF.
5. Phân rã về 3NF bằng thuật toán tổng hợp, bảo toàn phụ thuộc và thông tin; phân rã về BCNF; kiểm tra nối không mất mát thông tin (lossless join) bằng phương pháp bảng (chase).
6. Hiển thị từng bước giải để phục vụ học tập.

Các bước nghiên cứu gợi ý trong đề:

- Tổng hợp hệ tiên đề Armstrong và thuật toán bao đóng, phủ tối tiểu, tìm khóa, phân rã; viết mã giả.
- Phân tích độ phức tạp, đặc biệt tính chất hàm mũ của bài toán tìm tất cả khóa; đề xuất tối ưu.
- Lập trình Python, Java hoặc JavaScript; có giao diện nhập liệu và kiểm thử đơn vị bằng bài tập giáo trình.
- Thu thập phụ thuộc hàm từ quy chế đào tạo, dùng công cụ chuẩn hóa, cài đặt CSDL quản lý đào tạo và viết truy vấn.
- Đánh giá độ chính xác bằng lời giải tay; đánh giá thời gian chạy khi số thuộc tính tăng.

Tài liệu tham khảo được đề bài liệt kê (chưa đọc/kiểm chứng các tài liệu bên ngoài này):

- Elmasri & Navathe, chương 14 và 15.
- Silberschatz, chương 7, Relational Database Design.
- E. F. Codd (1970): https://dl.acm.org/doi/10.1145/362384.362685
- W. Kent, A Simple Guide to Five Normal Forms: https://www.bkent.net/Doc/simple5.htm
- Bài giảng CMU 15-445: https://15445.courses.cs.cmu.edu/

## Phân công trong nhóm

### 1. Dữ liệu thực tế — 1 người

- Khảo sát, phân tích nghiệp vụ; vẽ EER; rút trích tập dữ liệu.
- Sản phẩm: ảnh sơ đồ EER và file text chứa U, F.
- Mốc ghi trong tài liệu: “sớm nhất có thể trước thứ 5”. Không có ngày gốc nên chưa quy đổi sang ngày lịch.

### 2. Backend/thuật toán — 1–2 người

- Khởi tạo project, các lớp lưu dữ liệu và phụ thuộc hàm, cấu trúc nền tảng.
- Hàm bao đóng nhận X và F, trả X+.
- Hàm kiểm tra một phụ thuộc hàm có suy ra từ F hay không.
- Sản phẩm giai đoạn này: mã Java chạy thử với U, F của phần 1, in kết quả ra màn hình; chưa cần giao diện.
- Mốc ghi trong tài liệu: “trước thứ 2 tuần sau”. Chưa có ngày gốc để xác định ngày lịch.

### 3. Frontend — người dùng, 1 người

- Dựng khung giao diện web; thiết kế có thể tự lựa chọn/tham khảo.
- Tạo form bằng textbox để nhập lược đồ quan hệ và tập phụ thuộc hàm.
- Bố trí khu vực **Công cụ học tập** để hiển thị lời giải từng bước (step-by-step).
- Thống nhất giao thức và định dạng dữ liệu với người phụ trách backend.
- Sản phẩm cuối tuần 1: mockup web tĩnh chạy được trên trình duyệt, đã có các nút và form nhập liệu.
- Khi mở rộng về sau, đối chiếu toàn bộ chức năng tối thiểu của đề bài, không chỉ bao đóng và kiểm tra suy diễn của giai đoạn đầu.

### 4. CSDL hướng đối tượng — 1 người

- Cài ObjectDB để kết nối Java.
- Trích một phân hệ từ EER của phần 1, thiết kế số lượng lớp vừa phải.
- Viết Java Entity với annotation JPA, thể hiện OID, kế thừa, thuộc tính tập hợp List/Set và phương thức xử lý.
- Sản phẩm cuối tuần 1: chương trình Java kết nối thành công ObjectDB; đã tạo các Entity hợp lệ trong database.
- Người viết phân công có ghi phần này cần tìm hiểu thêm. ObjectDB có trong bản phân công nhưng không được nêu trong phần đề bài DOCX đã cung cấp; giữ rõ nguồn của yêu cầu này.

## Những điểm chưa được tài liệu chốt

- Framework/frontend stack, phong cách giao diện, thiết kế màn hình cụ thể.
- API, endpoint, schema request/response, cách biểu diễn phụ thuộc hàm và các bước giải.
- Bộ U, F thực tế, sơ đồ EER và quy chế đào tạo cụ thể.
- Ngày bắt đầu tuần 1 và hạn nộp theo ngày lịch.
- Phạm vi tích hợp ObjectDB với công cụ phân tích/chuẩn hóa.

Không tự coi các lựa chọn triển khai hoặc dữ liệu minh họa sau này là yêu cầu đã được tài liệu xác nhận.

## Bổ sung: sản phẩm thành viên 1

Người dùng đã cung cấp bộ dữ liệu và sơ đồ thực tế của thành viên 1. Bản sao nguyên nội dung được lưu tại:

- [U và F](member-1/U_F.txt).
- [Kết quả mong đợi](member-1/ket_qua_mong_doi.txt).
- [Sơ đồ EER](member-1/eer.jpg).

Các tài liệu bổ sung thay thế trạng thái “chưa có U, F và EER” ở trên. Chưa xác nhận API/backend đã triển khai định dạng này. Các kết quả trong file là kết quả do thành viên cung cấp, chưa được trợ lý kiểm chứng toàn bộ bằng chương trình độc lập.

### Dữ liệu và định dạng trao đổi được đề xuất

- U có 21 thuộc tính, F chính có 10 phụ thuộc hàm (đếm theo dòng, vế phải có thể nhiều thuộc tính).
- Bỏ qua dòng trống và dòng bắt đầu bằng `#`; dòng `U:` liệt kê thuộc tính cách nhau bởi dấu phẩy; dòng `F:` bắt đầu các dòng `VE_TRAI -> VE_PHAI`; mỗi vế dùng dấu phẩy phân tách thuộc tính.
- Tên thuộc tính không dấu, phân biệt hoa thường, không chứa khoảng trắng. Ví dụ có khoảng trắng quanh dấu phẩy nên bộ đọc cần bỏ khoảng trắng quanh từng tên.
- U là lược đồ tổng hợp phục vụ thuật toán, không phải yêu cầu tạo một bảng vật lý chứa toàn bộ thuộc tính.
- Phụ thuộc `MaSV, MaHP, HocKy -> MaLHP` chỉ là tùy chọn có điều kiện nghiệp vụ, **không thuộc F chính**. Không tự thêm khi nạp dữ liệu mẫu.

### Ràng buộc nghiệp vụ từ EER và U_F

- NGUOI chuyên biệt hóa toàn phần, rời nhau thành SINHVIEN và GIANGVIEN; MaSV/MaGV là định danh riêng sau ánh xạ. HoTen ánh xạ thành TenSV/TenGV.
- KHOA–NGANH, NGANH–SINHVIEN, HOCPHAN–LOPHOCPHAN, GIANGVIEN–LOPHOCPHAN và PHONGHOC–LOPHOCPHAN đều là quan hệ 1–N.
- SINHVIEN đăng ký LOPHOCPHAN theo M–N; Diem thuộc liên kết, khóa đăng ký là (MaSV, MaLHP).
- Mỗi lớp học phần có một học phần, giảng viên, phòng, học kỳ và một lịch học đại diện (Thu, TietBD). Nếu cần nhiều buổi mỗi tuần thì sơ đồ gợi ý tách LICH HOC.
- Hai bộ (MaPhong, HocKy, Thu, TietBD) và (MaGV, HocKy, Thu, TietBD) xác định MaLHP theo giả định không trùng phòng/giảng viên tại lịch đại diện.
- EER có NgaySinh của NGUOI, nhưng U chỉ liệt kê NgaySinh ở nhóm sinh viên và không có ngày sinh giảng viên riêng. Không tự bổ sung thuộc tính vào U; nếu ánh xạ đầy đủ EER sau này thì cần làm rõ điểm này.

### Mốc kết quả phục vụ giao diện và đối chiếu backend

- Ba khóa ứng viên được tài liệu nêu: `{MaSV, MaLHP}`, `{MaSV, MaGV, HocKy, Thu, TietBD}`, `{MaSV, MaPhong, HocKy, Thu, TietBD}`. Đây là khóa tối tiểu; không phải danh sách toàn bộ siêu khóa.
- Thuộc tính khóa: MaSV, MaLHP, MaGV, MaPhong, HocKy, Thu, TietBD. MaSV là thuộc tính nguồn bắt buộc có trong mọi khóa.
- `{MaSV, MaLHP}+ = U`; `{MaSV, MaHP, HocKy}+` chỉ có 11 thuộc tính và không phải U với F chính.
- File có 7 trường hợp bao đóng và 7 trường hợp kiểm tra suy diễn để đối chiếu (4 TRUE, 3 FALSE).
- Dạng chuẩn cao nhất mong đợi là 1NF, với giả định giá trị thuộc tính nguyên tử. Chỉ U và F không mô tả đầy đủ miền giá trị để tự xác nhận 1NF.
- File ghi 4/7/9 phụ thuộc vi phạm 2NF/3NF/BCNF theo các dòng F gộp vế phải. Không coi đây là số lượng vi phạm trong toàn bộ F+ hoặc số lượng khi tách vế phải đơn thuộc tính. Riêng dòng MaLHP có cả thuộc tính khóa và không khóa ở vế phải; phần MaHP là phần vi phạm 2NF/3NF.
- Lỗi diễn đạt trong file kết quả: “Chi con 2 PTH khong vi pham BCNF/3NF” nhưng chỉ liệt kê `MaSV, MaLHP -> Diem`. Theo số đếm của chính file: có **1** dòng không vi phạm BCNF và **3** dòng không vi phạm 3NF (gồm thêm hai phụ thuộc lịch với vế phải MaLHP là thuộc tính khóa). Giữ nguyên file nguồn, ghi đính chính ở đây.
- Hướng phân rã mong đợi gồm 8 quan hệ: KHOA, NGANH, SINHVIEN, HOCPHAN, GIANGVIEN, PHONGHOC, LOPHOCPHAN, DANGKY; chi tiết thuộc tính nằm trong file kết quả. Thứ tự/gộp lược đồ có thể khác, cần đánh giá bảo toàn phụ thuộc, nối không mất mát và đạt 3NF.

Khi làm frontend, dùng bộ này làm dữ liệu mẫu của nhóm và các kết quả làm mốc đối chiếu sau khi xác minh; hỗ trợ tên thuộc tính nhiều ký tự, vế trái/vế phải nhiều thuộc tính và khu vực hiển thị từng bước. Đây là định hướng triển khai suy ra từ dữ liệu, chưa phải yêu cầu triển khai ngay của người dùng.

## Triển khai frontend theo yêu cầu tiếp theo

Người dùng đã yêu cầu thực hiện phần 3. Đã bổ sung frontend tĩnh tại `db-normalization-tool/src/main/resources/static`, mở được trực tiếp qua `index.html` hoặc phục vụ cùng Spring Boot. Xem [hướng dẫn frontend và hợp đồng API đề xuất](FRONTEND.md). Chưa có backend API; bao đóng/suy diễn hiện là minh họa JavaScript có ghi nhãn rõ, các chức năng nâng cao chờ tích hợp. Đã đối chiếu 7 ca bao đóng và 7 ca suy diễn của thành viên 1 bằng kiểm thử tự động; không đồng nghĩa đã xác minh toàn bộ các phần khóa/dạng chuẩn/phân rã trong tài liệu nguồn.

## Tài liệu đề bài bổ sung ngày 23/10/2026

- Người dùng yêu cầu đọc và ghi nhớ `C:\Users\Quang Diep\Downloads\Do_an_10_Phan_tich_phu_thuoc_ham_va_chuan_hoa.docx`.
- Đã đọc nội dung văn bản và lưu [bản sao tài liệu](Do_an_10_Phan_tich_phu_thuoc_ham_va_chuan_hoa.docx) trong dự án.
- Nội dung yêu cầu tương đương đề bài DOCX ban đầu: cùng bài toán, 6 nhóm chức năng tối thiểu, 5 bước nghiên cứu gợi ý và danh mục tham khảo. Không có yêu cầu mới về framework, API, giao diện, deadline hay ObjectDB.
- Tài liệu mới dùng từ “phủ tối thiểu”; tài liệu trước dùng “phủ tối tiểu”. Trong ngữ cảnh hai đề bài, cùng chỉ hạng mục tìm phủ tối tiểu; đây không phải chức năng bổ sung.
- Phải phân biệt phạm vi frontend tuần 1 đã làm với sản phẩm cuối kỳ: toàn bộ công cụ vẫn cần tương đương hai tập F, phủ tối tiểu, tất cả khóa và siêu khóa, dạng chuẩn 1NF–BCNF, phân rã 3NF/BCNF, chase và lời giải từng bước.
- Đề bài còn yêu cầu dùng chính công cụ vào CSDL quản lý đào tạo; các bước nghiên cứu gợi ý gồm mã giả, phân tích độ phức tạp/tối ưu, kiểm thử giáo trình, cài đặt CSDL và truy vấn, so sánh lời giải tay và đo thời gian khi số thuộc tính tăng.
- Các chỉ dẫn trong tài liệu là yêu cầu tham chiếu cho đồ án, không phải lệnh trực tiếp để tự triển khai toàn bộ hoặc đọc các liên kết bên ngoài ở lượt này.

### Trạng thái mới nhất để tránh hiểu nhầm các ghi chú cũ

- Theo yêu cầu sau đó của người dùng, đã bỏ các nhãn nội bộ về frontend/tuần 1/backend và chế độ minh họa khỏi giao diện. Bao đóng và suy diễn vẫn tính bằng JavaScript tại trình duyệt; chưa kết nối API Java.
- Đã bổ sung báo lỗi cạnh ô nhập, nhãn bao đóng X⁺, thu gọn lời giải, hoàn tác dữ liệu và tăng độ dễ đọc. 18 kiểm thử xử lý dữ liệu đạt; chưa xác minh trực quan trong trình duyệt do môi trường xem trước không hoạt động.
- Trong lượt rà soát thành viên 1, chương trình đã xác nhận đủ 3 khóa ứng viên, 7 dòng F vi phạm 3NF và 9 dòng vi phạm BCNF; phân rã 8 quan hệ bảo toàn F, vượt qua chase không mất mát và mỗi quan hệ đạt BCNF theo F chiếu. Các lưu ý về giả định nghiệp vụ/ánh xạ NGUOI vẫn cần được làm rõ khi triển khai CSDL thực tế.
