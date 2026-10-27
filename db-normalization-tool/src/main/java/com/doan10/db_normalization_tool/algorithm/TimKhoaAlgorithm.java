package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.dto.KeyResponse;
import com.doan10.db_normalization_tool.dto.StepLog;
import com.doan10.db_normalization_tool.model.PhuThuocHam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TimKhoaAlgorithm {

    @Autowired
    private BaoDongAlgorithm baoDongAlgorithm;

    public TimKhoaAlgorithm() {}

    public TimKhoaAlgorithm(BaoDongAlgorithm baoDongAlgorithm) {
        this.baoDongAlgorithm = baoDongAlgorithm;
    }

    public KeyResponse timTatCaKhoa(Set<String> universe, List<PhuThuocHam> tapF) {
        KeyResponse response = new KeyResponse();
        response.setUniverse(universe);
        List<StepLog> steps = new ArrayList<>();
        int stepCount = 1;

        if (universe == null || universe.isEmpty()) {
            response.setSteps(steps);
            return response;
        }

        // Bước 1: Tập hợp các thuộc tính ở vế trái và vế phải
        Set<String> allLeft = new HashSet<>();
        Set<String> allRight = new HashSet<>();
        if (tapF != null) {
            for (PhuThuocHam fd : tapF) {
                if (fd.getVT() != null) allLeft.addAll(fd.getVT());
                if (fd.getVP() != null) allRight.addAll(fd.getVP());
            }
        }

        // Phân loại thành 4 nhóm L, R, B, N
        Set<String> groupL = new TreeSet<>(); // Chỉ ở vế trái (Nguồn)
        Set<String> groupR = new TreeSet<>(); // Chỉ ở vế phải (Đích)
        Set<String> groupB = new TreeSet<>(); // Ở cả hai vế (Trung gian)
        Set<String> groupN = new TreeSet<>(); // Không xuất hiện ở vế nào (Độc lập)

        for (String attr : universe) {
            boolean inLeft = allLeft.contains(attr);
            boolean inRight = allRight.contains(attr);

            if (inLeft && !inRight) {
                groupL.add(attr);
            } else if (!inLeft && inRight) {
                groupR.add(attr);
            } else if (inLeft && inRight) {
                groupB.add(attr);
            } else {
                groupN.add(attr);
            }
        }

        Map<String, Set<String>> attributeGroups = new LinkedHashMap<>();
        attributeGroups.put("L", groupL);
        attributeGroups.put("R", groupR);
        attributeGroups.put("B", groupB);
        attributeGroups.put("N", groupN);
        response.setAttributeGroups(attributeGroups);

        steps.add(new StepLog(
                stepCount++,
                "Phân loại thuộc tính",
                "Phân chia tập U thành 4 nhóm: L (chỉ vế trái), R (chỉ vế phải), B (cả hai vế), N (không xuất hiện).",
                String.format("L = %s, R = %s, B = %s, N = %s", groupL, groupR, groupB, groupN)
        ));

        // Bước 2: Khởi tạo tập cơ sở K_min = L U N
        Set<String> kMin = new TreeSet<>(groupL);
        kMin.addAll(groupN);

        steps.add(new StepLog(
                stepCount++,
                "Xác định tập nguồn cơ sở K_min",
                "Tập K_min = L ∪ N bắt buộc phải có mặt trong mọi khóa vì các thuộc tính này không thể được suy diễn từ thuộc tính khác.",
                "K_min = " + kMin
        ));

        // Tính bao đóng của K_min
        Set<String> baoDongKMin = baoDongAlgorithm.tinhBaoDong(kMin, tapF);
        List<Set<String>> candidateKeys = new ArrayList<>();

        if (baoDongKMin.containsAll(universe)) {
            // K_min là khóa duy nhất
            candidateKeys.add(kMin);
            steps.add(new StepLog(
                    stepCount++,
                    "Kiểm tra bao đóng (K_min)+",
                    "Vì (K_min)+ = U (" + baoDongKMin.size() + " thuộc tính), K_min chính là KHÓA DUY NHẤT của lược đồ quan hệ.",
                    "(K_min)+ = U -> Khóa duy nhất: " + kMin
            ));
        } else {
            steps.add(new StepLog(
                    stepCount++,
                    "Kiểm tra bao đóng (K_min)+",
                    "(K_min)+ = " + new TreeSet<>(baoDongKMin) + " != U. Cần duyệt tổ hợp các tập con của B kết hợp với K_min để tìm khóa.",
                    "(K_min)+ chưa bao phủ U"
            ));

            // Bước 3: Duyệt tổ hợp tập con của B
            List<String> listB = new ArrayList<>(groupB);
            int n = listB.size();

            // Duyệt theo kích thước tập con từ 1 đến n
            for (int k = 1; k <= n; k++) {
                List<Set<String>> combinations = new ArrayList<>();
                generateCombinations(listB, 0, k, new HashSet<>(), combinations);

                for (Set<String> comb : combinations) {
                    Set<String> candidate = new TreeSet<>(kMin);
                    candidate.addAll(comb);

                    // Kiểm tra xem candidate có chứa bất kỳ khóa nào đã tìm thấy trước đó không
                    boolean isSuperKeyOfFound = false;
                    for (Set<String> foundKey : candidateKeys) {
                        if (candidate.containsAll(foundKey)) {
                            isSuperKeyOfFound = true;
                            break;
                        }
                    }

                    if (isSuperKeyOfFound) {
                        continue; // Đã là siêu khóa mở rộng từ một khóa nhỏ hơn, không xét
                    }

                    Set<String> closure = baoDongAlgorithm.tinhBaoDong(candidate, tapF);
                    if (closure.containsAll(universe)) {
                        candidateKeys.add(candidate);
                        steps.add(new StepLog(
                                stepCount++,
                                "Tìm thấy khóa ứng viên",
                                "Tập hợp K_min ∪ " + comb + " có bao đóng bao phủ toàn bộ U và không bị chứa bởi khóa nào đã tìm thấy.",
                                "Khóa mới: " + candidate
                        ));
                    }
                }
            }
        }

        // Lọc lại để đảm bảo tính tối thiểu (không bị chứa bởi khóa nào khác)
        List<Set<String>> minimalKeys = new ArrayList<>();
        for (Set<String> key1 : candidateKeys) {
            boolean isMinimal = true;
            for (Set<String> key2 : candidateKeys) {
                if (!key1.equals(key2) && key1.containsAll(key2)) {
                    isMinimal = false;
                    break;
                }
            }
            if (isMinimal && !minimalKeys.contains(key1)) {
                minimalKeys.add(key1);
            }
        }

        // Xác định thuộc tính khóa và không khóa
        Set<String> primeAttrs = new TreeSet<>();
        for (Set<String> key : minimalKeys) {
            primeAttrs.addAll(key);
        }
        Set<String> nonPrimeAttrs = new TreeSet<>(universe);
        nonPrimeAttrs.removeAll(primeAttrs);

        response.setKeys(minimalKeys);
        response.setPrimeAttributes(primeAttrs);
        response.setNonPrimeAttributes(nonPrimeAttrs);

        steps.add(new StepLog(
                stepCount,
                "Tổng kết danh sách khóa",
                String.format("Đã tìm thấy %d khóa tối thiểu. Thuộc tính khóa: %s. Thuộc tính không khóa: %s.",
                        minimalKeys.size(), primeAttrs, nonPrimeAttrs),
                "Khóa: " + minimalKeys
        ));
        response.setSteps(steps);

        return response;
    }

    private void generateCombinations(List<String> list, int start, int length, Set<String> current, List<Set<String>> result) {
        if (current.size() == length) {
            result.add(new HashSet<>(current));
            return;
        }
        for (int i = start; i < list.size(); i++) {
            current.add(list.get(i));
            generateCombinations(list, i + 1, length, current, result);
            current.remove(list.get(i));
        }
    }
}
