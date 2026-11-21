package com.doan10.db_normalization_tool.controller;

import com.doan10.db_normalization_tool.dto.LichSuRequest;
import com.doan10.db_normalization_tool.entity.LichSuChuanHoa;
import com.doan10.db_normalization_tool.service.LichSuChuanHoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller cho Module Lưu Trữ Lịch Sử Chuẩn Hóa (Task 4)
 * Cung cấp:
 * 1. POST   /api/history      : Nhận kết quả giải thuật từ Người 2 hoặc Người 3 để lưu vào ObjectDB
 * 2. GET    /api/history      : Lấy danh sách toàn bộ lịch sử (mới nhất lên đầu) cho bảng giao diện của Người 3
 * 3. GET    /api/history/{id} : Xem chi tiết các bước giải thuật của một lượt chạy cụ thể
 * 4. DELETE /api/history/{id} : Xóa 1 bản ghi lịch sử theo OID
 * 5. DELETE /api/history      : Xóa sạch toàn bộ lịch sử
 */
@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "*") // Hỗ trợ Người 3 kết nối từ React / Vue / Angular / HTML fetch không bị chặn CORS
public class LichSuChuanHoaController {

    private final LichSuChuanHoaService lichSuService;

    @Autowired
    public LichSuChuanHoaController(LichSuChuanHoaService lichSuService) {
        this.lichSuService = lichSuService;
    }

    /**
     * API 1: Lưu lịch sử mới vào ObjectDB
     */
    @PostMapping
    public ResponseEntity<?> luuLichSu(@RequestBody LichSuRequest request) {
        try {
            LichSuChuanHoa saved = lichSuService.luuLichSu(request);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã lưu lịch sử chuẩn hóa thành công vào ObjectDB!");
            response.put("data", saved);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Lỗi lưu lịch sử: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    /**
     * API 2: Lấy danh sách tất cả các lượt chạy lịch sử (mới nhất xếp trước)
     */
    @GetMapping
    public ResponseEntity<List<LichSuChuanHoa>> layDanhSachLichSu() {
        List<LichSuChuanHoa> danhSach = lichSuService.layTatCaLichSu();
        return ResponseEntity.ok(danhSach);
    }

    /**
     * API 3: Xem chi tiết một lượt chạy giải thuật theo OID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> layChiTietLichSu(@PathVariable Long id) {
        LichSuChuanHoa item = lichSuService.layChiTietLichSu(id);
        if (item == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Không tìm thấy lịch sử có OID = " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }
        return ResponseEntity.ok(item);
    }

    /**
     * API 4: Xóa một bản ghi lịch sử theo OID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> xoaLichSu(@PathVariable Long id) {
        boolean deleted = lichSuService.xoaLichSu(id);
        Map<String, Object> res = new HashMap<>();
        if (deleted) {
            res.put("success", true);
            res.put("message", "Đã xóa bản ghi lịch sử OID = " + id);
            return ResponseEntity.ok(res);
        } else {
            res.put("success", false);
            res.put("message", "Không tìm thấy bản ghi để xóa với OID = " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
    }

    /**
     * API 5: Xóa toàn bộ lịch sử (dùng khi cần làm sạch dữ liệu kiểm thử)
     */
    @DeleteMapping
    public ResponseEntity<?> xoaTatCaLichSu() {
        int count = lichSuService.xoaTatCaLichSu();
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Đã xóa toàn bộ " + count + " bản ghi lịch sử chuẩn hóa!");
        return ResponseEntity.ok(res);
    }
}
