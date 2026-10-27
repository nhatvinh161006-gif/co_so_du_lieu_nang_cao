package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.dto.*;
import com.doan10.db_normalization_tool.model.PhuThuocHam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChuanHoaAlgorithm {

    @Autowired
    private BaoDongAlgorithm baoDongAlgorithm;

    @Autowired
    private TimKhoaAlgorithm timKhoaAlgorithm;

    @Autowired
    private PhuToiThieuAlgorithm phuToiThieuAlgorithm;

    public ChuanHoaAlgorithm() {}

    public ChuanHoaAlgorithm(BaoDongAlgorithm baoDongAlgorithm, TimKhoaAlgorithm timKhoaAlgorithm, PhuToiThieuAlgorithm phuToiThieuAlgorithm) {
        this.baoDongAlgorithm = baoDongAlgorithm;
        this.timKhoaAlgorithm = timKhoaAlgorithm;
        this.phuToiThieuAlgorithm = phuToiThieuAlgorithm;
    }

    public NormalFormResponse kiemTraDangChuan(Set<String> universe, List<PhuThuocHam> tapF) {
        NormalFormResponse response = new NormalFormResponse();
        List<StepLog> steps = new ArrayList<>();
        int stepCount = 1;

        if (universe == null || universe.isEmpty()) {
            response.setHighestNormalForm("1NF");
            response.setSteps(steps);
            return response;
        }

        // Bước 1: Tìm tất cả các khóa
        KeyResponse keyResp = timKhoaAlgorithm.timTatCaKhoa(universe, tapF);
        List<Set<String>> keys = keyResp.getKeys();
        Set<String> primeAttrs = keyResp.getPrimeAttributes();
        Set<String> nonPrimeAttrs = keyResp.getNonPrimeAttributes();

        steps.add(new StepLog(
                stepCount++,
                "Bước 1: Xác định tập khóa và các loại thuộc tính",
                String.format("Các khóa tìm được: %s. Thuộc tính khóa (Prime): %s. Thuộc tính không khóa: %s.",
                        keys, primeAttrs, nonPrimeAttrs),
                "Khóa: " + keys
        ));

        // Tách các phụ thuộc hàm thành vế phải đơn lẻ để kiểm tra chuẩn xác
        List<PhuThuocHam> singleRightF = new ArrayList<>();
        if (tapF != null) {
            for (PhuThuocHam fd : tapF) {
                if (fd.getVP() != null) {
                    for (String right : fd.getVP()) {
                        if (!fd.getVT().contains(right)) { // Bỏ qua phụ thuộc hàm hiển nhiên A -> A
                            singleRightF.add(new PhuThuocHam(new HashSet<>(fd.getVT()), Set.of(right)));
                        }
                    }
                }
            }
        }

        // Bước 2: Kiểm tra 2NF
        // Vi phạm 2NF: Tồn tại X -> A với A là thuộc tính không khóa, và X là một bộ phận thực sự của một khóa K nào đó (X ⊂ K, X != K)
        List<String> viPham2NF = new ArrayList<>();
        for (PhuThuocHam fd : singleRightF) {
            String a = fd.getVP().iterator().next();
            if (nonPrimeAttrs.contains(a)) {
                // A là thuộc tính không khóa, kiểm tra vế trái X có phải tập con thực sự của khóa nào không
                for (Set<String> key : keys) {
                    if (key.size() > 1 && key.containsAll(fd.getVT()) && !fd.getVT().equals(key)) {
                        viPham2NF.add(String.format("FD %s -> %s vi phạm 2NF: Thuộc tính không khóa '%s' phụ thuộc vào bộ phận thực sự %s của khóa %s",
                                fd.getVT(), a, a, fd.getVT(), key));
                        break;
                    }
                }
            }
        }

        boolean is2NF = viPham2NF.isEmpty();
        response.setIs2NF(is2NF);
        response.setViolations2NF(viPham2NF);

        steps.add(new StepLog(
                stepCount++,
                "Bước 2: Kiểm tra dạng chuẩn 2NF",
                is2NF ? "Lược đồ đạt chuẩn 2NF: Mọi thuộc tính không khóa đều phụ thuộc đầy đủ vào các khóa chính."
                        : String.format("Lược đồ KHÔNG đạt chuẩn 2NF do có %d vi phạm phụ thuộc bộ phận.", viPham2NF.size()),
                is2NF ? "ĐẠT 2NF" : "VI PHẠM 2NF: " + viPham2NF
        ));

        // Bước 3: Kiểm tra 3NF
        // Vi phạm 3NF: Tồn tại X -> A với A ∉ X mà X KHÔNG phải là siêu khóa VÀ A KHÔNG phải là thuộc tính khóa
        List<String> viPham3NF = new ArrayList<>();
        for (PhuThuocHam fd : singleRightF) {
            String a = fd.getVP().iterator().next();
            Set<String> closureX = baoDongAlgorithm.tinhBaoDong(fd.getVT(), tapF);
            boolean xIsSuperKey = closureX.containsAll(universe);
            boolean aIsPrime = primeAttrs.contains(a);

            if (!xIsSuperKey && !aIsPrime) {
                viPham3NF.add(String.format("FD %s -> %s vi phạm 3NF: Vế trái %s không phải siêu khóa VÀ vế phải '%s' không phải thuộc tính khóa",
                        fd.getVT(), a, fd.getVT(), a));
            }
        }

        boolean is3NF = is2NF && viPham3NF.isEmpty();
        response.setIs3NF(is3NF);
        response.setViolations3NF(viPham3NF);

        steps.add(new StepLog(
                stepCount++,
                "Bước 3: Kiểm tra dạng chuẩn 3NF",
                is3NF ? "Lược đồ đạt chuẩn 3NF: Với mọi X -> A, hoặc X là siêu khóa hoặc A là thuộc tính khóa."
                        : String.format("Lược đồ KHÔNG đạt chuẩn 3NF do có %d vi phạm phụ thuộc bắc cầu.", viPham3NF.size()),
                is3NF ? "ĐẠT 3NF" : "VI PHẠM 3NF: " + viPham3NF
        ));

        // Bước 4: Kiểm tra BCNF
        // Vi phạm BCNF: Tồn tại X -> A với A ∉ X mà X KHÔNG phải là siêu khóa
        List<String> viPhamBCNF = new ArrayList<>();
        for (PhuThuocHam fd : singleRightF) {
            String a = fd.getVP().iterator().next();
            Set<String> closureX = baoDongAlgorithm.tinhBaoDong(fd.getVT(), tapF);
            boolean xIsSuperKey = closureX.containsAll(universe);

            if (!xIsSuperKey) {
                viPhamBCNF.add(String.format("FD %s -> %s vi phạm BCNF: Vế trái %s không phải siêu khóa ((%s)+ != U)",
                        fd.getVT(), a, fd.getVT(), fd.getVT()));
            }
        }

        boolean isBCNF = is3NF && viPhamBCNF.isEmpty();
        response.setIsBCNF(isBCNF);
        response.setViolationsBCNF(viPhamBCNF);

        steps.add(new StepLog(
                stepCount++,
                "Bước 4: Kiểm tra dạng chuẩn BCNF",
                isBCNF ? "Lược đồ đạt chuẩn BCNF: Vế trái của mọi phụ thuộc hàm đều là siêu khóa."
                        : String.format("Lược đồ KHÔNG đạt BCNF do có %d phụ thuộc hàm mà vế trái không phải siêu khóa.", viPhamBCNF.size()),
                isBCNF ? "ĐẠT BCNF" : "VI PHẠM BCNF: " + viPhamBCNF
        ));

        // Kết luận dạng chuẩn cao nhất
        String highest;
        if (isBCNF) highest = "BCNF";
        else if (is3NF) highest = "3NF";
        else if (is2NF) highest = "2NF";
        else highest = "1NF";

        response.setHighestNormalForm(highest);
        steps.add(new StepLog(
                stepCount,
                "Kết luận dạng chuẩn cao nhất",
                "Dạng chuẩn cao nhất của lược đồ là: " + highest,
                highest
        ));
        response.setSteps(steps);

        return response;
    }

    public DecompositionResponse phanRa3NF(Set<String> universe, List<PhuThuocHam> tapF) {
        DecompositionResponse response = new DecompositionResponse();
        List<StepLog> steps = new ArrayList<>();
        int stepCount = 1;

        if (universe == null || universe.isEmpty()) {
            response.setSteps(steps);
            return response;
        }

        // Bước 1: Tìm phủ tối thiểu
        MinimalCoverResponse minCoverResp = phuToiThieuAlgorithm.timPhuToiThieu(tapF);
        List<PhuThuocHamDTO> minCoverDto = minCoverResp.getMinimalCover();
        response.setMinimalCover(minCoverDto);

        List<PhuThuocHam> minCoverF = new ArrayList<>();
        for (PhuThuocHamDTO dto : minCoverDto) {
            minCoverF.add(dto.toModel());
        }

        steps.add(new StepLog(
                stepCount++,
                "Bước 1: Tìm tập phủ tối thiểu F_min",
                String.format("Thu được phủ tối thiểu gồm %d phụ thuộc hàm làm cơ sở phân rã bảo toàn phụ thuộc.", minCoverF.size()),
                minCoverDto.toString()
        ));

        // Bước 2: Gom các phụ thuộc hàm có cùng vế trái
        // Với mỗi vế trái X, R_i = X U (hợp các vế phải)
        Map<Set<String>, Set<String>> grouped = new LinkedHashMap<>();
        for (PhuThuocHam fd : minCoverF) {
            Set<String> left = new TreeSet<>(fd.getVT());
            grouped.putIfAbsent(left, new TreeSet<>());
            grouped.get(left).addAll(fd.getVP());
        }

        List<Set<String>> subSchemas = new ArrayList<>();
        for (Map.Entry<Set<String>, Set<String>> entry : grouped.entrySet()) {
            Set<String> relation = new TreeSet<>(entry.getKey());
            relation.addAll(entry.getValue());
            subSchemas.add(relation);
        }

        steps.add(new StepLog(
                stepCount++,
                "Bước 2: Gom các phụ thuộc hàm cùng vế trái tạo các quan hệ con R_i",
                String.format("Tạo được %d quan hệ con từ các nhóm vế trái của F_min.", subSchemas.size()),
                subSchemas.toString()
        ));

        // Bước 3: Đảm bảo bảo toàn thông tin (Lossless Join)
        // Tìm các khóa của U
        KeyResponse keyResp = timKhoaAlgorithm.timTatCaKhoa(universe, tapF);
        List<Set<String>> keys = keyResp.getKeys();

        boolean containsCandidateKey = false;
        for (Set<String> rel : subSchemas) {
            for (Set<String> key : keys) {
                if (rel.containsAll(key)) {
                    containsCandidateKey = true;
                    break;
                }
            }
            if (containsCandidateKey) break;
        }

        if (!containsCandidateKey && !keys.isEmpty()) {
            Set<String> chosenKey = keys.get(0);
            subSchemas.add(new TreeSet<>(chosenKey));
            steps.add(new StepLog(
                    stepCount++,
                    "Bước 3: Bổ sung quan hệ chứa khóa để bảo toàn thông tin (Lossless Join)",
                    String.format("Chưa có quan hệ con nào chứa trọn vẹn một khóa của U. Bổ sung quan hệ con R_k = %s.", chosenKey),
                    "Thêm quan hệ khóa: " + chosenKey
            ));
        } else {
            steps.add(new StepLog(
                    stepCount++,
                    "Bước 3: Kiểm tra tính bảo toàn thông tin (Lossless Join)",
                    "Đã có ít nhất một quan hệ con chứa trọn vẹn khóa của U -> Đảm bảo phép nối không mất mát thông tin.",
                    "Đã thỏa mãn Lossless Join"
            ));
        }

        // Bước 4: Loại bỏ các quan hệ bị bao bởi quan hệ khác (R_i ⊆ R_j)
        List<Set<String>> finalSchemas = new ArrayList<>();
        for (int i = 0; i < subSchemas.size(); i++) {
            Set<String> r1 = subSchemas.get(i);
            boolean isSubset = false;
            for (int j = 0; j < subSchemas.size(); j++) {
                if (i != j) {
                    Set<String> r2 = subSchemas.get(j);
                    if (r2.containsAll(r1)) {
                        isSubset = true;
                        break;
                    }
                }
            }
            if (!isSubset && !finalSchemas.contains(r1)) {
                finalSchemas.add(r1);
            }
        }

        steps.add(new StepLog(
                stepCount,
                "Bước 4: Loại bỏ quan hệ con dư thừa (R_i ⊆ R_j)",
                String.format("Sau khi loại bỏ các tập con dư thừa, thu được %d quan hệ con chuẩn 3NF.", finalSchemas.size()),
                finalSchemas.toString()
        ));

        response.setSubSchemas(finalSchemas);
        response.setSteps(steps);

        return response;
    }

    /**
     * Phân rã lược đồ quan hệ về dạng chuẩn BCNF (Boyce-Codd Normal Form).
     *
     * Thuật toán:
     * 1. Bắt đầu với tập kết quả = {R}
     * 2. Với mỗi quan hệ Ri trong tập kết quả:
     *    - Tìm phụ thuộc hàm X → Y trong Ri mà X không phải siêu khóa
     *    - Nếu tìm thấy, phân rã Ri thành:
     *      + R1 = X ∪ Y
     *      + R2 = Ri \ Y  (tức là Ri minus Y, giữ lại X)
     * 3. Lặp đến khi không còn vi phạm
     *
     * Lưu ý: BCNF đảm bảo lossless join nhưng KHÔNG đảm bảo bảo toàn phụ thuộc hàm
     */
    public com.doan10.db_normalization_tool.dto.BcnfDecompositionResponse phanRaBCNF(
            Set<String> universe, List<PhuThuocHam> tapF) {

        com.doan10.db_normalization_tool.dto.BcnfDecompositionResponse response =
                new com.doan10.db_normalization_tool.dto.BcnfDecompositionResponse();
        List<StepLog> steps = new ArrayList<>();
        int stepCount = 1;

        if (universe == null || universe.isEmpty()) {
            response.setSteps(steps);
            return response;
        }

        // Danh sách các lược đồ con đang được xử lý
        List<Set<String>> resultSchemas = new ArrayList<>();
        resultSchemas.add(new TreeSet<>(universe));

        steps.add(new StepLog(
                stepCount++,
                "Khởi tạo",
                "Bắt đầu với lược đồ gốc R = " + new TreeSet<>(universe),
                "R = " + new TreeSet<>(universe)
        ));

        // Lấy tất cả FD cần xét
        List<com.doan10.db_normalization_tool.dto.PhuThuocHamDTO> violatingFDsDto = new ArrayList<>();

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < resultSchemas.size(); i++) {
                Set<String> schema = resultSchemas.get(i);

                // Tìm FD vi phạm BCNF trong schema này
                PhuThuocHam violating = findBcnfViolation(schema, tapF);
                if (violating != null) {
                    changed = true;

                    // Lưu FD vi phạm
                    violatingFDsDto.add(new com.doan10.db_normalization_tool.dto.PhuThuocHamDTO(violating));

                    // R1 = X ∪ Y (là bao đóng của X trong schema này)
                    Set<String> closureXInSchema = new TreeSet<>(baoDongAlgorithm.tinhBaoDong(violating.getVT(), tapF));
                    closureXInSchema.retainAll(schema); // chỉ lấy các thuộc tính thuộc schema

                    Set<String> r1 = new TreeSet<>(violating.getVT());
                    r1.addAll(violating.getVP());
                    r1.retainAll(schema); // chỉ lấy phần thuộc schema

                    // R2 = Ri \ (Y \ X)
                    Set<String> r2 = new TreeSet<>(schema);
                    Set<String> toRemove = new TreeSet<>(violating.getVP());
                    toRemove.removeAll(violating.getVT()); // Y \ X
                    r2.removeAll(toRemove);

                    steps.add(new StepLog(
                            stepCount++,
                            "Phân rã vi phạm BCNF",
                            String.format("Quan hệ %s vi phạm BCNF bởi FD %s → %s (vế trái không phải siêu khóa). " +
                                    "Phân rã thành R1 = %s và R2 = %s.",
                                    schema, violating.getVT(), violating.getVP(), r1, r2),
                            String.format("R1 = %s, R2 = %s", r1, r2)
                    ));

                    // Thay schema cũ bằng 2 schema mới
                    resultSchemas.remove(i);
                    resultSchemas.add(r1);
                    resultSchemas.add(r2);
                    break; // Restart vòng lặp
                }
            }
        }

        // Loại bỏ các schema bị chứa trong schema khác
        List<Set<String>> finalSchemas = new ArrayList<>();
        for (int i = 0; i < resultSchemas.size(); i++) {
            Set<String> r1 = resultSchemas.get(i);
            boolean isSubset = false;
            for (int j = 0; j < resultSchemas.size(); j++) {
                if (i != j && resultSchemas.get(j).containsAll(r1) && !resultSchemas.get(j).equals(r1)) {
                    isSubset = true;
                    break;
                }
            }
            if (!isSubset && !finalSchemas.contains(r1)) {
                finalSchemas.add(r1);
            }
        }

        steps.add(new StepLog(
                stepCount,
                "Kết quả phân rã BCNF",
                String.format("Phân rã hoàn tất. Thu được %d lược đồ con đạt chuẩn BCNF. " +
                        "Lưu ý: BCNF đảm bảo lossless join nhưng có thể không bảo toàn toàn bộ phụ thuộc hàm.",
                        finalSchemas.size()),
                finalSchemas.toString()
        ));

        response.setSubSchemas(finalSchemas);
        response.setViolatingFDs(violatingFDsDto);
        response.setSteps(steps);
        return response;
    }

    /**
     * Tìm một phụ thuộc hàm vi phạm BCNF trong schema cho trước.
     * Vi phạm BCNF: X → Y mà X không phải siêu khóa của schema đó.
     */
    private PhuThuocHam findBcnfViolation(Set<String> schema, List<PhuThuocHam> tapF) {
        if (tapF == null) return null;
        for (PhuThuocHam fd : tapF) {
            if (fd.getVT() == null || fd.getVP() == null) continue;
            // FD này có liên quan đến schema không?
            if (!schema.containsAll(fd.getVT())) continue;

            Set<String> vpInSchema = new HashSet<>(fd.getVP());
            vpInSchema.retainAll(schema);
            if (vpInSchema.isEmpty()) continue;
            if (fd.getVT().containsAll(vpInSchema)) continue; // Phụ thuộc hiển nhiên X → X

            // Tính bao đóng của vế trái trong toàn bộ schema
            Set<String> closure = baoDongAlgorithm.tinhBaoDong(fd.getVT(), tapF);
            // Kiểm tra X có phải siêu khóa của schema không
            boolean isSuperKey = closure.containsAll(schema);
            if (!isSuperKey) {
                return fd; // Vi phạm BCNF!
            }
        }
        return null;
    }
}
