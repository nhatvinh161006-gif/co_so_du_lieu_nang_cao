package com.doan10.db_normalization_tool.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Lớp cha Nguoi - Đại diện thông tin người dùng chung
 * Thể hiện:
 * 1. OID (Object Identifier) tự động sinh bằng @Id và @GeneratedValue
 * 2. Quan hệ kế thừa (Inheritance) chiến lược JOINED
 * 3. Thuộc tính trích xuất từ EER và Công việc.docx: hoTen, ngaySinh, gioiTinh
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Nguoi implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id; // Định danh đối tượng (OID)

    private String hoTen;
    private String ngaySinh; // Khớp với thuộc tính NgaySinh trên sơ đồ EER
    private String gioiTinh; // Khớp với yêu cầu Công việc.docx: "Nam" hoặc "Nữ"

    public Nguoi() {}

    public Nguoi(String hoTen, String ngaySinh) {
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = "Nam"; // Mặc định
    }

    public Nguoi(String hoTen, String ngaySinh, String gioiTinh) {
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(String ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    @Override
    public String toString() {
        return "Nguoi[id=" + id + ", hoTen=" + hoTen + ", ngaySinh=" + ngaySinh + ", gioiTinh=" + gioiTinh + "]";
    }
}
