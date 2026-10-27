package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.dto.ClosureResponse;
import com.doan10.db_normalization_tool.dto.StepLog;
import com.doan10.db_normalization_tool.model.PhuThuocHam;
import org.springframework.stereotype.Service;

import java.util.*;

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

    /**
     * Tính bao đóng của tập X theo F, có ghi lại từng bước (dùng cho API /closure)
     *
     * @param tapX    tập thuộc tính đầu vào X
     * @param tapF    tập phụ thuộc hàm F
     * @param universe tập thuộc tính toàn thể U (để xác định X+ == U không)
     * @return ClosureResponse chứa X+, isSuperKey, và các bước giải
     */
    public ClosureResponse tinhBaoDongCoLog(Set<String> tapX, List<PhuThuocHam> tapF, Set<String> universe) {
        ClosureResponse response = new ClosureResponse();
        List<StepLog> steps = new ArrayList<>();
        int stepCount = 1;

        response.setInputSet(tapX);

        if (tapX == null || tapX.isEmpty()) {
            response.setClosure(new HashSet<>());
            response.setIsSuperKey(false);
            response.setSteps(steps);
            return response;
        }

        Set<String> baoDong = new TreeSet<>(tapX);

        steps.add(new StepLog(
                stepCount++,
                "Khởi tạo",
                "Bắt đầu với X(0) = X đã cho.",
                "X(0) = " + baoDong
        ));

        if (tapF == null || tapF.isEmpty()) {
            response.setClosure(baoDong);
            response.setIsSuperKey(universe != null && baoDong.containsAll(universe));
            steps.add(new StepLog(stepCount, "Kết quả", "F rỗng, bao đóng chính là X.", baoDong.toString()));
            response.setSteps(steps);
            return response;
        }

        boolean isChanged;
        int iteration = 1;
        do {
            isChanged = false;
            List<String> appliedFDs = new ArrayList<>();

            for (PhuThuocHam fd : tapF) {
                if (fd == null || fd.getVT() == null || fd.getVP() == null) continue;
                if (baoDong.containsAll(fd.getVT()) && !baoDong.containsAll(fd.getVP())) {
                    Set<String> newAttrs = new TreeSet<>(fd.getVP());
                    newAttrs.removeAll(baoDong);
                    baoDong.addAll(fd.getVP());
                    isChanged = true;
                    appliedFDs.add(String.format("Áp dụng %s → %s, thêm %s", fd.getVT(), fd.getVP(), newAttrs));
                }
            }

            if (isChanged) {
                steps.add(new StepLog(
                        stepCount++,
                        "Vòng lặp " + iteration,
                        String.join("; ", appliedFDs),
                        "X(" + iteration + ") = " + baoDong
                ));
            }
            iteration++;
        } while (isChanged);

        boolean isSuperKey = universe != null && baoDong.containsAll(universe);
        response.setClosure(baoDong);
        response.setIsSuperKey(isSuperKey);

        steps.add(new StepLog(
                stepCount,
                "Kết quả",
                String.format("X+ = %s. %s",
                        baoDong,
                        isSuperKey ? "X là SIÊU KHÓA vì X+ = U." : "X không phải siêu khóa (X+ ≠ U)."),
                "X+ = " + baoDong
        ));
        response.setSteps(steps);
        return response;
    }
}
