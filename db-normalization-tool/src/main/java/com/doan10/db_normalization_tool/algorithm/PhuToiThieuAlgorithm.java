package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.dto.MinimalCoverResponse;
import com.doan10.db_normalization_tool.dto.PhuThuocHamDTO;
import com.doan10.db_normalization_tool.dto.StepLog;
import com.doan10.db_normalization_tool.model.PhuThuocHam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PhuToiThieuAlgorithm {

    @Autowired
    private BaoDongAlgorithm baoDongAlgorithm;

    public PhuToiThieuAlgorithm() {}

    public PhuToiThieuAlgorithm(BaoDongAlgorithm baoDongAlgorithm) {
        this.baoDongAlgorithm = baoDongAlgorithm;
    }

    public MinimalCoverResponse timPhuToiThieu(List<PhuThuocHam> originalF) {
        MinimalCoverResponse response = new MinimalCoverResponse();
        List<StepLog> steps = new ArrayList<>();
        int stepCount = 1;

        List<PhuThuocHamDTO> origDtoList = new ArrayList<>();
        if (originalF != null) {
            for (PhuThuocHam fd : originalF) {
                origDtoList.add(new PhuThuocHamDTO(fd));
            }
        }
        response.setOriginalDependencies(origDtoList);

        if (originalF == null || originalF.isEmpty()) {
            response.setSteps(steps);
            return response;
        }

        // ====================================================================
        // BƯỚC 1: Tách vế phải thành thuộc tính đơn (Single attribute on RHS)
        // ====================================================================
        List<PhuThuocHam> f1 = new ArrayList<>();
        for (PhuThuocHam fd : originalF) {
            if (fd.getVP() != null) {
                for (String rightAttr : fd.getVP()) {
                    f1.add(new PhuThuocHam(new HashSet<>(fd.getVT()), Set.of(rightAttr)));
                }
            }
        }

        steps.add(new StepLog(
                stepCount++,
                "Bước 1: Tách vế phải",
                String.format("Phân rã tất cả các phụ thuộc hàm có vế phải nhiều thuộc tính thành các phụ thuộc hàm có vế phải đơn lẻ. Tổng số FD sau khi tách: %d.", f1.size()),
                formatFList(f1)
        ));

        // ====================================================================
        // BƯỚC 2: Rút gọn vế trái (Remove extraneous attributes on LHS)
        // ====================================================================
        List<PhuThuocHam> f2 = new ArrayList<>();
        List<String> rutGonLogs = new ArrayList<>();

        for (PhuThuocHam fd : f1) {
            Set<String> left = new TreeSet<>(fd.getVT());
            String rightAttr = fd.getVP().iterator().next();

            // Nếu vế trái có từ 2 thuộc tính trở lên, kiểm tra từng thuộc tính
            if (left.size() > 1) {
                List<String> leftList = new ArrayList<>(left);
                for (String attr : leftList) {
                    if (left.size() <= 1) break;

                    Set<String> reducedLeft = new HashSet<>(left);
                    reducedLeft.remove(attr);

                    // Tính bao đóng của (X \ {attr}) trên toàn bộ tập f1
                    Set<String> closure = baoDongAlgorithm.tinhBaoDong(reducedLeft, f1);
                    if (closure.contains(rightAttr)) {
                        // attr là thuộc tính dư thừa ở vế trái
                        left.remove(attr);
                        rutGonLogs.add(String.format("Trong FD %s -> %s: Loại bỏ thuộc tính dư thừa '%s' vì (%s)+ = %s đã chứa '%s'",
                                fd.getVT(), rightAttr, attr, reducedLeft, new TreeSet<>(closure), rightAttr));
                    }
                }
            }
            f2.add(new PhuThuocHam(left, Set.of(rightAttr)));
        }

        String b2Desc = rutGonLogs.isEmpty()
                ? "Không có thuộc tính dư thừa ở vế trái của các phụ thuộc hàm."
                : String.join("; ", rutGonLogs);

        steps.add(new StepLog(
                stepCount++,
                "Bước 2: Rút gọn vế trái",
                b2Desc,
                formatFList(f2)
        ));

        // ====================================================================
        // BƯỚC 3: Loại bỏ các phụ thuộc hàm dư thừa (Remove redundant FDs)
        // ====================================================================
        List<PhuThuocHam> f3 = new ArrayList<>(f2);
        List<String> loaiBoLogs = new ArrayList<>();

        int i = 0;
        while (i < f3.size()) {
            PhuThuocHam currentFD = f3.get(i);
            String rightAttr = currentFD.getVP().iterator().next();

            // Tạo tập G = f3 \ {currentFD}
            List<PhuThuocHam> g = new ArrayList<>(f3);
            g.remove(i);

            // Tính bao đóng của currentFD.getVT() trên tập G
            Set<String> closure = baoDongAlgorithm.tinhBaoDong(currentFD.getVT(), g);

            if (closure.contains(rightAttr)) {
                // currentFD có thể suy ra từ các FD còn lại -> dư thừa -> xóa
                loaiBoLogs.add(String.format("Loại bỏ FD dư thừa [%s] vì vế trái %s có bao đóng trên F còn lại là %s (đã chứa '%s')",
                        currentFD, currentFD.getVT(), new TreeSet<>(closure), rightAttr));
                f3.remove(i); // Xóa khỏi danh sách, không tăng i
            } else {
                i++; // Giữ lại, chuyển sang xét FD tiếp theo
            }
        }

        String b3Desc = loaiBoLogs.isEmpty()
                ? "Tất cả các phụ thuộc hàm đều cần thiết, không có phụ thuộc hàm nào bị dư thừa."
                : String.join("; ", loaiBoLogs);

        steps.add(new StepLog(
                stepCount++,
                "Bước 3: Loại bỏ phụ thuộc hàm dư thừa",
                b3Desc,
                formatFList(f3)
        ));

        // Chuyển kết quả sang DTO
        List<PhuThuocHamDTO> minCoverDto = new ArrayList<>();
        for (PhuThuocHam fd : f3) {
            minCoverDto.add(new PhuThuocHamDTO(fd));
        }
        response.setMinimalCover(minCoverDto);
        response.setSteps(steps);

        return response;
    }

    private String formatFList(List<PhuThuocHam> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).toString());
            if (i < list.size() - 1) sb.append("; ");
        }
        sb.append("]");
        return sb.toString();
    }
}
