package com.doan10.db_normalization_tool.model;

import java.util.Set;

public class PhuThuocHam {
    private Set<String> vT; // Vế trái (Left side)
    private Set<String> vP; // Vế phải (Right side)

    // Constructor rỗng
    public PhuThuocHam() {}

    // Constructor có tham số
    public PhuThuocHam(Set<String> vT, Set<String> vP) {
        this.vT = vT;
        this.vP = vP;
    }

    // Các hàm Getter và Setter thủ công thay cho Lombok
    public Set<String> getVT() { return vT; }
    public void setVT(Set<String> vT) { this.vT = vT; }

    public Set<String> getVP() { return vP; }
    public void setVP(Set<String> vP) { this.vP = vP; }

    // Hàm tiện ích để in ra màn hình
    @Override
    public String toString() {
        return String.join(",", vT) + " -> " + String.join(",", vP);
    }
}