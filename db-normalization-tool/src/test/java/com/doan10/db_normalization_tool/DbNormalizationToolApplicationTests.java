package com.doan10.db_normalization_tool;

import com.doan10.db_normalization_tool.algorithm.BaoDongAlgorithm;
import com.doan10.db_normalization_tool.model.PhuThuocHam;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DbNormalizationToolApplicationTests {
    @Autowired
    private BaoDongAlgorithm baoDongAlgorithm;

    @Test
    void testTinhBaoDongVaKiemTraSuyDien() {
        List<PhuThuocHam> tapF = List.of(
                new PhuThuocHam(Set.of("A"), Set.of("B")),
                new PhuThuocHam(Set.of("C"), Set.of("D")),
                new PhuThuocHam(Set.of("B"), Set.of("E")));

        assertEquals(Set.of("A", "B", "C", "D", "E"),
                baoDongAlgorithm.tinhBaoDong(Set.of("A", "C"), tapF));

        Set<String> baoDongA = baoDongAlgorithm.tinhBaoDong(Set.of("A"), tapF);
        assertTrue(baoDongA.contains("E"));
        assertFalse(baoDongA.contains("D"));
    }
}
