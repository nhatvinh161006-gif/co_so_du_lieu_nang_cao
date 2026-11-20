package com.doan10.db_normalization_tool.dto;

/**
 * DTO nhận yêu cầu giao dịch đăng ký môn học
 */
public class DangKyRequest {
    private String maSV;
    private String maLHP;

    public DangKyRequest() {}

    public DangKyRequest(String maSV, String maLHP) {
        this.maSV = maSV;
        this.maLHP = maLHP;
    }

    public String getMaSV() {
        return maSV;
    }

    public void setMaSV(String maSV) {
        this.maSV = maSV;
    }

    public String getMaLHP() {
        return maLHP;
    }

    public void setMaLHP(String maLHP) {
        this.maLHP = maLHP;
    }
}
