package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.model.PhuThuocHam;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service // Đánh dấu đây là class xử lý logic của Spring
public class BaoDongAlgorithm {

    public Set<String> tinhBaoDong(Set<String> tapX, List<PhuThuocHam> tapF) {
        if (tapX == null) {
            return new HashSet<>();
        }
        Set<String> baoDong = new HashSet<>(tapX);
        if (tapF == null || tapF.isEmpty()) {
            return baoDong;
        }
        boolean isChanged;

        do {
            isChanged = false; // Reset cờ sau mỗi vòng
            for (PhuThuocHam fd : tapF) {
                if (fd == null || fd.getVT() == null || fd.getVP() == null) {
                    continue;
                }
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

    public boolean kiemTraSuyDien(PhuThuocHam fd, List<PhuThuocHam> tapF) {
        if (fd == null || tapF == null) {
            return false;
        }
        Set<String> baoDongVT = tinhBaoDong(fd.getVT(), tapF);
        return baoDongVT.containsAll(fd.getVP());
    }
}
