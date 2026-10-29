package com.doan10.db_normalization_tool;

import com.doan10.db_normalization_tool.algorithm.BaoDongAlgorithm;
import com.doan10.db_normalization_tool.algorithm.ChuanHoaAlgorithm;
import com.doan10.db_normalization_tool.algorithm.PhuToiThieuAlgorithm;
import com.doan10.db_normalization_tool.algorithm.TimKhoaAlgorithm;
import com.doan10.db_normalization_tool.model.PhuThuocHam;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuanLyDaoTaoTest {
    private static final Set<String> U = new LinkedHashSet<>(List.of(
            "MaSV", "TenSV", "NgaySinh", "MaNganh", "TenNganh", "MaKhoa", "TenKhoa",
            "MaHP", "TenHP", "SoTC", "MaLHP", "HocKy", "Thu", "TietBD", "MaGV",
            "TenGV", "HocVi", "MaPhong", "ToaNha", "SucChua", "Diem"));

    private static final List<PhuThuocHam> F = List.of(
            fd("MaSV", "TenSV", "NgaySinh", "MaNganh"),
            fd("MaNganh", "TenNganh", "MaKhoa"),
            fd("MaKhoa", "TenKhoa"),
            fd("MaHP", "TenHP", "SoTC"),
            fd("MaLHP", "MaHP", "HocKy", "MaGV", "MaPhong", "Thu", "TietBD"),
            fd("MaGV", "TenGV", "HocVi"),
            fd("MaPhong", "ToaNha", "SucChua"),
            fd("MaSV, MaLHP", "Diem"),
            fd("MaPhong, HocKy, Thu, TietBD", "MaLHP"),
            fd("MaGV, HocKy, Thu, TietBD", "MaLHP"));

    private final BaoDongAlgorithm baoDong = new BaoDongAlgorithm();
    private final TimKhoaAlgorithm timKhoa = new TimKhoaAlgorithm(baoDong);
    private final PhuToiThieuAlgorithm phuToiThieu = new PhuToiThieuAlgorithm(baoDong);
    private final ChuanHoaAlgorithm chuanHoa =
            new ChuanHoaAlgorithm(baoDong, timKhoa, phuToiThieu);

    @Test
    void findsExpectedCandidateKeysAndStepTrace() {
        var result = timKhoa.timTatCaKhoa(U, F);
        Set<Set<String>> expected = Set.of(
                Set.of("MaSV", "MaLHP"),
                Set.of("MaSV", "MaGV", "HocKy", "Thu", "TietBD"),
                Set.of("MaSV", "MaPhong", "HocKy", "Thu", "TietBD"));

        assertEquals(expected, Set.copyOf(result.khoaUngVien()));
        assertEquals(Set.of("MaSV"), result.tapNguon());
        assertTrue(result.buocGiai().stream().anyMatch(step ->
                step.detail().contains("bao phủ U")));
    }

    @Test
    void minimalCoverIsEquivalentAndAllRightSidesAreAtomic() {
        List<PhuThuocHam> cover = phuToiThieu.timPhuToiThieu(F).phuToiThieu();

        assertTrue(cover.stream().allMatch(fd -> fd.getVP().size() == 1));
        for (PhuThuocHam fd : F) {
            assertTrue(baoDong.tinhBaoDong(fd.getVT(), cover).containsAll(fd.getVP()),
                    () -> "Phủ tối thiểu không bảo toàn " + fd);
        }
        for (PhuThuocHam fd : cover) {
            assertTrue(baoDong.tinhBaoDong(fd.getVT(), F).containsAll(fd.getVP()),
                    () -> "Phủ tối thiểu tạo phụ thuộc mới " + fd);
        }
    }

    @Test
    void reports1nfAndSynthesizesExpectedEight3nfRelations() {
        var normalForm = chuanHoa.kiemTraDangChuan(U, F, true);
        var decomposition = chuanHoa.phanRa3NF(U, F);
        Set<Set<String>> expectedRelations = Set.of(
                Set.of("MaKhoa", "TenKhoa"),
                Set.of("MaNganh", "TenNganh", "MaKhoa"),
                Set.of("MaSV", "TenSV", "NgaySinh", "MaNganh"),
                Set.of("MaHP", "TenHP", "SoTC"),
                Set.of("MaLHP", "MaHP", "HocKy", "MaGV", "MaPhong", "Thu", "TietBD"),
                Set.of("MaGV", "TenGV", "HocVi"),
                Set.of("MaPhong", "ToaNha", "SucChua"),
                Set.of("MaSV", "MaLHP", "Diem"));

        assertEquals("1NF", normalForm.dangChuanCaoNhat());
        assertEquals(8, decomposition.cacQuanHe().size());
        assertEquals(expectedRelations, Set.copyOf(decomposition.cacQuanHe()));
        assertTrue(normalForm.buocGiai().size() > 0);
        assertTrue(decomposition.buocGiai().size() > 0);
    }

    private static PhuThuocHam fd(String left, String... right) {
        return new PhuThuocHam(Set.of(left.split(", ")), new LinkedHashSet<>(List.of(right)));
    }
}
