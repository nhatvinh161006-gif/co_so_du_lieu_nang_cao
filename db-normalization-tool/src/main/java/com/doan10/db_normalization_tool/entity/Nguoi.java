package com.doan10.db_normalization_tool.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Lớp cha Nguoi - Đại diện thông tin người dùng chung
 * Thể hiện:
 * 1. OID (Object Identifier) tự động sinh bằng @Id và @GeneratedValue
 * 2. Quan hệ kế thừa (Inheritance) chiến lược JOINED
 * 3. Thuộc tính trích xuất từ EER: hoTen, ngaySinh
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

    public Nguoi() {}

    public Nguoi(String hoTen, String ngaySinh) {
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
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

    @Override
    public String toString() {
        return "Nguoi[id=" + id + ", hoTen=" + hoTen + ", ngaySinh=" + ngaySinh + "]";
    }
}
