package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.model.PhuThuocHam;
import com.doan10.db_normalization_tool.model.StepLog;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class BaoDongAlgorithm {

    public Set<String> tinhBaoDong(Set<String> tapX, List<PhuThuocHam> tapF) {
        return tinhBaoDongChiTiet(tapX, tapF).baoDong();
    }

    public KetQuaBaoDong tinhBaoDongChiTiet(Set<String> tapX, List<PhuThuocHam> tapF) {
        Set<String> baoDong = new LinkedHashSet<>(tapX);
        List<StepLog> buocGiai = new ArrayList<>();
        boolean isChanged;
        int vongLap = 0;

        do {
            isChanged = false;
            vongLap++;
            for (PhuThuocHam fd : tapF) {
                if (baoDong.containsAll(fd.getVT())) {
                    Set<String> moi = new LinkedHashSet<>(fd.getVP());
                    moi.removeAll(baoDong);
                    if (!moi.isEmpty()) {
                        baoDong.addAll(moi);
                        isChanged = true;
                        buocGiai.add(new StepLog(
                                buocGiai.size() + 1,
                                "Áp dụng phụ thuộc hàm",
                                "Vòng " + vongLap + ": " + fd + " có vế trái nằm trong bao đóng; thêm "
                                        + moi + ", bao đóng hiện tại là " + baoDong + "."));
                    }
                }
            }
        } while (isChanged);

        buocGiai.add(new StepLog(
                buocGiai.size() + 1,
                "Đạt điểm cố định",
                "Không còn phụ thuộc hàm nào thêm được thuộc tính mới. Bao đóng cuối cùng: " + baoDong + "."));
        return new KetQuaBaoDong(baoDong, buocGiai);
    }

    public record KetQuaBaoDong(Set<String> baoDong, List<StepLog> buocGiai) {}
}