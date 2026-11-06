package com.doan10.db_normalization_tool.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp LopHocPhan - Trích xuất từ thực thể LOPHOCPHAN trên sơ đồ EER & Công việc.docx
 * Thể hiện:
 * 1. OID: id tự sinh bằng @Id @GeneratedValue
 * 2. Mối quan hệ đối tượng trực tiếp:
 *    - @ManyToOne HocPhan (môn học mở lớp)
 *    - @ManyToOne GiangVien (giảng viên phụ trách)
 *    - @ManyToOne PhongHoc (phòng học được phân bổ)
 * 3. Thuộc tính tập hợp (Collection): List<DangKyMon> dsDangKy
 * 4. Phương thức xử lý (Business Methods): kiemTraTrungLich, themDangKy, getThongTinLichHoc
 */
@Entity
public class LopHocPhan implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id; // OID

    private String maLHP; // Khớp với MaLHP trên sơ đồ EER (vd: "LHP_CSDL_01")
    private String hocKy; // Học kỳ (vd: "HK1-2026")
    private int thu;      // Thứ học (2 -> 7)
    private int tietBD;   // Tiết bắt đầu (1, 4, 7, ...)

    @ManyToOne
    private HocPhan hocPhan; // Mối kết hợp MO_LOP từ EER

    @ManyToOne
    private GiangVien giangVien; // Mối kết hợp GIANG_DAY từ EER

    @ManyToOne
    private PhongHoc phongHoc; // Tham chiếu trực tiếp đến đối tượng PhongHoc

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DangKyMon> dsDangKy = new ArrayList<>(); // Danh sách sinh viên đăng ký

    public LopHocPhan() {}

    public LopHocPhan(String maLHP, String hocKy, int thu, int tietBD, 
                      HocPhan hocPhan, GiangVien giangVien, PhongHoc phongHoc) {
        this.maLHP = maLHP;
        this.hocKy = hocKy;
        this.thu = thu;
        this.tietBD = tietBD;
        this.hocPhan = hocPhan;
        this.giangVien = giangVien;
        this.phongHoc = phongHoc;
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

    /**
     * Thêm sinh viên đăng ký vào lớp học phần
     */
    public void themDangKy(DangKyMon dk) {
        if (this.dsDangKy == null) {
            this.dsDangKy = new ArrayList<>();
        }
        this.dsDangKy.add(dk);
        if (dk.getLopHocPhan() != this) {
            dk.setLopHocPhan(this);
        }
    }

    /**
     * Lấy sĩ số hiện tại của lớp
     */
    public int getSiSoHienTai() {
        return (this.dsDangKy != null) ? this.dsDangKy.size() : 0;
    }

    public String getThongTinLichHoc() {
        String tenPhong = (phongHoc != null) ? phongHoc.getMaPhong() : "Chưa xếp";
        return "Thứ " + this.thu + ", Tiết " + this.tietBD + " | Phòng: " + tenPhong + " (" + this.hocKy + ")";
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

    public PhongHoc getPhongHoc() {
        return phongHoc;
    }

    public void setPhongHoc(PhongHoc phongHoc) {
        this.phongHoc = phongHoc;
    }

    public List<DangKyMon> getDsDangKy() {
        return dsDangKy;
    }

    public void setDsDangKy(List<DangKyMon> dsDangKy) {
        this.dsDangKy = dsDangKy;
    }

    @Override
    public String toString() {
        String tenHP = (hocPhan != null) ? hocPhan.getTenHP() : "N/A";
        String tenGV = (giangVien != null) ? giangVien.getHoTen() : "N/A";
        String tenPhong = (phongHoc != null) ? phongHoc.getMaPhong() : "N/A";
        return "LopHocPhan[maLHP=" + maLHP + ", hocPhan=" + tenHP + ", giangVien=" + tenGV + 
               ", phong=" + tenPhong + ", siSo=" + getSiSoHienTai() + "]";
    }
}
