package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.model.PhuThuocHam;
import com.doan10.db_normalization_tool.model.StepLog;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ArmstrongAlgorithm {

    public KetQuaArmstrong apDungTienDe(
            String loaiTienDe,
            Set<String> X,
            Set<String> Y,
            Set<String> Z,
            Set<String> W) {
        List<StepLog> buocGiai = new ArrayList<>();
        PhuThuocHam heQua = null;
        String tenTienDe = "";
        String moTa = "";
        boolean hopLe = true;

        Set<String> tapX = X != null ? X : Set.of();
        Set<String> tapY = Y != null ? Y : Set.of();
        Set<String> tapZ = Z != null ? Z : Set.of();
        Set<String> tapW = W != null ? W : Set.of();

        switch (loaiTienDe.toLowerCase()) {
            case "reflexivity", "phan_xa" -> {
                tenTienDe = "Tiên đề Phản xạ (Reflexivity)";
                moTa = "Nếu Y ⊆ X thì X → Y.";
                if (tapX.containsAll(tapY)) {
                    heQua = new PhuThuocHam(new LinkedHashSet<>(tapX), new LinkedHashSet<>(tapY));
                    buocGiai.add(new StepLog(1, "Kiểm tra điều kiện",
                            "Vế phải Y = " + tapY + " là tập con của vế trái X = " + tapX + "."));
                    buocGiai.add(new StepLog(2, "Áp dụng tiên đề",
                            "Theo Tiên đề Phản xạ, suy ra phụ thuộc hiển nhiên: " + heQua + "."));
                } else {
                    hopLe = false;
                    buocGiai.add(new StepLog(1, "Vi phạm điều kiện",
                            "Y = " + tapY + " KHÔNG phải tập con của X = " + tapX + " nên không thể áp dụng tiên đề phản xạ."));
                }
            }
            case "augmentation", "tang_truong" -> {
                tenTienDe = "Tiên đề Tăng trưởng (Augmentation)";
                moTa = "Nếu X → Y thì XZ → YZ với mọi tập Z.";
                Set<String> left = new LinkedHashSet<>(tapX);
                left.addAll(tapZ);
                Set<String> right = new LinkedHashSet<>(tapY);
                right.addAll(tapZ);
                heQua = new PhuThuocHam(left, right);
                buocGiai.add(new StepLog(1, "Giả thiết", "Cho trước phụ thuộc hàm: " + tapX + " → " + tapY + "."));
                buocGiai.add(new StepLog(2, "Bổ sung thuộc tính Z", "Bổ sung tập thuộc tính Z = " + tapZ + " vào cả hai vế."));
                buocGiai.add(new StepLog(3, "Áp dụng tiên đề", "Suy ra phụ thuộc hàm mới: " + heQua + "."));
            }
            case "transitivity", "bac_cau" -> {
                tenTienDe = "Tiên đề Bắc cầu (Transitivity)";
                moTa = "Nếu X → Y và Y → Z thì X → Z.";
                heQua = new PhuThuocHam(new LinkedHashSet<>(tapX), new LinkedHashSet<>(tapZ));
                buocGiai.add(new StepLog(1, "Giả thiết 1", "X → Y: " + tapX + " → " + tapY + "."));
                buocGiai.add(new StepLog(2, "Giả thiết 2", "Y → Z: " + tapY + " → " + tapZ + "."));
                buocGiai.add(new StepLog(3, "Áp dụng tiên đề",
                        "Vì X xác định Y và Y xác định Z, theo tính chất bắc cầu suy ra: " + heQua + "."));
            }
            case "union", "hop" -> {
                tenTienDe = "Quy tắc Hợp (Union Rule)";
                moTa = "Nếu X → Y và X → Z thì X → YZ.";
                Set<String> right = new LinkedHashSet<>(tapY);
                right.addAll(tapZ);
                heQua = new PhuThuocHam(new LinkedHashSet<>(tapX), right);
                buocGiai.add(new StepLog(1, "Giả thiết", "X → Y (" + tapX + " → " + tapY + ") và X → Z (" + tapX + " → " + tapZ + ")."));
                buocGiai.add(new StepLog(2, "Áp dụng quy tắc", "Hợp hai vế phải: " + heQua + "."));
            }
            case "decomposition", "phan_ra" -> {
                tenTienDe = "Quy tắc Phân rã (Decomposition Rule)";
                moTa = "Nếu X → YZ thì X → Y và X → Z.";
                heQua = new PhuThuocHam(new LinkedHashSet<>(tapX), new LinkedHashSet<>(tapY));
                buocGiai.add(new StepLog(1, "Giả thiết", "X → YZ (" + tapX + " → " + tapY + " ∪ " + tapZ + ")."));
                buocGiai.add(new StepLog(2, "Áp dụng quy tắc", "Tách vế phải thu được phụ thuộc hàm thành phần: " + heQua + "."));
            }
            case "pseudo_transitivity", "gia_bac_cau" -> {
                tenTienDe = "Quy tắc Giả bắc cầu (Pseudo-transitivity)";
                moTa = "Nếu X → Y và WY → Z thì WX → Z.";
                Set<String> left = new LinkedHashSet<>(tapW);
                left.addAll(tapX);
                heQua = new PhuThuocHam(left, new LinkedHashSet<>(tapZ));
                buocGiai.add(new StepLog(1, "Giả thiết", "X → Y và WY → Z."));
                buocGiai.add(new StepLog(2, "Áp dụng quy tắc", "Thay thế Y trong WY bằng X, suy ra: " + heQua + "."));
            }
            default -> {
                tenTienDe = "Tiên đề không xác định";
                moTa = "Vui lòng chọn: reflexivity, augmentation, transitivity, union, decomposition, pseudo_transitivity.";
                hopLe = false;
                buocGiai.add(new StepLog(1, "Lỗi cú pháp", "Tên tiên đề không hợp lệ."));
            }
        }

        return new KetQuaArmstrong(tenTienDe, moTa, hopLe, heQua != null ? heQua.toString() : null, buocGiai);
    }

    public record KetQuaArmstrong(
            String tenTienDe,
            String congThuc,
            boolean hopLe,
            String phuThuocSuyDien,
            List<StepLog> buocGiai
    ) {}
}
