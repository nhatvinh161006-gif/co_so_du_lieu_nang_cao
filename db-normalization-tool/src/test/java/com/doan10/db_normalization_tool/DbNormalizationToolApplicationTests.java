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
    void testTinhBaoDong() {
        // Giả lập tập FD: {A->B, C->D, B->E}
        PhuThuocHam fd1 = new PhuThuocHam(Set.of("A"), Set.of("B"));
        PhuThuocHam fd2 = new PhuThuocHam(Set.of("C"), Set.of("D"));
        PhuThuocHam fd3 = new PhuThuocHam(Set.of("B"), Set.of("E"));
        List<PhuThuocHam> tapF = Arrays.asList(fd1, fd2, fd3);

        // Tính bao đóng của {A, C}+
        Set<String> tapX = new HashSet<>(Arrays.asList("A", "C"));
        Set<String> ketQua = baoDongAlgorithm.tinhBaoDong(tapX, tapF);

        // In kết quả ra màn hình
        System.out.println("=================================================");
        System.out.println("KẾT QUẢ THUẬT TOÁN: Bao đóng của {A, C} là: " + ketQua);
        System.out.println("=================================================");
    }
}