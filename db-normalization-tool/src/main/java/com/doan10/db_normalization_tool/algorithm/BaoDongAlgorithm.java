package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.model.PhuThuocHam;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service // Đánh dấu đây là class xử lý logic của Spring
public class BaoDongAlgorithm {

    public Set<String> tinhBaoDong(Set<String> tapX, List<PhuThuocHam> tapF) {
        Set<String> baoDong = new HashSet<>(tapX);
        boolean isChanged;

        do {
            isChanged = false; // Reset cờ sau mỗi vòng
            for (PhuThuocHam fd : tapF) {
                // Nếu Vế trái nằm trọn trong bao đóng hiện tại
                if (baoDong.containsAll(fd.getVT())) {
                    // Nếu vế phải có chứa thuộc tính mới
                    if (!baoDong.containsAll(fd.getVP())) {
                        baoDong.addAll(fd.getVP()); // Thêm vế phải vào
                        isChanged = true; // Đánh dấu có sự thay đổi
                    }
                }
            }
        } while (isChanged); // Lặp đến khi kích thước bao đóng không đổi

        return baoDong;
    }
}