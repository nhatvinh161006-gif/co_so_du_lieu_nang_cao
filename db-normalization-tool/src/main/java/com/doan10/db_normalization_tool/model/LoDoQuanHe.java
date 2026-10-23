package com.doan10.db_normalization_tool.model;

import java.util.List;
import java.util.Set;

public class LoDoQuanHe {
    private Set<String> tapThuocTinh;
    private List<PhuThuocHam> tapPhuThuocHam;

    public LoDoQuanHe() {}

    public LoDoQuanHe(Set<String> tapThuocTinh, List<PhuThuocHam> tapPhuThuocHam) {
        this.tapThuocTinh = tapThuocTinh;
        this.tapPhuThuocHam = tapPhuThuocHam;
    }

    public Set<String> getTapThuocTinh() {
        return tapThuocTinh;
    }

    public void setTapThuocTinh(Set<String> tapThuocTinh) {
        this.tapThuocTinh = tapThuocTinh;
    }

    public List<PhuThuocHam> getTapPhuThuocHam() {
        return tapPhuThuocHam;
    }

    public void setTapPhuThuocHam(List<PhuThuocHam> tapPhuThuocHam) {
        this.tapPhuThuocHam = tapPhuThuocHam;
    }
}
