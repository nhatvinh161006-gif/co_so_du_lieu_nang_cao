package com.doan10.db_normalization_tool.api;

import com.doan10.db_normalization_tool.algorithm.BaoDongAlgorithm;
import com.doan10.db_normalization_tool.algorithm.ChuanHoaAlgorithm;
import com.doan10.db_normalization_tool.algorithm.PhuToiThieuAlgorithm;
import com.doan10.db_normalization_tool.algorithm.TimKhoaAlgorithm;
import com.doan10.db_normalization_tool.api.dto.AnalysisRequest;
import com.doan10.db_normalization_tool.api.dto.FunctionalDependencyDto;
import com.doan10.db_normalization_tool.model.PhuThuocHam;
import com.doan10.db_normalization_tool.model.StepLog;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/algorithms")
@CrossOrigin(origins = "*")
public class AlgorithmController {
    private static final Pattern ATTRIBUTE_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private final BaoDongAlgorithm baoDongAlgorithm;
    private final TimKhoaAlgorithm timKhoaAlgorithm;
    private final PhuToiThieuAlgorithm phuToiThieuAlgorithm;
    private final ChuanHoaAlgorithm chuanHoaAlgorithm;

    public AlgorithmController(
            BaoDongAlgorithm baoDongAlgorithm,
            TimKhoaAlgorithm timKhoaAlgorithm,
            PhuToiThieuAlgorithm phuToiThieuAlgorithm,
            ChuanHoaAlgorithm chuanHoaAlgorithm) {
        this.baoDongAlgorithm = baoDongAlgorithm;
        this.timKhoaAlgorithm = timKhoaAlgorithm;
        this.phuToiThieuAlgorithm = phuToiThieuAlgorithm;
        this.chuanHoaAlgorithm = chuanHoaAlgorithm;
    }

    @PostMapping("/closure")
    public ClosureResponse closure(@RequestBody AnalysisRequest request) {
        ValidatedInput input = validate(request, true, false);
        var result = baoDongAlgorithm.tinhBaoDongChiTiet(toSet(request.getX()), input.dependencies());
        return new ClosureResponse(toList(result.baoDong()), null, result.buocGiai());
    }

    @PostMapping("/implication")
    public ClosureResponse implication(@RequestBody AnalysisRequest request) {
        ValidatedInput input = validate(request, true, true);
        var result = baoDongAlgorithm.tinhBaoDongChiTiet(toSet(request.getX()), input.dependencies());
        boolean implied = result.baoDong().containsAll(request.getY());
        return new ClosureResponse(toList(result.baoDong()), implied, result.buocGiai());
    }

    @PostMapping("/keys")
    public KeysResponse keys(@RequestBody AnalysisRequest request) {
        ValidatedInput input = validate(request, false, false);
        var result = timKhoaAlgorithm.timTatCaKhoa(input.universe(), input.dependencies());
        return new KeysResponse(
                toLists(result.khoaUngVien()),
                toList(result.tapNguon()),
                toList(result.tapDich()),
                toList(result.tapTrungGian()),
                result.buocGiai());
    }

    @PostMapping("/minimal-cover")
    public MinimalCoverResponse minimalCover(@RequestBody AnalysisRequest request) {
        ValidatedInput input = validate(request, false, false);
        var result = phuToiThieuAlgorithm.timPhuToiThieu(input.dependencies());
        List<FunctionalDependencyDto> cover = result.phuToiThieu().stream()
                .map(fd -> new FunctionalDependencyDto(toList(fd.getVT()), toList(fd.getVP())))
                .toList();
        return new MinimalCoverResponse(cover, result.buocGiai());
    }

    @PostMapping("/check-nf")
    public NormalFormResponse checkNormalForm(@RequestBody AnalysisRequest request) {
        ValidatedInput input = validate(request, false, false);
        boolean atomicValues = request.getAtomicValues() == null || request.getAtomicValues();
        var result = chuanHoaAlgorithm.kiemTraDangChuan(input.universe(), input.dependencies(), atomicValues);
        return new NormalFormResponse(
                result.dangChuanCaoNhat(),
                toLists(result.khoaUngVien()),
                toList(result.thuocTinhKhoa()),
                result.viPham(),
                result.buocGiai());
    }

    @PostMapping("/decompose-3nf")
    public DecompositionResponse decompose3nf(@RequestBody AnalysisRequest request) {
        ValidatedInput input = validate(request, false, false);
        var result = chuanHoaAlgorithm.phanRa3NF(input.universe(), input.dependencies());
        return new DecompositionResponse(
                toLists(result.cacQuanHe()),
                toLists(result.khoaUngVien()),
                result.buocGiai());
    }

    private ValidatedInput validate(AnalysisRequest request, boolean requireX, boolean requireY) {
        if (request == null) {
            throw new InvalidAnalysisRequestException("body", "Request body không được để trống.");
        }
        validateAttributes(request.getUniverse(), "universe", false);
        Set<String> universe = toSet(request.getUniverse());
        if (request.getDependencies() == null) {
            throw new InvalidAnalysisRequestException("dependencies", "Trường dependencies phải là một mảng.");
        }

        List<PhuThuocHam> dependencies = new ArrayList<>();
        for (int i = 0; i < request.getDependencies().size(); i++) {
            FunctionalDependencyDto fd = request.getDependencies().get(i);
            String field = "dependencies[" + i + "]";
            if (fd == null) {
                throw new InvalidAnalysisRequestException(field, "Phụ thuộc hàm không được để trống.");
            }
            validateAttributes(fd.left(), field + ".left", true);
            validateAttributes(fd.right(), field + ".right", false);
            validateBelongsToUniverse(fd.left(), universe, field + ".left");
            validateBelongsToUniverse(fd.right(), universe, field + ".right");
            dependencies.add(new PhuThuocHam(toSet(fd.left()), toSet(fd.right())));
        }

        if (requireX || request.getX() != null) {
            validateAttributes(request.getX(), "x", true);
            validateBelongsToUniverse(request.getX(), universe, "x");
        }
        if (requireY || request.getY() != null) {
            validateAttributes(request.getY(), "y", true);
            validateBelongsToUniverse(request.getY(), universe, "y");
        }
        return new ValidatedInput(universe, dependencies);
    }

    private void validateAttributes(List<String> attributes, String field, boolean allowEmpty) {
        if (attributes == null) {
            throw new InvalidAnalysisRequestException(field, "Trường thuộc tính phải là một mảng.");
        }
        if (!allowEmpty && attributes.isEmpty()) {
            throw new InvalidAnalysisRequestException(field, "Tập thuộc tính không được rỗng.");
        }
        Set<String> seen = new LinkedHashSet<>();
        for (String attribute : attributes) {
            if (attribute == null || !ATTRIBUTE_NAME.matcher(attribute).matches()) {
                throw new InvalidAnalysisRequestException(field,
                        "Tên thuộc tính không hợp lệ; dùng chữ, số hoặc gạch dưới và bắt đầu bằng chữ/gạch dưới.");
            }
            if (!seen.add(attribute)) {
                throw new InvalidAnalysisRequestException(field,
                        "Tập thuộc tính bị lặp giá trị: " + attribute + ".");
            }
        }
    }

    private void validateBelongsToUniverse(List<String> attributes, Set<String> universe, String field) {
        List<String> outside = attributes.stream().filter(attribute -> !universe.contains(attribute)).toList();
        if (!outside.isEmpty()) {
            throw new InvalidAnalysisRequestException(field, outside + " không thuộc universe.");
        }
    }

    private Set<String> toSet(List<String> attributes) {
        return attributes == null ? new LinkedHashSet<>() : new LinkedHashSet<>(attributes);
    }

    private List<String> toList(Set<String> attributes) {
        return new ArrayList<>(attributes);
    }

    private List<List<String>> toLists(List<Set<String>> sets) {
        return sets.stream().map(this::toList).toList();
    }

    private record ValidatedInput(Set<String> universe, List<PhuThuocHam> dependencies) {}

    public record ClosureResponse(List<String> closure, Boolean implied, List<StepLog> steps) {}

    public record KeysResponse(
            List<List<String>> keys,
            List<String> source,
            List<String> destination,
            List<String> intermediate,
            List<StepLog> steps) {}

    public record MinimalCoverResponse(List<FunctionalDependencyDto> minimalCover, List<StepLog> steps) {}

    public record NormalFormResponse(
            String highestNormalForm,
            List<List<String>> candidateKeys,
            List<String> primeAttributes,
            List<ChuanHoaAlgorithm.Violation> violations,
            List<StepLog> steps) {}

    public record DecompositionResponse(
            List<List<String>> relations,
            List<List<String>> candidateKeys,
            List<StepLog> steps) {}
}
