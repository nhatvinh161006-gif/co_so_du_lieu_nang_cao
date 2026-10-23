package com.doan10.db_normalization_tool.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Lớp HocPhan - Trích xuất từ thực thể HOCPHAN trên sơ đồ EER
 * Thể hiện:
 * 1. OID: id tự sinh bằng @Id @GeneratedValue
 * 2. Các thuộc tính trích xuất từ EER: maHP, tenHP, soTC
 */
@Entity
public class HocPhan implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id; // OID

    private String maHP;     // Khớp MaHP trên sơ đồ EER
    private String tenHP;    // Khớp TenHP trên sơ đồ EER
    private int soTC;        // Khớp SoTC trên sơ đồ EER

    public HocPhan() {}

    public HocPhan(String maHP, String tenHP, int soTC) {
        this.maHP = maHP;
        this.tenHP = tenHP;
        this.soTC = soTC;
    }

    // --- GETTER & SETTER ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaHP() {
        return maHP;
    }

    public void setMaHP(String maHP) {
        this.maHP = maHP;
    }

    public String getTenHP() {
        return tenHP;
    }

    public void setTenHP(String tenHP) {
        this.tenHP = tenHP;
    }

    public String getTenHocPhan() {
        return tenHP;
    }

    public int getSoTC() {
        return soTC;
    }

    public void setSoTC(int soTC) {
        this.soTC = soTC;
    }

    public int getSoTinChi() {
        return soTC;
    }

    @Override
    public String toString() {
        return "HocPhan[maHP=" + maHP + ", tenHP=" + tenHP + ", soTC=" + soTC + "]";
    }
}
