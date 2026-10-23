package com.doan10.db_normalization_tool;

import com.doan10.db_normalization_tool.algorithm.BaoDongAlgorithm;
import com.doan10.db_normalization_tool.model.PhuThuocHam;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootTest
class DbNormalizationToolApplicationTests {

    @Autowired
    private BaoDongAlgorithm baoDongAlgorithm;

    @Test
    void testTinhBaoDongVaKiemTraSuyDien() {
        // Du lieu minh hoa; thay U va F bang du lieu cua phan 1 khi co.
        PhuThuocHam fd1 = new PhuThuocHam(Set.of("A"), Set.of("B"));
        PhuThuocHam fd2 = new PhuThuocHam(Set.of("C"), Set.of("D"));
        PhuThuocHam fd3 = new PhuThuocHam(Set.of("B"), Set.of("E"));
        List<PhuThuocHam> tapF = List.of(fd1, fd2, fd3);
        LoDoQuanHe loDo = new LoDoQuanHe(Set.of("A", "B", "C", "D", "E"), tapF);

        Set<String> tapX = Set.of("A", "C");
        Set<String> ketQua = baoDongAlgorithm.tinhBaoDong(tapX, loDo.getTapPhuThuocHam());
        PhuThuocHam suyDienDuoc = new PhuThuocHam(Set.of("A"), Set.of("E"));
        PhuThuocHam khongSuyDienDuoc = new PhuThuocHam(Set.of("A"), Set.of("D"));

        assertEquals(Set.of("A", "B", "C", "D", "E"), ketQua);
        assertTrue(baoDongAlgorithm.kiemTraSuyDien(suyDienDuoc, loDo.getTapPhuThuocHam()));
        assertFalse(baoDongAlgorithm.kiemTraSuyDien(khongSuyDienDuoc, loDo.getTapPhuThuocHam()));

        System.out.println("=================================================");
        System.out.println("Lược đồ U: " + new TreeSet<>(loDo.getTapThuocTinh()));
        System.out.println("Tập F: " + loDo.getTapPhuThuocHam());
        System.out.println("Bao đóng của {A, C}: " + new TreeSet<>(ketQua));
        System.out.println(suyDienDuoc + " suy diễn được từ F: "
                + baoDongAlgorithm.kiemTraSuyDien(suyDienDuoc, loDo.getTapPhuThuocHam()));
        System.out.println(khongSuyDienDuoc + " suy diễn được từ F: "
                + baoDongAlgorithm.kiemTraSuyDien(khongSuyDienDuoc, loDo.getTapPhuThuocHam()));
        System.out.println("=================================================");
    }
}
