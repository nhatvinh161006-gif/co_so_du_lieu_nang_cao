package com.doan10.db_normalization_tool.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity LichSuChuanHoa - Lưu trữ lịch sử chạy thuật toán chuẩn hóa CSDL trong ObjectDB
 * Thể hiện:
 * 1. OID: id tự động sinh bằng @Id @GeneratedValue
 * 2. Lưu trữ linh hoạt các tham số đầu vào và kết quả thuật toán của Người 2 và Người 3
 */
@Entity
public class LichSuChuanHoa implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id; // Định danh đối tượng OID tự động sinh của ObjectDB

    private String thoiGianChay;       // Thời điểm chạy (định dạng "yyyy-MM-dd HH:mm:ss")
    private String tapThuocTinhU;      // Tập thuộc tính đầu vào U (vd: "A,B,C,D,E,G,H")
    private String tapPhuThuocHamF;    // Tập phụ thuộc hàm đầu vào F (vd: "AB->C, B->D, CD->E, E->A")
    private String loaiThuatToan;      // BAO_DONG, PHU_TOI_TIEU, TIM_KHOA, DANG_CHUAN, PHAN_RA_3NF, PHAN_RA_BCNF
    private String dauVaoBoSung;       // Đầu vào phụ (vd: tập X = "A,B" khi tính bao đóng X+)
    
    @Lob
    private String ketQuaChiTiet;     // Chi tiết từng bước giải thuật và kết quả cuối cùng

    private long thoiGianThucThiMs;    // Thời gian tính toán giải thuật (mili giây)
    private boolean thanhCong;         // Trạng thái chạy thành công hay thất bại

    public LichSuChuanHoa() {}

    public LichSuChuanHoa(String thoiGianChay, String tapThuocTinhU, String tapPhuThuocHamF,
                          String loaiThuatToan, String dauVaoBoSung, String ketQuaChiTiet,
                          long thoiGianThucThiMs, boolean thanhCong) {
        this.thoiGianChay = thoiGianChay;
        this.tapThuocTinhU = tapThuocTinhU;
        this.tapPhuThuocHamF = tapPhuThuocHamF;
        this.loaiThuatToan = loaiThuatToan;
        this.dauVaoBoSung = dauVaoBoSung;
        this.ketQuaChiTiet = ketQuaChiTiet;
        this.thoiGianThucThiMs = thoiGianThucThiMs;
        this.thanhCong = thanhCong;
    }

    // --- GETTER & SETTER ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getThoiGianChay() {
        return thoiGianChay;
    }

    public void setThoiGianChay(String thoiGianChay) {
        this.thoiGianChay = thoiGianChay;
    }

    public String getTapThuocTinhU() {
        return tapThuocTinhU;
    }

    public void setTapThuocTinhU(String tapThuocTinhU) {
        this.tapThuocTinhU = tapThuocTinhU;
    }

    public String getTapPhuThuocHamF() {
        return tapPhuThuocHamF;
    }

    public void setTapPhuThuocHamF(String tapPhuThuocHamF) {
        this.tapPhuThuocHamF = tapPhuThuocHamF;
    }

    public String getLoaiThuatToan() {
        return loaiThuatToan;
    }

    public void setLoaiThuatToan(String loaiThuatToan) {
        this.loaiThuatToan = loaiThuatToan;
    }

    public String getDauVaoBoSung() {
        return dauVaoBoSung;
    }

    public void setDauVaoBoSung(String dauVaoBoSung) {
        this.dauVaoBoSung = dauVaoBoSung;
    }

    public String getKetQuaChiTiet() {
        return ketQuaChiTiet;
    }

    public void setKetQuaChiTiet(String ketQuaChiTiet) {
        this.ketQuaChiTiet = ketQuaChiTiet;
    }

    public long getThoiGianThucThiMs() {
        return thoiGianThucThiMs;
    }

    public void setThoiGianThucThiMs(long thoiGianThucThiMs) {
        this.thoiGianThucThiMs = thoiGianThucThiMs;
    }

    public boolean isThanhCong() {
        return thanhCong;
    }

    public void setThanhCong(boolean thanhCong) {
        this.thanhCong = thanhCong;
    }

    @Override
    public String toString() {
        return "LichSuChuanHoa[id=" + id + ", thoiGian=" + thoiGianChay + 
               ", thuatToan=" + loaiThuatToan + ", U=" + tapThuocTinhU + 
               ", thoiGianChay=" + thoiGianThucThiMs + "ms, thanhCong=" + thanhCong + "]";
    }
}
