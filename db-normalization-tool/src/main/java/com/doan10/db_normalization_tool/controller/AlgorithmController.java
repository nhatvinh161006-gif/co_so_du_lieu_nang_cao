package com.doan10.db_normalization_tool.controller;

import com.doan10.db_normalization_tool.algorithm.BaoDongAlgorithm;
import com.doan10.db_normalization_tool.algorithm.ChuanHoaAlgorithm;
import com.doan10.db_normalization_tool.algorithm.PhuToiThieuAlgorithm;
import com.doan10.db_normalization_tool.algorithm.TimKhoaAlgorithm;
import com.doan10.db_normalization_tool.dto.*;
import com.doan10.db_normalization_tool.model.PhuThuocHam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller cho tất cả các thuật toán chuẩn hóa CSDL.
 *
 * Base URL: /api/algorithms
 *
 * Danh sách API:
 * POST /closure         - Tính bao đóng X+ của tập thuộc tính X theo F
 * POST /keys            - Tìm tất cả khóa tối thiểu (phân loại L/R/B/N)
 * POST /minimal-cover   - Tìm phủ tối thiểu F_min của tập F
 * POST /check-nf        - Kiểm tra dạng chuẩn (1NF/2NF/3NF/BCNF) và tìm vi phạm
 * POST /decompose-3nf   - Phân rã về 3NF (bảo toàn phụ thuộc + lossless join)
 * POST /decompose-bcnf  - Phân rã về BCNF (lossless join, có thể mất phụ thuộc)
 * GET  /health          - Kiểm tra server đang chạy
 */
@RestController
@RequestMapping("/api/algorithms")
@CrossOrigin(origins = "*") // Cho phép frontend ở bất kỳ cổng nào gọi API này
public class AlgorithmController {

    @Autowired
    private BaoDongAlgorithm baoDongAlgorithm;

    @Autowired
    private TimKhoaAlgorithm timKhoaAlgorithm;

    @Autowired
    private PhuToiThieuAlgorithm phuToiThieuAlgorithm;

    @Autowired
    private ChuanHoaAlgorithm chuanHoaAlgorithm;

    // =========================================================
    // Helper: Chuyển List<PhuThuocHamDTO> -> List<PhuThuocHam>
    // =========================================================
    private List<PhuThuocHam> convertDtoListToModel(List<PhuThuocHamDTO> dtoList) {
        List<PhuThuocHam> result = new ArrayList<>();
        if (dtoList != null) {
            for (PhuThuocHamDTO dto : dtoList) {
                result.add(dto.toModel());
            }
        }
        return result;
    }

    // =========================================================
    // GET /health - Kiểm tra server
    // =========================================================
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("status", "UP");
        result.put("message", "DB Normalization Tool Backend đang chạy!");
        result.put("version", "1.0.0");
        return ResponseEntity.ok(result);
    }

    // =========================================================
    // POST /closure - Tính bao đóng X+
    // Body: { "universe": ["A","B","C"], "inputSet": ["A"],
    //         "dependencies": [{"left":["A"],"right":["B"]}] }
    // =========================================================
    @PostMapping("/closure")
    public ResponseEntity<ClosureResponse> tinhBaoDong(@RequestBody ClosureRequest request) {
        Set<String> inputSet = request.getInputSet();
        Set<String> universe = request.getUniverse();
        List<PhuThuocHam> tapF = convertDtoListToModel(request.getDependencies());
        ClosureResponse response = baoDongAlgorithm.tinhBaoDongCoLog(inputSet, tapF, universe);
        return ResponseEntity.ok(response);
    }

    // =========================================================
    // POST /keys - Tìm tất cả khóa tối thiểu
    // Body: { "universe": ["A","B","C","D"],
    //         "dependencies": [{"left":["A"],"right":["B","C"]}] }
    // =========================================================
    @PostMapping("/keys")
    public ResponseEntity<KeyResponse> timTatCaKhoa(@RequestBody AlgorithmRequest request) {
        Set<String> universe = request.getUniverse();
        List<PhuThuocHam> tapF = convertDtoListToModel(request.getDependencies());
        KeyResponse response = timKhoaAlgorithm.timTatCaKhoa(universe, tapF);
        return ResponseEntity.ok(response);
    }

    // =========================================================
    // POST /minimal-cover - Tìm phủ tối thiểu
    // Body: { "dependencies": [{"left":["A","B"],"right":["C","D"]}] }
    // =========================================================
    @PostMapping("/minimal-cover")
    public ResponseEntity<MinimalCoverResponse> timPhuToiThieu(@RequestBody AlgorithmRequest request) {
        List<PhuThuocHam> tapF = convertDtoListToModel(request.getDependencies());
        MinimalCoverResponse response = phuToiThieuAlgorithm.timPhuToiThieu(tapF);
        return ResponseEntity.ok(response);
    }

    // =========================================================
    // POST /check-nf - Kiểm tra dạng chuẩn 1NF/2NF/3NF/BCNF
    // Body: { "universe": [...], "dependencies": [...] }
    // =========================================================
    @PostMapping("/check-nf")
    public ResponseEntity<NormalFormResponse> kiemTraDangChuan(@RequestBody AlgorithmRequest request) {
        Set<String> universe = request.getUniverse();
        List<PhuThuocHam> tapF = convertDtoListToModel(request.getDependencies());
        NormalFormResponse response = chuanHoaAlgorithm.kiemTraDangChuan(universe, tapF);
        return ResponseEntity.ok(response);
    }

    // =========================================================
    // POST /decompose-3nf - Phân rã về 3NF
    // Bảo toàn phụ thuộc hàm + Lossless join
    // Body: { "universe": [...], "dependencies": [...] }
    // =========================================================
    @PostMapping("/decompose-3nf")
    public ResponseEntity<DecompositionResponse> phanRa3NF(@RequestBody AlgorithmRequest request) {
        Set<String> universe = request.getUniverse();
        List<PhuThuocHam> tapF = convertDtoListToModel(request.getDependencies());
        DecompositionResponse response = chuanHoaAlgorithm.phanRa3NF(universe, tapF);
        return ResponseEntity.ok(response);
    }

    // =========================================================
    // POST /decompose-bcnf - Phân rã về BCNF
    // Lossless join, có thể không bảo toàn phụ thuộc hàm
    // Body: { "universe": [...], "dependencies": [...] }
    // =========================================================
    @PostMapping("/decompose-bcnf")
    public ResponseEntity<BcnfDecompositionResponse> phanRaBCNF(@RequestBody AlgorithmRequest request) {
        Set<String> universe = request.getUniverse();
        List<PhuThuocHam> tapF = convertDtoListToModel(request.getDependencies());
        BcnfDecompositionResponse response = chuanHoaAlgorithm.phanRaBCNF(universe, tapF);
        return ResponseEntity.ok(response);
    }
}
