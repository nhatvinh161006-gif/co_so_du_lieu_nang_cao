package com.doan10.db_normalization_tool;

import com.doan10.db_normalization_tool.algorithm.BaoDongAlgorithm;
import com.doan10.db_normalization_tool.algorithm.ChuanHoaAlgorithm;
import com.doan10.db_normalization_tool.algorithm.PhuToiThieuAlgorithm;
import com.doan10.db_normalization_tool.algorithm.TimKhoaAlgorithm;
import com.doan10.db_normalization_tool.model.PhuThuocHam;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlgorithmExamplesTest {
    private final BaoDongAlgorithm baoDong = new BaoDongAlgorithm();
    private final TimKhoaAlgorithm timKhoa = new TimKhoaAlgorithm(baoDong);
    private final PhuToiThieuAlgorithm phuToiThieu = new PhuToiThieuAlgorithm(baoDong);
    private final ChuanHoaAlgorithm chuanHoa =
            new ChuanHoaAlgorithm(baoDong, timKhoa, phuToiThieu);

    @Test
    void closureLogsEachUsefulDependencyApplication() {
        List<PhuThuocHam> dependencies = List.of(
                fd(Set.of("A"), Set.of("B")),
                fd(Set.of("B"), Set.of("C")));

        var result = baoDong.tinhBaoDongChiTiet(Set.of("A"), dependencies);

        assertEquals(Set.of("A", "B", "C"), result.baoDong());
        assertEquals(3, result.buocGiai().size());
        assertTrue(result.buocGiai().getFirst().detail().contains("thêm"));
    }

    @Test
    void findsEveryCandidateKeyAndClassifiesAttributes() {
        List<PhuThuocHam> dependencies = List.of(
                fd(Set.of("A"), Set.of("B")),
                fd(Set.of("B"), Set.of("A")));

        var result = timKhoa.timTatCaKhoa(Set.of("A", "B", "C"), dependencies);

        assertEquals(Set.of(Set.of("A", "C"), Set.of("B", "C")), Set.copyOf(result.khoaUngVien()));
        assertEquals(Set.of("C"), result.tapNguon());
        assertTrue(result.tapDich().isEmpty());
        assertEquals(Set.of("A", "B"), result.tapTrungGian());
    }

    @Test
    void minimalCoverSplitsRightSideAndRemovesRedundantDependency() {
        List<PhuThuocHam> dependencies = List.of(
                fd(Set.of("A"), Set.of("B", "C")),
                fd(Set.of("B"), Set.of("C")));

        var result = phuToiThieu.timPhuToiThieu(dependencies);

        assertEquals(Set.of("A->B", "B->C"), result.phuToiThieu().stream()
                .map(AlgorithmExamplesTest::format)
                .collect(java.util.stream.Collectors.toSet()));
        assertTrue(result.buocGiai().stream().anyMatch(step -> step.action().equals("Loại phụ thuộc dư thừa")));
    }

    @Test
    void checksHighestNormalFormAndCreates3nfRelations() {
        Set<String> universe = Set.of("A", "B", "C");
        List<PhuThuocHam> dependencies = List.of(
                fd(Set.of("A"), Set.of("B")),
                fd(Set.of("B"), Set.of("C")));

        var normalForm = chuanHoa.kiemTraDangChuan(universe, dependencies, true);
        var decomposition = chuanHoa.phanRa3NF(universe, dependencies);

        assertEquals("2NF", normalForm.dangChuanCaoNhat());
        assertTrue(normalForm.viPham().stream().anyMatch(v -> v.normalForm().equals("3NF")));
        assertEquals(2, decomposition.cacQuanHe().size());
        assertTrue(decomposition.cacQuanHe().stream().anyMatch(r -> r.containsAll(Set.of("A", "B"))));
        assertFalse(decomposition.buocGiai().isEmpty());
    }

    private static PhuThuocHam fd(Set<String> left, Set<String> right) {
        return new PhuThuocHam(left, right);
    }

    private static String format(PhuThuocHam fd) {
        return String.join("", fd.getVT()) + "->" + String.join("", fd.getVP());
    }
}
