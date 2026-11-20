# TÀI LIỆU HƯỚNG DẪN TÍCH HỢP MODULE LỊCH SỬ CHUẨN HÓA (OBJECTDB)
**Dành cho:** Người 2 (Backend Thuật toán) & Người 3 (Frontend Giao diện Web)  
**Phân hệ phụ trách:** CSDL Hướng đối tượng (ObjectDB) - Người 4 (`MaGiaVy`)  
**Mục tiêu:** Giúp Người 2 và Người 3 kết nối lưu trữ và hiển thị lịch sử chuẩn hóa trong 5 phút mà không cần can thiệp vào tầng cơ sở dữ liệu.

---

## I. TỔNG QUAN VỀ MODULE LỊCH SỬ CHUẨN HÓA
Module này sử dụng **CSDL Hướng đối tượng ObjectDB** (`db/daotao.odb`) để lưu trữ vết các lượt chạy thuật toán chuẩn hóa:
- **Tính bao đóng ($X^+$)**
- **Tìm phủ tối tiểu ($F_{min}$)**
- **Tìm tất cả các khóa tối thiểu (Keys)**
- **Kiểm tra dạng chuẩn (1NF, 2NF, 3NF, BCNF)**
- **Phân rã lược đồ đạt 3NF / BCNF (Lossless join & Dependency preserving)**

Toàn bộ dữ liệu được lưu dưới dạng thực thể [LichSuChuanHoa.java](file:///d:/CSDL_NC/co_so_du_lieu_nang_cao/db-normalization-tool/src/main/java/com/doan10/db_normalization_tool/entity/LichSuChuanHoa.java) gồm:
- `id` (Long): Định danh đối tượng (OID tự sinh của ObjectDB).
- `thoiGianChay` (String): Thời điểm chạy (định dạng `yyyy-MM-dd HH:mm:ss`).
- `tapThuocTinhU` (String): Tập thuộc tính đầu vào $U$ (vd: `"A, B, C, D, E"`).
- `tapPhuThuocHamF` (String): Tập phụ thuộc hàm $F$ (vd: `"A -> B, B -> C, CD -> E"`).
- `loaiThuatToan` (String): Tên thuật toán (`"TINH_BAO_DONG"`, `"TIM_KHOA"`, `"PHU_TOI_TIEU"`, `"PHAN_RA_3NF"`, `"PHAN_RA_BCNF"`).
- `dauVaoBoSung` (String): Tham số phụ (vd: tập $X = \{"A, B"\}$ cần tìm bao đóng).
- `ketQuaChiTiet` (String - kiểu `@Lob` không giới hạn dung lượng): Các bước giải chi tiết và kết quả cuối cùng.
- `thoiGianThucThiMs` (long): Thời gian chạy giải thuật tính bằng mili-giây (phục vụ đánh giá hiệu năng).
- `thanhCong` (boolean): `true` nếu chạy thành công, `false` nếu phát hiện lỗi hoặc không hợp lệ.

---

## II. HƯỚNG DẪN DÀNH CHO NGƯỜI 2 (BACKEND THUẬT TOÁN)

Người 2 có thể lưu lịch sử giải thuật trực tiếp bằng **Java Code** cực kỳ đơn giản:

### Cách 1: Tiêm phụ thuộc (Dependency Injection với Spring `@Autowired`)
Trong class Service hoặc Controller của Người 2:
```java
package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.service.LichSuChuanHoaService;
import com.doan10.db_normalization_tool.entity.LichSuChuanHoa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NormalizationService {

    @Autowired
    private LichSuChuanHoaService lichSuService;

    public Set<String> xuLyTinhBaoDong(String u, String f, String tapX) {
        long startTime = System.currentTimeMillis();

        // 1. Thực thi thuật toán tính toán của bạn ở đây...
        Set<String> ketQua = ...;

        long durationMs = System.currentTimeMillis() - startTime;
        String ketQuaChiTiet = "Bao đóng của {" + tapX + "}+ = " + ketQua.toString();

        // 2. GỌI LƯU LỊCH SỬ CHỈ VỚI 1 DÒNG LỆNH DUY NHẤT:
        lichSuService.luuLichSu(
            u,                 // Tập U
            f,                 // Tập F
            "TINH_BAO_DONG",   // Loại thuật toán
            tapX,              // Đầu vào phụ
            ketQuaChiTiet,     // Các bước giải và kết quả
            durationMs,        // Thời gian thực thi (ms)
            true               // Thành công
        );

        return ketQua;
    }
}
```

### Cách 2: Khởi tạo thủ công khi chạy ngoài Spring (CLI / Unit Test)
```java
LichSuChuanHoaService service = new LichSuChuanHoaService();
service.init();
service.luuLichSu(u, f, "TIM_TAT_CA_KHOA", "Toàn bộ U", ketQuaChiTiet, durationMs, true);
```

---

## III. HƯỚNG DẪN DÀNH CHO NGƯỜI 3 (FRONTEND GIAO DIỆN WEB)

Người 3 có thể gọi trực tiếp các **REST API** thông qua HTTP/JSON (Server Spring Boot chạy tại `http://localhost:8080`).

### 1. API Lưu lịch sử mới: `POST /api/history`
- **URL:** `http://localhost:8080/api/history`
- **Method:** `POST`
- **Headers:** `Content-Type: application/json`
- **Request Body mẫu:**
```json
{
  "tapThuocTinhU": "A, B, C, D, E",
  "tapPhuThuocHamF": "AB -> C, B -> D, CD -> E",
  "loaiThuatToan": "TINH_BAO_DONG",
  "dauVaoBoSung": "{A, B}+",
  "ketQuaChiTiet": "Bao đóng của {A, B}+ = {A, B, C, D, E}. Bước 1: Áp dụng AB->C thêm C. Bước 2: B->D thêm D. Bước 3: CD->E thêm E.",
  "thoiGianThucThiMs": 4,
  "thanhCong": true
}
```
- **Response (201 Created):**
```json
{
  "success": true,
  "message": "Đã lưu lịch sử chuẩn hóa thành công vào ObjectDB!",
  "data": {
    "id": 1,
    "thoiGianChay": "2026-10-31 20:30:00",
    "tapThuocTinhU": "A, B, C, D, E",
    "tapPhuThuocHamF": "AB -> C, B -> D, CD -> E",
    "loaiThuatToan": "TINH_BAO_DONG",
    "dauVaoBoSung": "{A, B}+",
    "ketQuaChiTiet": "Bao đóng của {A, B}+ = {A, B, C, D, E}...",
    "thoiGianThucThiMs": 4,
    "thanhCong": true
  }
}
```

---

### 2. API Lấy danh sách lịch sử: `GET /api/history`
- **URL:** `http://localhost:8080/api/history`
- **Method:** `GET`
- **Mô tả:** Trả về danh sách tất cả các lượt chạy, tự động sắp xếp bản ghi mới nhất lên đầu (`ORDER BY id DESC`) để hiển thị lên bảng Table trên giao diện Web.
- **Ví dụ code Javascript (Fetch API) cho Người 3:**
```javascript
async function loadHistory() {
  try {
    const res = await fetch('http://localhost:8080/api/history');
    const data = await res.json();
    console.log("Danh sách lịch sử:", data);
    // Render danh sách data lên bảng HTML Table...
  } catch (err) {
    console.error("Lỗi khi tải lịch sử:", err);
  }
}
```

---

### 3. API Xem chi tiết một lượt chạy: `GET /api/history/{id}`
- **URL:** `http://localhost:8080/api/history/1`
- **Method:** `GET`
- **Mô tả:** Trả về toàn bộ chi tiết các bước giải thuật của bản ghi có OID tương ứng để hiển thị lên Modal / Popup xem lại các bước giải.

---

### 4. API Xóa 1 bản ghi: `DELETE /api/history/{id}`
- **URL:** `http://localhost:8080/api/history/1`
- **Method:** `DELETE`
- **Mô tả:** Xóa một lượt chạy cụ thể khỏi CSDL ObjectDB.

---

### 5. API Xóa sạch lịch sử: `DELETE /api/history`
- **URL:** `http://localhost:8080/api/history`
- **Method:** `DELETE`
- **Mô tả:** Nút "Xóa toàn bộ lịch sử" trên giao diện Web.

---

## IV. CÁCH KHỞI CHẠY HỆ THỐNG
1. **Khởi chạy Server Spring Boot:**
   ```bash
   cd d:\CSDL_NC\co_so_du_lieu_nang_cao\db-normalization-tool
   .\mvnw.cmd spring-boot:run
   ```
2. **Kiểm tra trực tiếp:**
   Mở trình duyệt truy cập: `http://localhost:8080/api/history` để xem JSON trả về.
