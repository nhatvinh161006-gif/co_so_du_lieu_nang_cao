package com.doan10.db_normalization_tool.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp con GiangVien kế thừa từ Nguoi
 * Thể hiện:
 * 1. Kế thừa (Inheritance)
 * 2. Thuộc tính tập hợp (Collection List<LopHocPhan>)
 * 3. Phương thức xử lý (Business Methods): phanCongLopDay, tongSoLopPhuTrach
 */
@Entity
public class GiangVien extends Nguoi {
    private static final long serialVersionUID = 1L;

    private String maGV;  // Khớp với MaGV trên sơ đồ EER
    private String hocVi; // Khớp với HocVi trên sơ đồ EER (Tiến sĩ, Thạc sĩ, ...)

    // Thuộc tính tập hợp: Một giảng viên phụ trách danh sách nhiều lớp học phần
    @OneToMany(mappedBy = "giangVien", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LopHocPhan> dsLopGiangDay = new ArrayList<>();

    public GiangVien() {
        super();
    }

    public GiangVien(String hoTen, String ngaySinh, String maGV, String hocVi) {
        super(hoTen, ngaySinh);
        this.maGV = maGV;
        this.hocVi = hocVi;
    }

    // --- CÁC PHƯƠNG THỨC XỬ LÝ NGHIỆP VỤ (OODBMS Methods) ---

    /**
     * Phân công giảng dạy một lớp học phần mới
     */
    public void phanCongLopDay(LopHocPhan lhp) {
        if (this.dsLopGiangDay == null) {
            this.dsLopGiangDay = new ArrayList<>();
        }
        this.dsLopGiangDay.add(lhp);
        lhp.setGiangVien(this);
    }

    /**
     * Thống kê tổng số lớp học phần giảng viên đang phụ trách
     */
    public int tongSoLopPhuTrach() {
        return this.dsLopGiangDay != null ? this.dsLopGiangDay.size() : 0;
    }

    // --- GETTER & SETTER ---
    public String getMaGV() {
        return maGV;
    }

    public void setMaGV(String maGV) {
        this.maGV = maGV;
    }

    public String getHocVi() {
        return hocVi;
    }

    public void setHocVi(String hocVi) {
        this.hocVi = hocVi;
    }

    public List<LopHocPhan> getDsLopGiangDay() {
        return dsLopGiangDay;
    }

    public void setDsLopGiangDay(List<LopHocPhan> dsLopGiangDay) {
        this.dsLopGiangDay = dsLopGiangDay;
    }

    @Override
    public String toString() {
        return "GiangVien[id=" + getId() + ", maGV=" + maGV + ", hoTen=" + getHoTen() + ", hocVi=" + hocVi + "]";
    }
}
