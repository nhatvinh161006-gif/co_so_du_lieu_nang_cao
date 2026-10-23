package com.doan10.db_normalization_tool.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Lớp DangKy - Trích xuất từ mối kết hợp DANG_KY giữa SINHVIEN và LOPHOCPHAN
 * Thể hiện:
 * 1. OID: id tự sinh bằng @Id @GeneratedValue
 * 2. Mối quan hệ đối tượng: @ManyToOne LopHocPhan
 * 3. Thuộc tính trích xuất từ EER: diem
 * 4. Phương thức xử lý: laMonDat, getDiemChu
 */
@Entity
public class DangKy implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id; // OID

    @ManyToOne
    private LopHocPhan lopHocPhan; // Tham chiếu đến Lớp học phần đăng ký

    private double diem; // Khớp với thuộc tính Diem trên sơ đồ EER

    public DangKy() {}

    public DangKy(LopHocPhan lopHocPhan, double diem) {
        this.lopHocPhan = lopHocPhan;
        this.diem = diem;
    }

    // --- CÁC PHƯƠNG THỨC XỬ LÝ NGHIỆP VỤ (OODBMS Methods) ---

    /**
     * Kiểm tra sinh viên có đạt (qua môn) học phần này hay không
     * Quy chế đại học: điểm tổng kết >= 4.0 là đạt
     */
    public boolean laMonDat() {
        return this.diem >= 4.0;
    }

    /**
     * Quy đổi điểm hệ 10 sang thang điểm chữ (A, B, C, D, F)
     */
    public String getDiemChu() {
        if (diem >= 8.5) return "A";
        if (diem >= 7.0) return "B";
        if (diem >= 5.5) return "C";
        if (diem >= 4.0) return "D";
        return "F";
    }

    // --- GETTER & SETTER ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LopHocPhan getLopHocPhan() {
        return lopHocPhan;
    }

    public void setLopHocPhan(LopHocPhan lopHocPhan) {
        this.lopHocPhan = lopHocPhan;
    }

    public double getDiem() {
        return diem;
    }

    public void setDiem(double diem) {
        this.diem = diem;
    }

    public double getDiemSo() {
        return diem;
    }

    @Override
    public String toString() {
        return "DangKy[id=" + id + ", lop=" + (lopHocPhan != null ? lopHocPhan.getMaLHP() : "null")
                + ", diem=" + diem + " (" + getDiemChu() + ")]";
    }
}
