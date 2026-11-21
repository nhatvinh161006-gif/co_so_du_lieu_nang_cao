package com.doan10.db_normalization_tool.dto;

/**
 * DTO nhận dữ liệu gửi lên từ Người 2 (Backend thuật toán) hoặc Người 3 (Frontend)
 */
public class LichSuRequest {
    private String tapThuocTinhU;
    private String tapPhuThuocHamF;
    private String loaiThuatToan;
    private String dauVaoBoSung;
    private String ketQuaChiTiet;
    private long thoiGianThucThiMs;
    private boolean thanhCong = true;

    public LichSuRequest() {}

    public LichSuRequest(String tapThuocTinhU, String tapPhuThuocHamF, String loaiThuatToan, 
                         String dauVaoBoSung, String ketQuaChiTiet, long thoiGianThucThiMs, boolean thanhCong) {
        this.tapThuocTinhU = tapThuocTinhU;
        this.tapPhuThuocHamF = tapPhuThuocHamF;
        this.loaiThuatToan = loaiThuatToan;
        this.dauVaoBoSung = dauVaoBoSung;
        this.ketQuaChiTiet = ketQuaChiTiet;
        this.thoiGianThucThiMs = thoiGianThucThiMs;
        this.thanhCong = thanhCong;
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
}
