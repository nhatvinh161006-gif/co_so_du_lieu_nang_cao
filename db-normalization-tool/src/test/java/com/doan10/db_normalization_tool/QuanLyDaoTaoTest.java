package com.doan10.db_normalization_tool;

import com.doan10.db_normalization_tool.algorithm.BaoDongAlgorithm;
import com.doan10.db_normalization_tool.algorithm.ChuanHoaAlgorithm;
import com.doan10.db_normalization_tool.algorithm.PhuToiThieuAlgorithm;
import com.doan10.db_normalization_tool.algorithm.TimKhoaAlgorithm;
import com.doan10.db_normalization_tool.dto.*;
import com.doan10.db_normalization_tool.model.PhuThuocHam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class QuanLyDaoTaoTest {

    private BaoDongAlgorithm baoDongAlgorithm;
    private TimKhoaAlgorithm timKhoaAlgorithm;
    private PhuToiThieuAlgorithm phuToiThieuAlgorithm;
    private ChuanHoaAlgorithm chuanHoaAlgorithm;

    private Set<String> universe;
    private List<PhuThuocHam> fList;

    @BeforeEach
    void setUp() {
        baoDongAlgorithm = new BaoDongAlgorithm();
        timKhoaAlgorithm = new TimKhoaAlgorithm(baoDongAlgorithm);
        phuToiThieuAlgorithm = new PhuToiThieuAlgorithm(baoDongAlgorithm);
        chuanHoaAlgorithm = new ChuanHoaAlgorithm(baoDongAlgorithm, timKhoaAlgorithm, phuToiThieuAlgorithm);

        // 21 thuộc tính theo đề tài 10 và file U_F.txt của Người số 1
        universe = Set.of(
                "MaSV", "TenSV", "NgaySinh",
                "MaNganh", "TenNganh",
                "MaKhoa", "TenKhoa",
                "MaHP", "TenHP", "SoTC",
                "MaLHP", "HocKy", "Thu", "TietBD",
                "MaGV", "TenGV", "HocVi",
                "MaPhong", "ToaNha", "SucChua",
                "Diem"
        );

        // 10 phụ thuộc hàm chính thức từ file U_F.txt
        fList = List.of(
                new PhuThuocHam(Set.of("MaSV"), Set.of("TenSV", "NgaySinh", "MaNganh")),
                new PhuThuocHam(Set.of("MaNganh"), Set.of("TenNganh", "MaKhoa")),
                new PhuThuocHam(Set.of("MaKhoa"), Set.of("TenKhoa")),
                new PhuThuocHam(Set.of("MaHP"), Set.of("TenHP", "SoTC")),
                new PhuThuocHam(Set.of("MaLHP"), Set.of("MaHP", "HocKy", "MaGV", "MaPhong", "Thu", "TietBD")),
                new PhuThuocHam(Set.of("MaGV"), Set.of("TenGV", "HocVi")),
                new PhuThuocHam(Set.of("MaPhong"), Set.of("ToaNha", "SucChua")),
                new PhuThuocHam(Set.of("MaSV", "MaLHP"), Set.of("Diem")),
                new PhuThuocHam(Set.of("MaPhong", "HocKy", "Thu", "TietBD"), Set.of("MaLHP")),
                new PhuThuocHam(Set.of("MaGV", "HocKy", "Thu", "TietBD"), Set.of("MaLHP"))
        );
    }

    @Test
    @DisplayName("1. Kiểm tra bao đóng các tập thuộc tính thực tế (đối chiếu ket_qua_mong_doi.txt)")
    void testBaoDongThucTe() {
        // {MaSV}+
        Set<String> clMaSV = baoDongAlgorithm.tinhBaoDong(Set.of("MaSV"), fList);
        assertEquals(Set.of("MaSV", "TenSV", "NgaySinh", "MaNganh", "TenNganh", "MaKhoa", "TenKhoa"), clMaSV);

        // {MaNganh}+
        Set<String> clMaNganh = baoDongAlgorithm.tinhBaoDong(Set.of("MaNganh"), fList);
        assertEquals(Set.of("MaNganh", "TenNganh", "MaKhoa", "TenKhoa"), clMaNganh);

        // {MaGV}+
        Set<String> clMaGV = baoDongAlgorithm.tinhBaoDong(Set.of("MaGV"), fList);
        assertEquals(Set.of("MaGV", "TenGV", "HocVi"), clMaGV);

        // {MaLHP}+
        Set<String> clMaLHP = baoDongAlgorithm.tinhBaoDong(Set.of("MaLHP"), fList);
        Set<String> expectedLHP = Set.of("MaLHP", "MaHP", "TenHP", "SoTC", "HocKy", "Thu", "TietBD", "MaGV", "TenGV", "HocVi", "MaPhong", "ToaNha", "SucChua");
        assertEquals(expectedLHP, clMaLHP);

        // {MaPhong, HocKy, Thu, TietBD}+
        Set<String> clPhongLich = baoDongAlgorithm.tinhBaoDong(Set.of("MaPhong", "HocKy", "Thu", "TietBD"), fList);
        assertEquals(expectedLHP, clPhongLich);

        // {MaSV, MaLHP}+ = U (21 thuộc tính)
        Set<String> clKhoa = baoDongAlgorithm.tinhBaoDong(Set.of("MaSV", "MaLHP"), fList);
        assertEquals(universe, clKhoa);
    }

    @Test
    @DisplayName("2. Kiểm tra suy diễn logic (đối chiếu ket_qua_mong_doi.txt)")
    void testSuyDienThucTe() {
        assertTrue(baoDongAlgorithm.kiemTraSuyDien(new PhuThuocHam(Set.of("MaSV"), Set.of("TenSV")), fList));
        assertTrue(baoDongAlgorithm.kiemTraSuyDien(new PhuThuocHam(Set.of("MaSV"), Set.of("TenKhoa")), fList));
        assertTrue(baoDongAlgorithm.kiemTraSuyDien(new PhuThuocHam(Set.of("MaLHP"), Set.of("ToaNha")), fList));
        assertTrue(baoDongAlgorithm.kiemTraSuyDien(new PhuThuocHam(Set.of("MaSV", "MaLHP"), Set.of("TenGV", "SoTC", "TenKhoa", "Diem")), fList));

        assertFalse(baoDongAlgorithm.kiemTraSuyDien(new PhuThuocHam(Set.of("MaSV"), Set.of("TenHP")), fList));
        assertFalse(baoDongAlgorithm.kiemTraSuyDien(new PhuThuocHam(Set.of("MaLHP"), Set.of("Diem")), fList));
        assertFalse(baoDongAlgorithm.kiemTraSuyDien(new PhuThuocHam(Set.of("MaKhoa"), Set.of("TenNganh")), fList));
    }

    @Test
    @DisplayName("3. Kiểm tra tìm tất cả 3 khóa tối thiểu của CSDL Quản lý Đào tạo")
    void testTimTatCaKhoaThucTe() {
        KeyResponse keyResp = timKhoaAlgorithm.timTatCaKhoa(universe, fList);
        List<Set<String>> keys = keyResp.getKeys();

        // Đề bài và kết quả mong đợi xác nhận có đúng 3 khóa
        assertEquals(3, keys.size(), "Hệ thống phải tìm ra đúng 3 khóa tối thiểu");

        Set<String> k1 = Set.of("MaSV", "MaLHP");
        Set<String> k2 = Set.of("MaSV", "MaGV", "HocKy", "Thu", "TietBD");
        Set<String> k3 = Set.of("MaSV", "MaPhong", "HocKy", "Thu", "TietBD");

        assertTrue(keys.contains(k1), "Phải chứa khóa {MaSV, MaLHP}");
        assertTrue(keys.contains(k2), "Phải chứa khóa {MaSV, MaGV, HocKy, Thu, TietBD}");
        assertTrue(keys.contains(k3), "Phải chứa khóa {MaSV, MaPhong, HocKy, Thu, TietBD}");

        // Thuộc tính nguồn L bắt buộc phải có MaSV
        assertTrue(keyResp.getAttributeGroups().get("L").contains("MaSV"));
    }

    @Test
    @DisplayName("4. Kiểm tra dạng chuẩn của lược đồ U (kết quả mong đợi: 1NF)")
    void testDangChuanThucTe() {
        NormalFormResponse nfResp = chuanHoaAlgorithm.kiemTraDangChuan(universe, fList);

        assertEquals("1NF", nfResp.getHighestNormalForm(), "Lược đồ tổng hợp U chỉ đạt 1NF do có phụ thuộc bộ phận vào khóa");
        assertFalse(nfResp.isIs2NF());
        assertFalse(nfResp.isIs3NF());
        assertFalse(nfResp.isIsBCNF());

        // Có các vi phạm 2NF do MaSV -> ..., MaLHP -> ..., MaGV -> ..., MaPhong -> ...
        assertFalse(nfResp.getViolations2NF().isEmpty());
    }

    @Test
    @DisplayName("5. Kiểm tra phân rã 3NF bảo toàn phụ thuộc và lossless join")
    void testPhanRa3NFThucTe() {
        DecompositionResponse decompResp = chuanHoaAlgorithm.phanRa3NF(universe, fList);
        List<Set<String>> subSchemas = decompResp.getSubSchemas();

        assertNotNull(subSchemas);
        assertFalse(subSchemas.isEmpty());

        // Kiểm tra tính bảo toàn thông tin (chứa ít nhất 1 khóa của U)
        boolean hasKey = false;
        Set<String> k1 = Set.of("MaSV", "MaLHP");
        for (Set<String> rel : subSchemas) {
            if (rel.containsAll(k1)) {
                hasKey = true;
                break;
            }
        }
        assertTrue(hasKey, "Các quan hệ con phân rã 3NF phải chứa ít nhất một khóa để đảm bảo Lossless Join");
    }
}
