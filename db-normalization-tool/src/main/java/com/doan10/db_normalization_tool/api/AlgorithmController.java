package com.doan10.db_normalization_tool.api;

import com.doan10.db_normalization_tool.algorithm.*;
import com.doan10.db_normalization_tool.api.dto.AnalysisRequest;
import com.doan10.db_normalization_tool.api.dto.FunctionalDependencyDto;
import com.doan10.db_normalization_tool.model.PhuThuocHam;
import com.doan10.db_normalization_tool.model.StepLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
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
    private final TuongDuongAlgorithm tuongDuongAlgorithm;
    private final SieuKhoaAlgorithm sieuKhoaAlgorithm;
    private final PhanRaBcnfAlgorithm phanRaBcnfAlgorithm;
    private final ChaseAlgorithm chaseAlgorithm;
    private final BaoToanPhuThuocAlgorithm baoToanPhuThuocAlgorithm;
    private final ArmstrongAlgorithm armstrongAlgorithm;
    private final BenchmarkAlgorithm benchmarkAlgorithm;
    private final SqlGeneratorAlgorithm sqlGeneratorAlgorithm;

    // Constructor 4 tham số để tương thích ngược với các bài test hiện có
    public AlgorithmController(
            BaoDongAlgorithm baoDongAlgorithm,
            TimKhoaAlgorithm timKhoaAlgorithm,
            PhuToiThieuAlgorithm phuToiThieuAlgorithm,
            ChuanHoaAlgorithm chuanHoaAlgorithm) {
        this(
                baoDongAlgorithm,
                timKhoaAlgorithm,
                phuToiThieuAlgorithm,
                chuanHoaAlgorithm,
                new TuongDuongAlgorithm(baoDongAlgorithm),
                new SieuKhoaAlgorithm(timKhoaAlgorithm),
                new PhanRaBcnfAlgorithm(baoDongAlgorithm, timKhoaAlgorithm),
                new ChaseAlgorithm(),
                new BaoToanPhuThuocAlgorithm(baoDongAlgorithm),
                new ArmstrongAlgorithm(),
                new BenchmarkAlgorithm(baoDongAlgorithm, timKhoaAlgorithm, phuToiThieuAlgorithm, chuanHoaAlgorithm,
                        new PhanRaBcnfAlgorithm(baoDongAlgorithm, timKhoaAlgorithm), new ChaseAlgorithm()),
                new SqlGeneratorAlgorithm()
        );
    }

    // Constructor đầy đủ cho Spring Boot IoC Injection
    @Autowired
    public AlgorithmController(
            BaoDongAlgorithm baoDongAlgorithm,
            TimKhoaAlgorithm timKhoaAlgorithm,
            PhuToiThieuAlgorithm phuToiThieuAlgorithm,
            ChuanHoaAlgorithm chuanHoaAlgorithm,
            TuongDuongAlgorithm tuongDuongAlgorithm,
            SieuKhoaAlgorithm sieuKhoaAlgorithm,
            PhanRaBcnfAlgorithm phanRaBcnfAlgorithm,
            ChaseAlgorithm chaseAlgorithm,
            BaoToanPhuThuocAlgorithm baoToanPhuThuocAlgorithm,
            ArmstrongAlgorithm armstrongAlgorithm,
            BenchmarkAlgorithm benchmarkAlgorithm,
            SqlGeneratorAlgorithm sqlGeneratorAlgorithm) {
        this.baoDongAlgorithm = baoDongAlgorithm;
        this.timKhoaAlgorithm = timKhoaAlgorithm;
        this.phuToiThieuAlgorithm = phuToiThieuAlgorithm;
        this.chuanHoaAlgorithm = chuanHoaAlgorithm;
        this.tuongDuongAlgorithm = tuongDuongAlgorithm;
        this.sieuKhoaAlgorithm = sieuKhoaAlgorithm;
        this.phanRaBcnfAlgorithm = phanRaBcnfAlgorithm;
        this.chaseAlgorithm = chaseAlgorithm;
        this.baoToanPhuThuocAlgorithm = baoToanPhuThuocAlgorithm;
        this.armstrongAlgorithm = armstrongAlgorithm;
        this.benchmarkAlgorithm = benchmarkAlgorithm;
        this.sqlGeneratorAlgorithm = sqlGeneratorAlgorithm;
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

    // ==========================================
    // 5 TÍNH NĂNG MỨC ĐỘ CAO
    // ==========================================

    @PostMapping("/equivalence")
    public EquivalenceResponse equivalence(@RequestBody AnalysisRequest request) {
        if (request == null) {
            throw new InvalidAnalysisRequestException("body", "Request body không được để trống.");
        }
        if (request.getDependencies() == null) {
            throw new InvalidAnalysisRequestException("dependencies", "Tập phụ thuộc hàm F1 (dependencies) không được để trống.");
        }
        if (request.getDependencies2() == null) {
            throw new InvalidAnalysisRequestException("dependencies2", "Tập phụ thuộc hàm F2 (dependencies2) không được để trống.");
        }

        Set<String> universe = request.getUniverse() != null ? toSet(request.getUniverse()) : new LinkedHashSet<>();
        if (request.getUniverse() != null) {
            validateAttributes(request.getUniverse(), "universe", false);
        }

        List<PhuThuocHam> f1 = parseDependencies(request.getDependencies(), universe, "dependencies");
        List<PhuThuocHam> f2 = parseDependencies(request.getDependencies2(), universe, "dependencies2");

        var result = tuongDuongAlgorithm.kiemTraTuongDuong(f1, f2);

        List<EquivalenceCheckItem> details1 = result.chiTietF1SuyRaF2().stream()
                .map(c -> new EquivalenceCheckItem(
                        new FunctionalDependencyDto(toList(c.phuThuoc().getVT()), toList(c.phuThuoc().getVP())),
                        toList(c.baoDongVeTrai()),
                        c.duocSuyDien(),
                        c.lyDo()))
                .toList();

        List<EquivalenceCheckItem> details2 = result.chiTietF2SuyRaF1().stream()
                .map(c -> new EquivalenceCheckItem(
                        new FunctionalDependencyDto(toList(c.phuThuoc().getVT()), toList(c.phuThuoc().getVP())),
                        toList(c.baoDongVeTrai()),
                        c.duocSuyDien(),
                        c.lyDo()))
                .toList();

        return new EquivalenceResponse(
                result.tuongDuong(),
                result.f1SuyRaF2(),
                result.f2SuyRaF1(),
                details1,
                details2,
                result.buocGiai()
        );
    }

    @PostMapping("/superkeys")
    public SuperkeysResponse superkeys(@RequestBody AnalysisRequest request) {
        ValidatedInput input = validate(request, false, false);
        var result = sieuKhoaAlgorithm.timTatCaSieuKhoa(input.universe(), input.dependencies(), request.getLimit());

        Map<Integer, String> countBySizeStr = new LinkedHashMap<>();
        result.phanBoTheoKichThuoc().forEach((k, v) -> countBySizeStr.put(k, v.toString()));

        return new SuperkeysResponse(
                toLists(result.khoaUngVien()),
                result.tongSoSieuKhoa().toString(),
                countBySizeStr,
                toLists(result.danhSachSieuKhoaMau()),
                result.daLietKeHet(),
                result.buocGiai()
        );
    }

    @PostMapping("/decompose-bcnf")
    public BcnfDecompositionResponse decomposeBcnf(@RequestBody AnalysisRequest request) {
        ValidatedInput input = validate(request, false, false);
        var result = phanRaBcnfAlgorithm.phanRaBcnf(input.universe(), input.dependencies());

        List<BcnfRelationDto> relations = result.cacQuanHe().stream()
                .map(q -> new BcnfRelationDto(q.tenQuanHe(), toList(q.thuocTinh()), toLists(q.khoaUngVien())))
                .toList();

        List<BcnfViolationDto> violations = result.danhSachViPham().stream()
                .map(v -> new BcnfViolationDto(
                        toList(v.quanHeBiTach()),
                        toList(v.veTraiX()),
                        toList(v.vePhaiY()),
                        toList(v.quanHeConR1()),
                        toList(v.quanHeConR2()),
                        toList(v.baoDongX())))
                .toList();

        return new BcnfDecompositionResponse(relations, violations, result.buocGiai());
    }

    @PostMapping("/chase")
    public ChaseResponse chase(@RequestBody AnalysisRequest request) {
        ValidatedInput input = validate(request, false, false);
        List<Set<String>> relations = resolveRelations(request, input);

        var result = chaseAlgorithm.kiemTraLosslessJoin(input.universe(), relations, input.dependencies());

        return new ChaseResponse(
                result.baoToanThongTin(),
                result.hangThoaManToanA(),
                result.danhSachThuocTinh(),
                result.bangBanDau(),
                result.bangKetQua(),
                result.buocGiai()
        );
    }

    @PostMapping("/check-preservation")
    public PreservationResponse checkPreservation(@RequestBody AnalysisRequest request) {
        ValidatedInput input = validate(request, false, false);
        List<Set<String>> relations = resolveRelations(request, input);

        var result = baoToanPhuThuocAlgorithm.kiemTraBaoToan(relations, input.dependencies());

        List<PreservationDetailDto> details = result.danhSachChiTiet().stream()
                .map(d -> new PreservationDetailDto(
                        new FunctionalDependencyDto(toList(d.phuThuoc().getVT()), toList(d.phuThuoc().getVP())),
                        d.duocBaoToan(),
                        toList(d.baoDongLanTruyen()),
                        d.ghiChu()))
                .toList();

        List<FunctionalDependencyDto> lost = result.phuThuocBiMat().stream()
                .map(fd -> new FunctionalDependencyDto(toList(fd.getVT()), toList(fd.getVP())))
                .toList();

        return new PreservationResponse(
                result.baoToanTatCa(),
                details,
                lost,
                result.buocGiai()
        );
    }

    // ==========================================
    // CÁC TÍNH NĂNG MỨC ĐỘ TRUNG BÌNH & THẤP
    // ==========================================

    @PostMapping("/full-analysis")
    public FullAnalysisResponse fullAnalysis(@RequestBody AnalysisRequest request) {
        ValidatedInput input = validate(request, false, false);
        boolean atomicValues = request.getAtomicValues() == null || request.getAtomicValues();
        List<StepLog> allSteps = new ArrayList<>();

        // 1. Khóa & Siêu khóa
        var keyRes = timKhoaAlgorithm.timTatCaKhoa(input.universe(), input.dependencies());
        allSteps.addAll(keyRes.buocGiai());
        var superkeyRes = sieuKhoaAlgorithm.timTatCaSieuKhoa(input.universe(), input.dependencies(), 20);

        // 2. Phủ tối thiểu
        var minCoverRes = phuToiThieuAlgorithm.timPhuToiThieu(input.dependencies());
        allSteps.addAll(minCoverRes.buocGiai());

        // 3. Kiểm tra dạng chuẩn
        var nfRes = chuanHoaAlgorithm.kiemTraDangChuan(input.universe(), input.dependencies(), atomicValues);
        allSteps.addAll(nfRes.buocGiai());

        // 4. Phân rã 3NF
        var decomp3nf = chuanHoaAlgorithm.phanRa3NF(input.universe(), input.dependencies());
        allSteps.addAll(decomp3nf.buocGiai());

        // 5. Phân rã BCNF
        var decompBcnf = phanRaBcnfAlgorithm.phanRaBcnf(input.universe(), input.dependencies());
        allSteps.addAll(decompBcnf.buocGiai());

        // 6. Chase kiểm tra 3NF và BCNF
        var chase3nf = chaseAlgorithm.kiemTraLosslessJoin(input.universe(), decomp3nf.cacQuanHe(), input.dependencies());
        List<Set<String>> bcnfRels = decompBcnf.cacQuanHe().stream().map(PhanRaBcnfAlgorithm.QuanHeBcnf::thuocTinh).toList();
        var chaseBcnf = chaseAlgorithm.kiemTraLosslessJoin(input.universe(), bcnfRels, input.dependencies());

        // 7. Bảo toàn phụ thuộc trên 3NF và BCNF
        var pres3nf = baoToanPhuThuocAlgorithm.kiemTraBaoToan(decomp3nf.cacQuanHe(), input.dependencies());
        var presBcnf = baoToanPhuThuocAlgorithm.kiemTraBaoToan(bcnfRels, input.dependencies());

        // 8. Cảnh báo lược đồ nâng cao
        List<String> canhBao = phatHienCanhBao(input.universe(), input.dependencies());

        // 9. Sinh DDL
        List<List<Set<String>>> khoaTheoQuanHe = new ArrayList<>();
        for (Set<String> r : decomp3nf.cacQuanHe()) {
            khoaTheoQuanHe.add(timKhoaAlgorithm.timTatCaKhoa(r, input.dependencies()).khoaUngVien());
        }
        String sqlDdl = sqlGeneratorAlgorithm.taoCauLenhSqlDdl(decomp3nf.cacQuanHe(), khoaTheoQuanHe);

        List<FunctionalDependencyDto> coverDto = minCoverRes.phuToiThieu().stream()
                .map(fd -> new FunctionalDependencyDto(toList(fd.getVT()), toList(fd.getVP()))).toList();

        List<BcnfRelationDto> bcnfDtos = decompBcnf.cacQuanHe().stream()
                .map(q -> new BcnfRelationDto(q.tenQuanHe(), toList(q.thuocTinh()), toLists(q.khoaUngVien()))).toList();

        return new FullAnalysisResponse(
                nfRes.dangChuanCaoNhat(),
                toLists(keyRes.khoaUngVien()),
                superkeyRes.tongSoSieuKhoa().toString(),
                coverDto,
                toLists(decomp3nf.cacQuanHe()),
                bcnfDtos,
                chase3nf.baoToanThongTin(),
                chaseBcnf.baoToanThongTin(),
                pres3nf.baoToanTatCa(),
                presBcnf.baoToanTatCa(),
                presBcnf.phuThuocBiMat().stream().map(fd -> new FunctionalDependencyDto(toList(fd.getVT()), toList(fd.getVP()))).toList(),
                canhBao,
                sqlDdl,
                allSteps
        );
    }

    @PostMapping("/compare-3nf-bcnf")
    public CompareResponse compare3nfBcnf(@RequestBody AnalysisRequest request) {
        ValidatedInput input = validate(request, false, false);

        var decomp3nf = chuanHoaAlgorithm.phanRa3NF(input.universe(), input.dependencies());
        var decompBcnf = phanRaBcnfAlgorithm.phanRaBcnf(input.universe(), input.dependencies());

        List<Set<String>> bcnfRels = decompBcnf.cacQuanHe().stream().map(PhanRaBcnfAlgorithm.QuanHeBcnf::thuocTinh).toList();

        var chase3nf = chaseAlgorithm.kiemTraLosslessJoin(input.universe(), decomp3nf.cacQuanHe(), input.dependencies());
        var chaseBcnf = chaseAlgorithm.kiemTraLosslessJoin(input.universe(), bcnfRels, input.dependencies());

        var pres3nf = baoToanPhuThuocAlgorithm.kiemTraBaoToan(decomp3nf.cacQuanHe(), input.dependencies());
        var presBcnf = baoToanPhuThuocAlgorithm.kiemTraBaoToan(bcnfRels, input.dependencies());

        List<FunctionalDependencyDto> lostInBcnf = presBcnf.phuThuocBiMat().stream()
                .map(fd -> new FunctionalDependencyDto(toList(fd.getVT()), toList(fd.getVP()))).toList();

        String nhanXet = "Thuật toán tổng hợp 3NF luôn đảm bảo cả hai tính chất: (1) Bảo toàn phụ thuộc hàm và (2) Nối không mất mát thông tin. "
                + "Thuật toán phân rã BCNF luôn đảm bảo nối không mất mát thông tin, tuy nhiên "
                + (presBcnf.baoToanTatCa() ? "trong lược đồ này cũng bảo toàn phụ thuộc." : "trong lược đồ này BỊ MẤT " + lostInBcnf.size() + " phụ thuộc hàm.");

        List<StepLog> steps = new ArrayList<>();
        steps.addAll(decomp3nf.buocGiai());
        steps.addAll(decompBcnf.buocGiai());

        return new CompareResponse(
                decomp3nf.cacQuanHe().size(),
                decompBcnf.cacQuanHe().size(),
                pres3nf.baoToanTatCa(),
                presBcnf.baoToanTatCa(),
                chase3nf.baoToanThongTin(),
                chaseBcnf.baoToanThongTin(),
                toLists(decomp3nf.cacQuanHe()),
                decompBcnf.cacQuanHe().stream().map(q -> new BcnfRelationDto(q.tenQuanHe(), toList(q.thuocTinh()), toLists(q.khoaUngVien()))).toList(),
                lostInBcnf,
                nhanXet,
                steps
        );
    }

    @PostMapping("/armstrong")
    public ArmstrongResponse armstrong(@RequestBody AnalysisRequest request) {
        String axiom = request.getAxiomType() != null ? request.getAxiomType() : "reflexivity";
        var res = armstrongAlgorithm.apDungTienDe(
                axiom,
                toSet(request.getX()),
                toSet(request.getY()),
                toSet(request.getZ()),
                toSet(request.getW())
        );
        return new ArmstrongResponse(
                res.tenTienDe(),
                res.congThuc(),
                res.hopLe(),
                res.phuThuocSuyDien(),
                res.buocGiai()
        );
    }

    @PostMapping("/benchmark")
    public BenchmarkAlgorithm.KetQuaBenchmark benchmark() {
        return benchmarkAlgorithm.chayDanhGiaHieuNang();
    }

    @PostMapping("/generate-ddl")
    public SqlDdlResponse generateDdl(@RequestBody AnalysisRequest request) {
        ValidatedInput input = validate(request, false, false);
        List<Set<String>> relations = resolveRelations(request, input);

        List<List<Set<String>>> keysPerRelation = new ArrayList<>();
        for (Set<String> rel : relations) {
            keysPerRelation.add(timKhoaAlgorithm.timTatCaKhoa(rel, input.dependencies()).khoaUngVien());
        }

        String sql = sqlGeneratorAlgorithm.taoCauLenhSqlDdl(relations, keysPerRelation);
        return new SqlDdlResponse(sql, toLists(relations));
    }

    // ==========================================
    // PHÁT HIỆN CẢNH BÁO LƯỢC ĐỒ NÂNG CAO
    // ==========================================
    private List<String> phatHienCanhBao(Set<String> universe, List<PhuThuocHam> dependencies) {
        List<String> canhBao = new ArrayList<>();
        Set<String> usedInFd = new LinkedHashSet<>();

        for (PhuThuocHam fd : dependencies) {
            usedInFd.addAll(fd.getVT());
            usedInFd.addAll(fd.getVP());

            // Phụ thuộc tầm thường
            if (fd.getVT().containsAll(fd.getVP())) {
                canhBao.add("Phụ thuộc hiển nhiên (Trivial FD): " + fd + " có vế phải nằm trong vế trái, không mang thông tin mới.");
            }
        }

        // Thuộc tính cô lập không tham gia vào bất kỳ PTH nào
        Set<String> coLap = new LinkedHashSet<>(universe);
        coLap.removeAll(usedInFd);
        if (!coLap.isEmpty()) {
            canhBao.add("Thuộc tính độc lập (không xuất hiện trong bất kỳ PTH nào): " + coLap + ". Các thuộc tính này bắt buộc phải thuộc mọi khóa.");
        }

        return canhBao;
    }

    // ==========================================
    // HELPER & VALIDATION METHODS
    // ==========================================

    private List<Set<String>> resolveRelations(AnalysisRequest request, ValidatedInput input) {
        if (request.getRelations() != null && !request.getRelations().isEmpty()) {
            List<Set<String>> relations = new ArrayList<>();
            for (int i = 0; i < request.getRelations().size(); i++) {
                List<String> rel = request.getRelations().get(i);
                String field = "relations[" + i + "]";
                validateAttributes(rel, field, false);
                validateBelongsToUniverse(rel, input.universe(), field);
                relations.add(toSet(rel));
            }
            return relations;
        }

        var decomp3nf = chuanHoaAlgorithm.phanRa3NF(input.universe(), input.dependencies());
        return decomp3nf.cacQuanHe();
    }

    private List<PhuThuocHam> parseDependencies(List<FunctionalDependencyDto> dtoList, Set<String> universe, String fieldName) {
        List<PhuThuocHam> list = new ArrayList<>();
        boolean checkUniverse = universe != null && !universe.isEmpty();

        for (int i = 0; i < dtoList.size(); i++) {
            FunctionalDependencyDto fd = dtoList.get(i);
            String field = fieldName + "[" + i + "]";
            if (fd == null) {
                throw new InvalidAnalysisRequestException(field, "Phụ thuộc hàm không được để trống.");
            }
            validateAttributes(fd.left(), field + ".left", true);
            validateAttributes(fd.right(), field + ".right", false);
            if (checkUniverse) {
                validateBelongsToUniverse(fd.left(), universe, field + ".left");
                validateBelongsToUniverse(fd.right(), universe, field + ".right");
            }
            list.add(new PhuThuocHam(toSet(fd.left()), toSet(fd.right())));
        }
        return list;
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

        List<PhuThuocHam> dependencies = parseDependencies(request.getDependencies(), universe, "dependencies");

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

    // DTO Responses
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

    public record EquivalenceCheckItem(
            FunctionalDependencyDto dependency,
            List<String> leftClosure,
            boolean implied,
            String reason) {}

    public record EquivalenceResponse(
            boolean equivalent,
            boolean f1ImpliesF2,
            boolean f2ImpliesF1,
            List<EquivalenceCheckItem> f1ImpliesF2Details,
            List<EquivalenceCheckItem> f2ImpliesF1Details,
            List<StepLog> steps) {}

    public record SuperkeysResponse(
            List<List<String>> candidateKeys,
            String totalCount,
            Map<Integer, String> countBySize,
            List<List<String>> sampleSuperkeys,
            boolean allListed,
            List<StepLog> steps) {}

    public record BcnfRelationDto(
            String name,
            List<String> attributes,
            List<List<String>> candidateKeys) {}

    public record BcnfViolationDto(
            List<String> relation,
            List<String> left,
            List<String> right,
            List<String> r1,
            List<String> r2,
            List<String> leftClosure) {}

    public record BcnfDecompositionResponse(
            List<BcnfRelationDto> relations,
            List<BcnfViolationDto> violations,
            List<StepLog> steps) {}

    public record ChaseResponse(
            boolean lossless,
            Integer satisfyingRow,
            List<String> attributes,
            List<String> initialTableau,
            List<String> finalTableau,
            List<StepLog> steps) {}

    public record PreservationDetailDto(
            FunctionalDependencyDto dependency,
            boolean preserved,
            List<String> propagatedClosure,
            String note) {}

    public record PreservationResponse(
            boolean preserved,
            List<PreservationDetailDto> details,
            List<FunctionalDependencyDto> lostDependencies,
            List<StepLog> steps) {}

    public record FullAnalysisResponse(
            String highestNormalForm,
            List<List<String>> candidateKeys,
            String totalSuperkeys,
            List<FunctionalDependencyDto> minimalCover,
            List<List<String>> decomposition3nf,
            List<BcnfRelationDto> decompositionBcnf,
            boolean lossless3nf,
            boolean losslessBcnf,
            boolean preserved3nf,
            boolean preservedBcnf,
            List<FunctionalDependencyDto> lostDependenciesBcnf,
            List<String> warnings,
            String sqlDdl,
            List<StepLog> allSteps) {}

    public record CompareResponse(
            int relationsCount3nf,
            int relationsCountBcnf,
            boolean preserved3nf,
            boolean preservedBcnf,
            boolean lossless3nf,
            boolean losslessBcnf,
            List<List<String>> relations3nf,
            List<BcnfRelationDto> relationsBcnf,
            List<FunctionalDependencyDto> lostDependenciesBcnf,
            String summary,
            List<StepLog> steps) {}

    public record ArmstrongResponse(
            String axiomName,
            String formula,
            boolean valid,
            String derivedDependency,
            List<StepLog> steps) {}

    public record SqlDdlResponse(
            String sqlDdl,
            List<List<String>> relations) {}
}
