package com.doan10.db_normalization_tool.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Lớp LopHocPhan - Trích xuất từ thực thể LOPHOCPHAN trên sơ đồ EER
 * Thể hiện:
 * 1. OID: id tự sinh bằng @Id @GeneratedValue
 * 2. Mối quan hệ đối tượng: @ManyToOne HocPhan, @ManyToOne GiangVien
 * 3. Phương thức xử lý (Business Methods): kiemTraTrungLich
 */
@Entity
public class LopHocPhan implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id; // OID

    private String maLHP; // Khớp với MaLHP trên sơ đồ EER
    private String hocKy; // Học kỳ (vd: "HK1-2026")
    private int thu;      // Thứ học (2 -> 7)
    private int tietBD;   // Tiết bắt đầu (1, 4, 7, ...)

    @ManyToOne
    private HocPhan hocPhan; // Mối kết hợp MO_LOP từ EER

    @ManyToOne
    private GiangVien giangVien; // Mối kết hợp GIANG_DAY từ EER

    public LopHocPhan() {}

    public LopHocPhan(String maLHP, String hocKy, int thu, int tietBD, HocPhan hocPhan, GiangVien giangVien) {
        this.maLHP = maLHP;
        this.hocKy = hocKy;
        this.thu = thu;
        this.tietBD = tietBD;
        this.hocPhan = hocPhan;
        this.giangVien = giangVien;
    }

    // --- CÁC PHƯƠNG THỨC XỬ LÝ NGHIỆP VỤ (OODBMS Methods) ---

    /**
     * Phương thức kiểm tra trùng lịch học/dạy
     * @param thu Thứ cần kiểm tra
     * @param tietBD Tiết bắt đầu cần kiểm tra
     * @return true nếu cùng thứ và tiết bắt đầu
     */
    public boolean kiemTraTrungLich(int thu, int tietBD) {
        return this.thu == thu && this.tietBD == tietBD;
    }

    public String getThongTinLichHoc() {
        return "Thứ " + this.thu + ", Tiết " + this.tietBD + " (" + this.hocKy + ")";
    }

    // --- GETTER & SETTER ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaLHP() {
        return maLHP;
    }

    public void setMaLHP(String maLHP) {
        this.maLHP = maLHP;
    }

    public String getHocKy() {
        return hocKy;
    }

    public void setHocKy(String hocKy) {
        this.hocKy = hocKy;
    }

    public int getThu() {
        return thu;
    }

    public void setThu(int thu) {
        this.thu = thu;
    }

    public int getTietBD() {
        return tietBD;
    }

    public void setTietBD(int tietBD) {
        this.tietBD = tietBD;
    }

    public HocPhan getHocPhan() {
        return hocPhan;
    }

    public void setHocPhan(HocPhan hocPhan) {
        this.hocPhan = hocPhan;
    }

    public GiangVien getGiangVien() {
        return giangVien;
    }

    public void setGiangVien(GiangVien giangVien) {
        this.giangVien = giangVien;
    }

    @Override
    public String toString() {
        return "LopHocPhan[maLHP=" + maLHP + ", hocPhan=" + (hocPhan != null ? hocPhan.getTenHocPhan() : "null")
                + ", lich=" + getThongTinLichHoc() + "]";
    }
}
