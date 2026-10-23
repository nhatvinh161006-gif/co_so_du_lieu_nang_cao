package com.doan10.db_normalization_tool.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp con SinhVien kế thừa từ Nguoi
 * Thể hiện:
 * 1. Kế thừa (Inheritance) từ lớp cha Nguoi
 * 2. Thuộc tính tập hợp (Collection List<DangKy>)
 * 3. Các phương thức xử lý nghiệp vụ thực tế (Business Methods):
 *    - themDangKy: Thêm môn học
 *    - kiemTraTrungLich: Kiểm tra lịch học mới có trùng với các lớp đã đăng ký không
 *    - tinhDiemTrungBinh: Tính ĐTB học kỳ/tích lũy có trọng số tín chỉ
 *    - tongTinChiTichLuy: Tính tổng tín chỉ của các môn đạt
 *    - xepLoaiHocLuc: Xếp loại học lực đại học
 */
@Entity
public class SinhVien extends Nguoi {
    private static final long serialVersionUID = 1L;

    private String maSV; // Khớp với MaSV trên sơ đồ EER

    // Thuộc tính tập hợp (Collection): Một sinh viên có danh sách nhiều lượt đăng ký lớp học phần
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DangKy> dsDangKy = new ArrayList<>();

    public SinhVien() {
        super();
    }

    public SinhVien(String hoTen, String ngaySinh, String maSV) {
        super(hoTen, ngaySinh);
        this.maSV = maSV;
    }

    // --- CÁC PHƯƠNG THỨC XỬ LÝ NGHIỆP VỤ (OODBMS Encapsulated Methods) ---

    /**
     * Thêm đăng ký lớp học phần mới
     */
    public void themDangKy(DangKy dk) {
        if (this.dsDangKy == null) {
            this.dsDangKy = new ArrayList<>();
        }
        this.dsDangKy.add(dk);
    }

    /**
     * Phương thức nghiệp vụ: Kiểm tra lớp học phần mới có bị trùng lịch học
     * với bất kỳ môn nào sinh viên đã đăng ký trước đó hay không
     */
    public boolean kiemTraTrungLich(LopHocPhan lopMoi) {
        if (this.dsDangKy == null || lopMoi == null) return false;
        for (DangKy dk : this.dsDangKy) {
            LopHocPhan lhpDaDangKy = dk.getLopHocPhan();
            if (lhpDaDangKy != null && lhpDaDangKy.kiemTraTrungLich(lopMoi.getThu(), lopMoi.getTietBD())) {
                return true; // Trùng lịch học
            }
        }
        return false;
    }

    /**
     * Phương thức nghiệp vụ: Tính Điểm Trung Bình tích lũy có trọng số theo số tín chỉ
     */
    public double tinhDiemTrungBinh() {
        if (this.dsDangKy == null || this.dsDangKy.isEmpty()) return 0.0;
        double tongDiemTinChi = 0.0;
        int tongSoTinChi = 0;

        for (DangKy dk : this.dsDangKy) {
            int tc = 1; // Mặc định 1 tín chỉ nếu chưa có lớp
            if (dk.getLopHocPhan() != null && dk.getLopHocPhan().getHocPhan() != null) {
                tc = dk.getLopHocPhan().getHocPhan().getSoTC();
            }
            tongDiemTinChi += dk.getDiem() * tc;
            tongSoTinChi += tc;
        }

        if (tongSoTinChi == 0) return 0.0;
        return Math.round((tongDiemTinChi / tongSoTinChi) * 100.0) / 100.0;
    }

    /**
     * Phương thức nghiệp vụ: Tính tổng số tín chỉ tích lũy đạt được (điểm >= 4.0)
     */
    public int tongTinChiTichLuy() {
        if (this.dsDangKy == null) return 0;
        int tongTC = 0;
        for (DangKy dk : this.dsDangKy) {
            if (dk.laMonDat() && dk.getLopHocPhan() != null && dk.getLopHocPhan().getHocPhan() != null) {
                tongTC += dk.getLopHocPhan().getHocPhan().getSoTC();
            }
        }
        return tongTC;
    }

    /**
     * Phương thức nghiệp vụ: Xếp loại học lực đại học theo chuẩn thang điểm 10
     */
    public String xepLoaiHocLuc() {
        double dtb = tinhDiemTrungBinh();
        if (dtb >= 9.0) return "Xuất sắc";
        if (dtb >= 8.0) return "Giỏi";
        if (dtb >= 6.5) return "Khá";
        if (dtb >= 5.0) return "Trung bình";
        return "Yếu";
    }

    // --- GETTER & SETTER ---
    public String getMaSV() {
        return maSV;
    }

    public void setMaSV(String maSV) {
        this.maSV = maSV;
    }

    public List<DangKy> getDsDangKy() {
        return dsDangKy;
    }

    public void setDsDangKy(List<DangKy> dsDangKy) {
        this.dsDangKy = dsDangKy;
    }

    @Override
    public String toString() {
        return "SinhVien[maSV=" + maSV + ", hoTen=" + getHoTen() + ", ĐTB=" + tinhDiemTrungBinh() + "]";
    }
}
