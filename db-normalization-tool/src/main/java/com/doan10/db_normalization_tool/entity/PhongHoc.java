package com.doan10.db_normalization_tool.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Lớp PhongHoc - Đại diện cho phòng học được phân bổ cho lớp học phần
 * Thể hiện:
 * 1. OID: id tự sinh bằng @Id @GeneratedValue của ObjectDB
 * 2. Thuộc tính nghiệp vụ: maPhong, toaNha, sucChua, loaiPhong
 */
@Entity
public class PhongHoc implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id; // Định danh đối tượng (OID)

    private String maPhong;   // Ví dụ: A101, B202, LAB01
    private String toaNha;    // Tòa nhà: Tòa A, Tòa B, Tòa C
    private int sucChua;      // Sức chứa chỗ ngồi (ví dụ: 60)
    private String loaiPhong; // Loại phòng: "Lý thuyết" hoặc "Thực hành"

    public PhongHoc() {}

    public PhongHoc(String maPhong, String toaNha, int sucChua, String loaiPhong) {
        this.maPhong = maPhong;
        this.toaNha = toaNha;
        this.sucChua = sucChua;
        this.loaiPhong = loaiPhong;
    }

    // --- GETTER & SETTER ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getToaNha() {
        return toaNha;
    }

    public void setToaNha(String toaNha) {
        this.toaNha = toaNha;
    }

    public int getSucChua() {
        return sucChua;
    }

    public void setSucChua(int sucChua) {
        this.sucChua = sucChua;
    }

    public String getLoaiPhong() {
        return loaiPhong;
    }

    public void setLoaiPhong(String loaiPhong) {
        this.loaiPhong = loaiPhong;
    }

    @Override
    public String toString() {
        return "PhongHoc[id=" + id + ", maPhong=" + maPhong + ", toaNha=" + toaNha + 
               ", sucChua=" + sucChua + ", loaiPhong=" + loaiPhong + "]";
    }
}
