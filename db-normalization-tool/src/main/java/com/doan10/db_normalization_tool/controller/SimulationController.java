package com.doan10.db_normalization_tool.controller;

import com.doan10.db_normalization_tool.dto.DangKyRequest;
import com.doan10.db_normalization_tool.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API Controller cho Module Mô Phỏng Nghiệp Vụ, Ràng Buộc Tiên Quyết
 * & Kiểm Thử Transaction Rollback/Commit trên ObjectDB
 */
@RestController
@RequestMapping("/api/simulation")
@CrossOrigin(origins = "*")
public class SimulationController {

    private final SimulationService simulationService;

    @Autowired
    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/students")
    public ResponseEntity<List<Map<String, Object>>> getStudents() {
        return ResponseEntity.ok(simulationService.getStudents());
    }

    @GetMapping("/courses")
    public ResponseEntity<List<Map<String, Object>>> getCourses() {
        return ResponseEntity.ok(simulationService.getCourses());
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody DangKyRequest request) {
        if (request == null || request.getMaSV() == null || request.getMaLHP() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "success", false,
                    "message", "Yêu cầu phải có maSV và maLHP"
            ));
        }
        Map<String, Object> result = simulationService.executeRegistration(
                request.getMaSV().trim(),
                request.getMaLHP().trim()
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetData() {
        simulationService.resetDatabase();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã nạp lại dữ liệu mẫu gốc ban đầu thành công vào daotao.odb!"
        ));
    }

    @GetMapping("/queries")
    public ResponseEntity<List<Map<String, Object>>> getQueries() {
        return ResponseEntity.ok(simulationService.getOqlQueries());
    }
}
