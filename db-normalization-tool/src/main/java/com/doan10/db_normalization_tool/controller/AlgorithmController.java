package com.doan10.db_normalization_tool.controller;

import com.doan10.db_normalization_tool.algorithm.ChuanHoaAlgorithm;
import com.doan10.db_normalization_tool.algorithm.PhuToiThieuAlgorithm;
import com.doan10.db_normalization_tool.algorithm.TimKhoaAlgorithm;
import com.doan10.db_normalization_tool.dto.*;
import com.doan10.db_normalization_tool.model.PhuThuocHam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/algorithms")
@CrossOrigin(origins = "*")
public class AlgorithmController {

    @Autowired
    private TimKhoaAlgorithm timKhoaAlgorithm;

    @Autowired
    private PhuToiThieuAlgorithm phuToiThieuAlgorithm;

    @Autowired
    private ChuanHoaAlgorithm chuanHoaAlgorithm;

    private List<PhuThuocHam> convertDtoListToModel(List<PhuThuocHamDTO> dtoList) {
        List<PhuThuocHam> result = new ArrayList<>();
        if (dtoList != null) {
            for (PhuThuocHamDTO dto : dtoList) {
                result.add(dto.toModel());
            }
        }
        return result;
    }

    /**
     * POST /api/algorithms/keys
     * Nhận U, F -> Trả về danh sách khóa tối thiểu, phân nhóm L/R/B/N và các bước giải chi tiết.
     */
    @PostMapping("/keys")
    public ResponseEntity<KeyResponse> timTatCaKhoa(@RequestBody AlgorithmRequest request) {
        Set<String> universe = request.getUniverse();
        List<PhuThuocHam> tapF = convertDtoListToModel(request.getDependencies());
        KeyResponse response = timKhoaAlgorithm.timTatCaKhoa(universe, tapF);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/algorithms/minimal-cover
     * Nhận F -> Trả về tập phủ tối thiểu F_min và các bước tách vế phải, rút gọn vế trái, loại bỏ FD dư thừa.
     */
    @PostMapping("/minimal-cover")
    public ResponseEntity<MinimalCoverResponse> timPhuToiThieu(@RequestBody AlgorithmRequest request) {
        List<PhuThuocHam> tapF = convertDtoListToModel(request.getDependencies());
        MinimalCoverResponse response = phuToiThieuAlgorithm.timPhuToiThieu(tapF);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/algorithms/check-nf
     * Nhận U, F -> Trả về dạng chuẩn cao nhất (1NF, 2NF, 3NF, BCNF) cùng các phụ thuộc hàm vi phạm.
     */
    @PostMapping("/check-nf")
    public ResponseEntity<NormalFormResponse> kiemTraDangChuan(@RequestBody AlgorithmRequest request) {
        Set<String> universe = request.getUniverse();
        List<PhuThuocHam> tapF = convertDtoListToModel(request.getDependencies());
        NormalFormResponse response = chuanHoaAlgorithm.kiemTraDangChuan(universe, tapF);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/algorithms/decompose-3nf
     * Nhận U, F -> Trả về danh sách các quan hệ con chuẩn 3NF (bảo toàn phụ thuộc và lossless join).
     */
    @PostMapping("/decompose-3nf")
    public ResponseEntity<DecompositionResponse> phanRa3NF(@RequestBody AlgorithmRequest request) {
        Set<String> universe = request.getUniverse();
        List<PhuThuocHam> tapF = convertDtoListToModel(request.getDependencies());
        DecompositionResponse response = chuanHoaAlgorithm.phanRa3NF(universe, tapF);
        return ResponseEntity.ok(response);
    }
}
