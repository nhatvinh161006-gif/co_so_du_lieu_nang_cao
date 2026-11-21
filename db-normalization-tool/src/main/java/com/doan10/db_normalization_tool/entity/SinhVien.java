package com.doan10.db_normalization_tool.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp con SinhVien kế thừa từ Nguoi
 * Thể hiện:
 * 1. Kế thừa (Inheritance) từ lớp cha Nguoi
 * 2. Thuộc tính tập hợp (Collection List<DangKyMon>)
 * 3. Các phương thức xử lý nghiệp vụ thực tế (Business Methods):
 *    - themDangKy: Thêm đăng ký môn học
 *    - kiemTraTrungLich: Kiểm tra lịch học mới có trùng với các lớp đã đăng ký không
 *    - daDatMonTienQuyet: Kiểm tra sinh viên đã vượt qua môn học tiên quyết chưa
 *    - tinhDiemTrungBinh: Tính ĐTB học kỳ/tích lũy có trọng số tín chỉ
 *    - tongTinChiTichLuy: Tính tổng tín chỉ của các môn đạt
 *    - xepLoaiHocLuc: Xếp loại học lực đại học
 */
@Entity
public class SinhVien extends Nguoi {
    private static final long serialVersionUID = 1L;

    private String maSV; // Khớp với MaSV trên sơ đồ EER (vd: "SV001")

    // Thuộc tính tập hợp (Collection): Một sinh viên có danh sách nhiều lượt đăng ký môn học
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DangKyMon> dsDangKy = new ArrayList<>();

    public SinhVien() {
        super();
    }

    public SinhVien(String hoTen, String ngaySinh, String maSV) {
        super(hoTen, ngaySinh);
        this.maSV = maSV;
    }

    public SinhVien(String hoTen, String ngaySinh, String gioiTinh, String maSV) {
        super(hoTen, ngaySinh, gioiTinh);
        this.maSV = maSV;
    }

    // --- CÁC PHƯƠNG THỨC XỬ LÝ NGHIỆP VỤ (OODBMS Encapsulated Methods) ---

    /**
     * Thêm đăng ký lớp học phần mới
     */
    public void themDangKy(DangKyMon dk) {
        if (this.dsDangKy == null) {
            this.dsDangKy = new ArrayList<>();
        }
        this.dsDangKy.add(dk);
        if (dk.getSinhVien() != this) {
            dk.setSinhVien(this);
        }
    }

    /**
     * Phương thức nghiệp vụ: Kiểm tra lớp học phần mới có bị trùng lịch học
     * với bất kỳ môn nào sinh viên đã đăng ký trước đó hay không
     */
    public boolean kiemTraTrungLich(LopHocPhan lopMoi) {
        if (this.dsDangKy == null || lopMoi == null) return false;
        for (DangKyMon dk : this.dsDangKy) {
            LopHocPhan lhpDaDangKy = dk.getLopHocPhan();
            if (lhpDaDangKy != null && lhpDaDangKy.kiemTraTrungLich(lopMoi.getThu(), lopMoi.getTietBD())) {
                return true; // Trùng lịch học
            }
        }
        return false;
    }

    /**
     * Phương thức nghiệp vụ: Kiểm tra sinh viên đã thỏa mãn điều kiện tiên quyết
     * của học phần này hay chưa (đã học môn tiên quyết và có điểm >= 4.0)
     */
    public boolean daDatMonTienQuyet(HocPhan hp) {
        if (hp == null || !hp.coMonTienQuyet()) {
            return true; // Không có môn tiên quyết -> Luôn thỏa mãn
        }
        HocPhan tq = hp.getHocPhanTienQuyet();
        if (this.dsDangKy == null) return false;
        for (DangKyMon dk : this.dsDangKy) {
            if (dk.getLopHocPhan() != null && dk.getLopHocPhan().getHocPhan() != null) {
                if (dk.getLopHocPhan().getHocPhan().getMaHP().equalsIgnoreCase(tq.getMaHP())) {
                    return dk.laMonDat();
                }
            }
        }
        return false; // Chưa học hoặc chưa đạt môn tiên quyết
    }

    /**
     * Phương thức nghiệp vụ: Tính Điểm trung bình tích lũy hệ 10 có trọng số tín chỉ
     * DTB = Tổng(Điểm tổng kết * Số tín chỉ) / Tổng(Số tín chỉ)
     */
    public double tinhDiemTrungBinh() {
        if (this.dsDangKy == null || this.dsDangKy.isEmpty()) return 0.0;
        double tongDiemHeSo = 0.0;
        int tongTinChi = 0;

        for (DangKyMon dk : this.dsDangKy) {
            if (dk.getLopHocPhan() != null && dk.getLopHocPhan().getHocPhan() != null) {
                int tinChi = dk.getLopHocPhan().getHocPhan().getSoTC();
                tongDiemHeSo += dk.tinhDiemTongKet() * tinChi;
                tongTinChi += tinChi;
            }
        }
        if (tongTinChi == 0) return 0.0;
        return Math.round((tongDiemHeSo / tongTinChi) * 100.0) / 100.0;
    }

    /**
     * Phương thức nghiệp vụ: Tính tổng số tín chỉ tích lũy đạt được (các môn có điểm >= 4.0)
     */
    public int tongTinChiTichLuy() {
        if (this.dsDangKy == null) return 0;
        int tichLuy = 0;
        for (DangKyMon dk : this.dsDangKy) {
            if (dk.laMonDat() && dk.getLopHocPhan() != null && dk.getLopHocPhan().getHocPhan() != null) {
                tichLuy += dk.getLopHocPhan().getHocPhan().getSoTC();
            }
        }
        return tichLuy;
    }

    /**
     * Phương thức nghiệp vụ: Xếp loại học lực theo quy chế đào tạo
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

    public List<DangKyMon> getDsDangKy() {
        return dsDangKy;
    }

    public void setDsDangKy(List<DangKyMon> dsDangKy) {
        this.dsDangKy = dsDangKy;
    }

    @Override
    public String toString() {
        return "SinhVien[maSV=" + maSV + ", hoTen=" + getHoTen() + ", gioiTinh=" + getGioiTinh() + 
               ", DTB=" + tinhDiemTrungBinh() + ", TichLuy=" + tongTinChiTichLuy() + "TC, XepLoai=" + xepLoaiHocLuc() + "]";
    }
}
