package com.doan10.db_normalization_tool;

import com.doan10.db_normalization_tool.algorithm.BaoDongAlgorithm;
import com.doan10.db_normalization_tool.algorithm.ChuanHoaAlgorithm;
import com.doan10.db_normalization_tool.algorithm.PhuToiThieuAlgorithm;
import com.doan10.db_normalization_tool.algorithm.TimKhoaAlgorithm;
import com.doan10.db_normalization_tool.dto.*;
import com.doan10.db_normalization_tool.model.PhuThuocHam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TheoryExamplesTest {

    private BaoDongAlgorithm baoDongAlgorithm;
    private TimKhoaAlgorithm timKhoaAlgorithm;
    private PhuToiThieuAlgorithm phuToiThieuAlgorithm;
    private ChuanHoaAlgorithm chuanHoaAlgorithm;

    @BeforeEach
    void setUp() {
        baoDongAlgorithm = new BaoDongAlgorithm();
        timKhoaAlgorithm = new TimKhoaAlgorithm(baoDongAlgorithm);
        phuToiThieuAlgorithm = new PhuToiThieuAlgorithm(baoDongAlgorithm);
        chuanHoaAlgorithm = new ChuanHoaAlgorithm(baoDongAlgorithm, timKhoaAlgorithm, phuToiThieuAlgorithm);
    }

    @Test
    @DisplayName("Test lý thuyết 1: Tìm khóa trên lược đồ R(A, B, C, D, E) với F = {AB->C, C->D, D->E}")
    void testTimKhoaLyThuyet() {
        Set<String> u = Set.of("A", "B", "C", "D", "E");
        List<PhuThuocHam> f = List.of(
                new PhuThuocHam(Set.of("A", "B"), Set.of("C")),
                new PhuThuocHam(Set.of("C"), Set.of("D")),
                new PhuThuocHam(Set.of("D"), Set.of("E"))
        );

        KeyResponse keyResp = timKhoaAlgorithm.timTatCaKhoa(u, f);
        assertEquals(1, keyResp.getKeys().size());
        assertEquals(Set.of("A", "B"), keyResp.getKeys().get(0));
        assertEquals(Set.of("A", "B"), keyResp.getPrimeAttributes());
        assertEquals(Set.of("C", "D", "E"), keyResp.getNonPrimeAttributes());
    }

    @Test
    @DisplayName("Test lý thuyết 2: Phủ tối thiểu với F có vế trái dư thừa và phụ thuộc bắc cầu dư thừa")
    void testPhuToiThieuLyThuyet() {
        // F: A -> B, B -> C, A -> C, AB -> D
        // B1: Đã đơn vế phải.
        // B2: AB -> D rút gọn thành A -> D (vì A+ chứa B -> A+ chứa D)
        // B3: A -> C dư thừa vì A -> B và B -> C
        // F_min: {A -> B, B -> C, A -> D}
        List<PhuThuocHam> f = List.of(
                new PhuThuocHam(Set.of("A"), Set.of("B")),
                new PhuThuocHam(Set.of("B"), Set.of("C")),
                new PhuThuocHam(Set.of("A"), Set.of("C")),
                new PhuThuocHam(Set.of("A", "B"), Set.of("D"))
        );

        MinimalCoverResponse resp = phuToiThieuAlgorithm.timPhuToiThieu(f);
        List<PhuThuocHamDTO> minCover = resp.getMinimalCover();

        assertEquals(3, minCover.size());
        assertFalse(resp.getSteps().isEmpty());
    }

    @Test
    @DisplayName("Test lý thuyết 3: Kiểm tra dạng chuẩn 2NF, 3NF, BCNF")
    void testKiemTraDangChuanLyThuyet() {
        // R(A, B, C, D) với F = {AB -> C, C -> D, D -> B}
        // Khóa: {A, B}, {A, C}, {A, D}
        // Prime: A, B, C, D -> Mọi thuộc tính đều là thuộc tính khóa!
        // 2NF: Đạt (không có thuộc tính không khóa)
        // 3NF: Đạt (mọi vế phải đều là thuộc tính khóa)
        // BCNF: Không đạt vì C -> D (C không phải siêu khóa)
        Set<String> u = Set.of("A", "B", "C", "D");
        List<PhuThuocHam> f = List.of(
                new PhuThuocHam(Set.of("A", "B"), Set.of("C")),
                new PhuThuocHam(Set.of("C"), Set.of("D")),
                new PhuThuocHam(Set.of("D"), Set.of("B"))
        );

        NormalFormResponse nfResp = chuanHoaAlgorithm.kiemTraDangChuan(u, f);
        assertTrue(nfResp.isIs2NF());
        assertTrue(nfResp.isIs3NF());
        assertFalse(nfResp.isIsBCNF());
        assertEquals("3NF", nfResp.getHighestNormalForm());
    }

    @Test
    @DisplayName("Test lý thuyết 4: Phân rã 3NF bảo toàn phụ thuộc và Lossless Join")
    void testPhanRa3NFLyThuyet() {
        Set<String> u = Set.of("A", "B", "C", "D");
        List<PhuThuocHam> f = List.of(
                new PhuThuocHam(Set.of("A"), Set.of("B")),
                new PhuThuocHam(Set.of("B"), Set.of("C")),
                new PhuThuocHam(Set.of("C"), Set.of("D"))
        );

        DecompositionResponse decompResp = chuanHoaAlgorithm.phanRa3NF(u, f);
        List<Set<String>> schemas = decompResp.getSubSchemas();

        assertNotNull(schemas);
        assertFalse(schemas.isEmpty());
        // Khóa là {A}. Các quan hệ con: {A, B}, {B, C}, {C, D}
        assertTrue(schemas.contains(Set.of("A", "B")));
        assertTrue(schemas.contains(Set.of("B", "C")));
        assertTrue(schemas.contains(Set.of("C", "D")));
    }
}
