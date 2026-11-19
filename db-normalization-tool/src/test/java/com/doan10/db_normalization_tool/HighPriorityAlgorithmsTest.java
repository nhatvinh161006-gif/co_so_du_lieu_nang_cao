package com.doan10.db_normalization_tool;

import com.doan10.db_normalization_tool.algorithm.*;
import com.doan10.db_normalization_tool.model.PhuThuocHam;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HighPriorityAlgorithmsTest {
    private final BaoDongAlgorithm baoDong = new BaoDongAlgorithm();
    private final TimKhoaAlgorithm timKhoa = new TimKhoaAlgorithm(baoDong);
    private final TuongDuongAlgorithm tuongDuong = new TuongDuongAlgorithm(baoDong);
    private final SieuKhoaAlgorithm sieuKhoa = new SieuKhoaAlgorithm(timKhoa);
    private final PhanRaBcnfAlgorithm phanRaBcnf = new PhanRaBcnfAlgorithm(baoDong, timKhoa);
    private final ChaseAlgorithm chase = new ChaseAlgorithm();
    private final BaoToanPhuThuocAlgorithm baoToan = new BaoToanPhuThuocAlgorithm(baoDong);

    // ==========================================
    // 1. KIỂM TRA TƯƠNG ĐƯƠNG HAI TẬP PTH (EQUIVALENCE)
    // ==========================================
    @Test
    @DisplayName("Kiểm tra hai tập PTH tương đương: F1 ≡ F2")
    void testEquivalenceTrue() {
        // F1: A -> B, B -> C, A -> C
        // F2: A -> B, B -> C
        List<PhuThuocHam> f1 = List.of(
                fd("A", "B"),
                fd("B", "C"),
                fd("A", "C")
        );
        List<PhuThuocHam> f2 = List.of(
                fd("A", "B"),
                fd("B", "C")
        );

        var result = tuongDuong.kiemTraTuongDuong(f1, f2);

        assertTrue(result.tuongDuong());
        assertTrue(result.f1SuyRaF2());
        assertTrue(result.f2SuyRaF1());
        assertFalse(result.buocGiai().isEmpty());
    }

    @Test
    @DisplayName("Kiểm tra hai tập PTH không tương đương: F1 ≢ F2")
    void testEquivalenceFalse() {
        // F1: A -> B
        // F2: A -> B, B -> C
        List<PhuThuocHam> f1 = List.of(fd("A", "B"));
        List<PhuThuocHam> f2 = List.of(fd("A", "B"), fd("B", "C"));

        var result = tuongDuong.kiemTraTuongDuong(f1, f2);

        assertFalse(result.tuongDuong());
        assertFalse(result.f1SuyRaF2()); // F1 không suy ra được B -> C của F2
        assertTrue(result.f2SuyRaF1());  // F2 vẫn suy ra được A -> B của F1
    }

    // ==========================================
    // 2. TÌM TẤT CẢ SIÊU KHÓA VÀ ĐẾM BẰNG PIE
    // ==========================================
    @Test
    @DisplayName("Tìm tất cả siêu khóa và đếm chính xác bằng Principle of Inclusion-Exclusion")
    void testSuperkeysComputation() {
        // U = {A, B, C}, F = {A -> B, B -> A}
        // Khóa ứng viên: {A, C} và {B, C}
        // Siêu khóa gồm:
        // - size 2: {A, C}, {B, C} (2 siêu khóa)
        // - size 3: {A, B, C} (1 siêu khóa)
        // Tổng: 3 siêu khóa!
        Set<String> U = Set.of("A", "B", "C");
        List<PhuThuocHam> F = List.of(
                fd("A", "B"),
                fd("B", "A")
        );

        var result = sieuKhoa.timTatCaSieuKhoa(U, F, 100);

        assertEquals(2, result.khoaUngVien().size());
        assertEquals(BigInteger.valueOf(3), result.tongSoSieuKhoa());
        assertEquals(BigInteger.valueOf(2), result.phanBoTheoKichThuoc().get(2));
        assertEquals(BigInteger.valueOf(1), result.phanBoTheoKichThuoc().get(3));
        assertEquals(3, result.danhSachSieuKhoaMau().size());
        assertTrue(result.daLietKeHet());
    }

    // ==========================================
    // 3. PHÂN RÃ BCNF (BCNF DECOMPOSITION)
    // ==========================================
    @Test
    @DisplayName("Phân rã BCNF cho lược đồ kinh điển: R(A, B, C), F = {A -> B, B -> C}")
    void testBcnfDecompositionClassic() {
        // Khóa duy nhất: {A}.
        // B -> C vi phạm 3NF và BCNF vì B không là siêu khóa.
        // Phân rã BCNF: R1(B, C) và R2(A, B).
        Set<String> U = Set.of("A", "B", "C");
        List<PhuThuocHam> F = List.of(
                fd("A", "B"),
                fd("B", "C")
        );

        var result = phanRaBcnf.phanRaBcnf(U, F);

        assertEquals(2, result.cacQuanHe().size());
        assertTrue(result.cacQuanHe().stream().anyMatch(r -> r.thuocTinh().equals(Set.of("B", "C"))));
        assertTrue(result.cacQuanHe().stream().anyMatch(r -> r.thuocTinh().equals(Set.of("A", "B"))));
        assertEquals(1, result.danhSachViPham().size());
        assertFalse(result.buocGiai().isEmpty());
    }

    @Test
    @DisplayName("Lược đồ đã đạt BCNF thì không bị tách thêm")
    void testBcnfAlreadyInBcnf() {
        // R(A, B), F = {A -> B} -> Đã đạt BCNF
        Set<String> U = Set.of("A", "B");
        List<PhuThuocHam> F = List.of(fd("A", "B"));

        var result = phanRaBcnf.phanRaBcnf(U, F);

        assertEquals(1, result.cacQuanHe().size());
        assertEquals(Set.of("A", "B"), result.cacQuanHe().get(0).thuocTinh());
        assertTrue(result.danhSachViPham().isEmpty());
    }

    // ==========================================
    // 4. KIỂM TRA CHASE (LOSSLESS JOIN TEST)
    // ==========================================
    @Test
    @DisplayName("Kiểm tra Chase: Phép phân rã lossless join thành công")
    void testChaseLosslessJoinSuccess() {
        // U = {A, B, C}, F = {A -> B}
        // Phân rã rho = { R1(A, B), R2(A, C) }
        // R1 ∩ R2 = {A}. Vì A -> B (A là khóa của R1) nên phép nối không mất mát thông tin.
        Set<String> U = Set.of("A", "B", "C");
        List<Set<String>> rho = List.of(
                Set.of("A", "B"),
                Set.of("A", "C")
        );
        List<PhuThuocHam> F = List.of(fd("A", "B"));

        var result = chase.kiemTraLosslessJoin(U, rho, F);

        assertTrue(result.baoToanThongTin());
        assertNotNull(result.hangThoaManToanA());
        assertFalse(result.bangBanDau().isEmpty());
        assertFalse(result.bangKetQua().isEmpty());
        assertFalse(result.buocGiai().isEmpty());
    }

    @Test
    @DisplayName("Kiểm tra Chase: Phép phân rã lossy (bị mất mát thông tin)")
    void testChaseLossyJoin() {
        // U = {A, B, C}, F = {A -> B}
        // Phân rã rho = { R1(B, C), R2(A, C) }
        // R1 ∩ R2 = {C}. Nhưng C không xác định hàm bất kỳ thuộc tính nào => Lossy Join!
        Set<String> U = Set.of("A", "B", "C");
        List<Set<String>> rho = List.of(
                Set.of("B", "C"),
                Set.of("A", "C")
        );
        List<PhuThuocHam> F = List.of(fd("A", "B"));

        var result = chase.kiemTraLosslessJoin(U, rho, F);

        assertFalse(result.baoToanThongTin());
        assertNull(result.hangThoaManToanA());
    }

    // ==========================================
    // 5. KIỂM TRA BẢO TOÀN PHỤ THUỘC (DEPENDENCY PRESERVATION)
    // ==========================================
    @Test
    @DisplayName("Kiểm tra bảo toàn phụ thuộc: Thành công")
    void testDependencyPreservationSuccess() {
        // U = {A, B, C}, F = {A -> B, B -> C}
        // rho = { R1(A, B), R2(B, C) }
        // A -> B thuộc R1, B -> C thuộc R2 => Cả hai phụ thuộc đều được bảo toàn trực tiếp!
        List<Set<String>> rho = List.of(
                Set.of("A", "B"),
                Set.of("B", "C")
        );
        List<PhuThuocHam> F = List.of(
                fd("A", "B"),
                fd("B", "C")
        );

        var result = baoToan.kiemTraBaoToan(rho, F);

        assertTrue(result.baoToanTatCa());
        assertTrue(result.phuThuocBiMat().isEmpty());
        assertEquals(2, result.danhSachChiTiet().size());
        assertTrue(result.danhSachChiTiet().stream().allMatch(ChiTiet -> ChiTiet.duocBaoToan()));
    }

    @Test
    @DisplayName("Kiểm tra bảo toàn phụ thuộc: Ví dụ kinh điển vi phạm bảo toàn PTH khi phân rã BCNF")
    void testDependencyPreservationLoss() {
        // Ví dụ kinh điển: R(A, B, C), F = {AB -> C, C -> B}
        // Khóa: {A, B} và {A, C}
        // C -> B vi phạm BCNF vì C không là siêu khóa.
        // Phân rã BCNF: R1(B, C) và R2(A, C).
        // Phân rã này BỊ MẤT phụ thuộc AB -> C!
        List<Set<String>> rho = List.of(
                Set.of("B", "C"),
                Set.of("A", "C")
        );
        List<PhuThuocHam> F = List.of(
                fd(Set.of("A", "B"), Set.of("C")),
                fd(Set.of("C"), Set.of("B"))
        );

        var result = baoToan.kiemTraBaoToan(rho, F);

        assertFalse(result.baoToanTatCa());
        assertEquals(1, result.phuThuocBiMat().size());
        assertEquals(Set.of("A", "B"), result.phuThuocBiMat().get(0).getVT());
        assertEquals(Set.of("C"), result.phuThuocBiMat().get(0).getVP());
    }

    private static PhuThuocHam fd(String left, String right) {
        return new PhuThuocHam(Set.of(left), Set.of(right));
    }

    private static PhuThuocHam fd(Set<String> left, Set<String> right) {
        return new PhuThuocHam(new LinkedHashSet<>(left), new LinkedHashSet<>(right));
    }
}
