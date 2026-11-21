package com.doan10.db_normalization_tool.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Lớp liên kết DangKyMon - Trích xuất từ mối kết hợp DANG_KY giữa SINHVIEN và LOPHOCPHAN
 * Thể hiện:
 * 1. OID: id tự sinh bằng @Id @GeneratedValue
 * 2. Mối quan hệ đối tượng: @ManyToOne SinhVien, @ManyToOne LopHocPhan
 * 3. Thuộc tính nghiệp vụ: ngayDangKy, diemChuyenCan (10%), diemGiuaKy (30%), diemCuoiKy (60%)
 * 4. Phương thức xử lý (Encapsulated OOP Methods):
 *    - tinhDiemTongKet: Tính điểm tổng kết theo trọng số
 *    - laMonDat: Kiểm tra qua môn (>= 4.0)
 *    - getDiemChu: Quy đổi sang hệ chữ A, B, C, D, F
 */
@Entity
public class DangKyMon implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id; // OID

    @ManyToOne
    private SinhVien sinhVien; // Tham chiếu trực tiếp đến đối tượng SinhVien

    @ManyToOne
    private LopHocPhan lopHocPhan; // Tham chiếu trực tiếp đến đối tượng LopHocPhan

    private String ngayDangKy; // Ngày đăng ký môn học (vd: "2026-09-05")

    private double diemChuyenCan; // Hệ số 10% (0.1)
    private double diemGiuaKy;    // Hệ số 30% (0.3)
    private double diemCuoiKy;    // Hệ số 60% (0.6)

    public DangKyMon() {}

    public DangKyMon(SinhVien sinhVien, LopHocPhan lopHocPhan, String ngayDangKy, 
                     double diemChuyenCan, double diemGiuaKy, double diemCuoiKy) {
        this.sinhVien = sinhVien;
        this.lopHocPhan = lopHocPhan;
        this.ngayDangKy = ngayDangKy;
        this.diemChuyenCan = diemChuyenCan;
        this.diemGiuaKy = diemGiuaKy;
        this.diemCuoiKy = diemCuoiKy;
    }

    public DangKyMon(LopHocPhan lopHocPhan, double diemTongKet) {
        this.lopHocPhan = lopHocPhan;
        this.diemChuyenCan = diemTongKet;
        this.diemGiuaKy = diemTongKet;
        this.diemCuoiKy = diemTongKet;
        this.ngayDangKy = "2026-09-01";
    }

    // --- CÁC PHƯƠNG THỨC XỬ LÝ NGHIỆP VỤ (OODBMS Encapsulated Methods) ---

    /**
     * Tính điểm tổng kết môn học theo công thức:
     * DiemTK = 10% Chuyên Cần + 30% Giữa Kỳ + 60% Cuối Kỳ
     */
    public double tinhDiemTongKet() {
        double dtb = (this.diemChuyenCan * 0.1) + (this.diemGiuaKy * 0.3) + (this.diemCuoiKy * 0.6);
        return Math.round(dtb * 10.0) / 10.0;
    }

    /**
     * Phương thức tương thích trả về điểm tổng kết
     */
    public double getDiem() {
        return tinhDiemTongKet();
    }

    /**
     * Kiểm tra sinh viên có đạt (qua môn) học phần này hay không
     * Quy chế tín chỉ đại học: điểm tổng kết >= 4.0 là đạt
     */
    public boolean laMonDat() {
        return tinhDiemTongKet() >= 4.0;
    }

    /**
     * Quy đổi điểm hệ 10 sang thang điểm chữ (A, B, C, D, F)
     */
    public String getDiemChu() {
        double dtb = tinhDiemTongKet();
        if (dtb >= 8.5) return "A";
        if (dtb >= 7.0) return "B";
        if (dtb >= 5.5) return "C";
        if (dtb >= 4.0) return "D";
        return "F";
    }

    // --- GETTER & SETTER ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SinhVien getSinhVien() {
        return sinhVien;
    }

    public void setSinhVien(SinhVien sinhVien) {
        this.sinhVien = sinhVien;
    }

    public LopHocPhan getLopHocPhan() {
        return lopHocPhan;
    }

    public void setLopHocPhan(LopHocPhan lopHocPhan) {
        this.lopHocPhan = lopHocPhan;
    }

    public String getNgayDangKy() {
        return ngayDangKy;
    }

    public void setNgayDangKy(String ngayDangKy) {
        this.ngayDangKy = ngayDangKy;
    }

    public double getDiemChuyenCan() {
        return diemChuyenCan;
    }

    public void setDiemChuyenCan(double diemChuyenCan) {
        this.diemChuyenCan = diemChuyenCan;
    }

    public double getDiemGiuaKy() {
        return diemGiuaKy;
    }

    public void setDiemGiuaKy(double diemGiuaKy) {
        this.diemGiuaKy = diemGiuaKy;
    }

    public double getDiemCuoiKy() {
        return diemCuoiKy;
    }

    public void setDiemCuoiKy(double diemCuoiKy) {
        this.diemCuoiKy = diemCuoiKy;
    }

    @Override
    public String toString() {
        String tenLHP = (lopHocPhan != null) ? lopHocPhan.getMaLHP() : "N/A";
        String tenSV = (sinhVien != null) ? sinhVien.getHoTen() : "N/A";
        return "DangKyMon[id=" + id + ", sv=" + tenSV + ", lhp=" + tenLHP + 
               ", diemTK=" + tinhDiemTongKet() + " (" + getDiemChu() + ")]";
    }
}
