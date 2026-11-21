package com.doan10.db_normalization_tool.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp HocPhan - Trích xuất từ thực thể HOCPHAN trên sơ đồ EER & Công việc.docx
 * Thể hiện:
 * 1. OID: id tự sinh bằng @Id @GeneratedValue
 * 2. Các thuộc tính trích xuất từ EER: maHP, tenHP, soTC
 * 3. Quan hệ tự liên kết (Self-referencing): hocPhanTienQuyet (Điều kiện tiên quyết)
 * 4. Thuộc tính tập hợp (Collection): List<LopHocPhan> dsLopHocPhan
 */
@Entity
public class HocPhan implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id; // OID

    private String maHP;     // Khớp MaHP trên sơ đồ EER (vd: "CSDL201")
    private String tenHP;    // Khớp TenHP trên sơ đồ EER (vd: "Cơ sở dữ liệu nâng cao")
    private int soTC;        // Khớp SoTC trên sơ đồ EER (vd: 3)

    // Quan hệ tự liên kết: Môn học tiên quyết (Prerequisite Course)
    @ManyToOne
    private HocPhan hocPhanTienQuyet;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LopHocPhan> dsLopHocPhan = new ArrayList<>(); // Danh sách lớp học phần mở cho môn học này

    public HocPhan() {}

    public HocPhan(String maHP, String tenHP, int soTC) {
        this.maHP = maHP;
        this.tenHP = tenHP;
        this.soTC = soTC;
    }

    public HocPhan(String maHP, String tenHP, int soTC, HocPhan hocPhanTienQuyet) {
        this.maHP = maHP;
        this.tenHP = tenHP;
        this.soTC = soTC;
        this.hocPhanTienQuyet = hocPhanTienQuyet;
    }

    // --- CÁC PHƯƠNG THỨC NGHIỆP VỤ ---
    public boolean coMonTienQuyet() {
        return this.hocPhanTienQuyet != null;
    }

    public void themLopHocPhan(LopHocPhan lhp) {
        if (this.dsLopHocPhan == null) {
            this.dsLopHocPhan = new ArrayList<>();
        }
        this.dsLopHocPhan.add(lhp);
        if (lhp.getHocPhan() != this) {
            lhp.setHocPhan(this);
        }
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

    public HocPhan getHocPhanTienQuyet() {
        return hocPhanTienQuyet;
    }

    public void setHocPhanTienQuyet(HocPhan hocPhanTienQuyet) {
        this.hocPhanTienQuyet = hocPhanTienQuyet;
    }

    public List<LopHocPhan> getDsLopHocPhan() {
        return dsLopHocPhan;
    }

    public void setDsLopHocPhan(List<LopHocPhan> dsLopHocPhan) {
        this.dsLopHocPhan = dsLopHocPhan;
    }

    @Override
    public String toString() {
        String tq = (hocPhanTienQuyet != null) ? hocPhanTienQuyet.getMaHP() : "Không";
        return "HocPhan[maHP=" + maHP + ", tenHP=" + tenHP + ", soTC=" + soTC + 
               ", tienQuyet=" + tq + ", soLopDaMo=" + ((dsLopHocPhan != null) ? dsLopHocPhan.size() : 0) + "]";
    }
}
