package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.model.PhuThuocHam;
import com.doan10.db_normalization_tool.model.StepLog;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PhuToiThieuAlgorithm {
    private final BaoDongAlgorithm baoDongAlgorithm;

    public PhuToiThieuAlgorithm(BaoDongAlgorithm baoDongAlgorithm) {
        this.baoDongAlgorithm = baoDongAlgorithm;
    }

    public KetQuaPhuToiThieu timPhuToiThieu(List<PhuThuocHam> tapF) {
        List<PhuThuocHam> phuThuocHam = new ArrayList<>();
        List<StepLog> buocGiai = new ArrayList<>();

        for (PhuThuocHam fd : tapF) {
            for (String thuocTinh : fd.getVP()) {
                PhuThuocHam tach = new PhuThuocHam(
                        new LinkedHashSet<>(fd.getVT()),
                        new LinkedHashSet<>(Set.of(thuocTinh)));
                phuThuocHam.add(tach);
                buocGiai.add(new StepLog(buocGiai.size() + 1, "Tách vế phải",
                        "Tách " + fd + " thành phụ thuộc đơn thuộc tính " + tach + "."));
            }
        }

        for (int i = 0; i < phuThuocHam.size(); i++) {
            PhuThuocHam fd = phuThuocHam.get(i);
            boolean changed;
            do {
                changed = false;
                for (String attribute : new ArrayList<>(fd.getVT())) {
                    Set<String> leftTrial = new LinkedHashSet<>(fd.getVT());
                    leftTrial.remove(attribute);
                    Set<String> closure = baoDongAlgorithm.tinhBaoDong(leftTrial, phuThuocHam);
                    if (closure.containsAll(fd.getVP())) {
                        fd.setVT(leftTrial);
                        changed = true;
                        buocGiai.add(new StepLog(buocGiai.size() + 1, "Rút gọn vế trái",
                                "Trong " + fd + ", bỏ " + attribute + " vì "
                                        + leftTrial + "+ vẫn chứa " + fd.getVP() + "."));
                        break;
                    }
                    buocGiai.add(new StepLog(buocGiai.size() + 1, "Giữ thuộc tính vế trái",
                            "Không thể bỏ " + attribute + " khỏi " + fd
                                    + " vì bao đóng thử " + closure + " không chứa " + fd.getVP() + "."));
                }
            } while (changed);
        }

        for (int i = 0; i < phuThuocHam.size();) {
            PhuThuocHam fd = phuThuocHam.get(i);
            List<PhuThuocHam> conLai = new ArrayList<>(phuThuocHam);
            conLai.remove(i);
            Set<String> closure = baoDongAlgorithm.tinhBaoDong(fd.getVT(), conLai);
            if (closure.containsAll(fd.getVP())) {
                buocGiai.add(new StepLog(buocGiai.size() + 1, "Loại phụ thuộc dư thừa",
                        "Xóa " + fd + " vì vế trái có bao đóng " + closure
                                + " trên tập FD còn lại, vẫn suy ra được vế phải."));
                phuThuocHam.remove(i);
            } else {
                buocGiai.add(new StepLog(buocGiai.size() + 1, "Giữ phụ thuộc",
                        "Giữ " + fd + " vì nếu bỏ đi, bao đóng " + closure
                                + " không suy ra được vế phải " + fd.getVP() + "."));
                i++;
            }
        }
        return new KetQuaPhuToiThieu(phuThuocHam, buocGiai);
    }

    public record KetQuaPhuToiThieu(List<PhuThuocHam> phuToiThieu, List<StepLog> buocGiai) {}
}
