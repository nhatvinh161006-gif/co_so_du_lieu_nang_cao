package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.model.PhuThuocHam;
import com.doan10.db_normalization_tool.model.StepLog;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BaoToanPhuThuocAlgorithm {
    private final BaoDongAlgorithm baoDongAlgorithm;

    public BaoToanPhuThuocAlgorithm(BaoDongAlgorithm baoDongAlgorithm) {
        this.baoDongAlgorithm = baoDongAlgorithm;
    }

    public KetQuaBaoToanPhuThuoc kiemTraBaoToan(
            List<Set<String>> phanRa,
            List<PhuThuocHam> tapF) {
        List<StepLog> buocGiai = new ArrayList<>();
        buocGiai.add(new StepLog(1, "Khởi tạo kiểm tra bảo toàn phụ thuộc",
                "Kiểm tra xem từng phụ thuộc hàm X -> Y trong F có được bảo toàn qua phép phân rã gồm "
                        + phanRa.size() + " lược đồ con hay không."));

        List<ChiTietBaoToan> danhSachChiTiet = new ArrayList<>();
        List<PhuThuocHam> phuThuocBiMat = new ArrayList<>();
        boolean baoToanTatCa = true;

        for (int idx = 0; idx < tapF.size(); idx++) {
            PhuThuocHam fd = tapF.get(idx);

            // Kiểm tra xem fd có nằm trọn trong một lược đồ con nào không (trường hợp hiển nhiên)
            boolean namTronTrongMotQuanHe = false;
            int quanHeChuaTron = -1;
            Set<String> hopXY = new LinkedHashSet<>(fd.getVT());
            hopXY.addAll(fd.getVP());

            for (int i = 0; i < phanRa.size(); i++) {
                if (phanRa.get(i).containsAll(hopXY)) {
                    namTronTrongMotQuanHe = true;
                    quanHeChuaTron = i + 1;
                    break;
                }
            }

            if (namTronTrongMotQuanHe) {
                String ghiChu = "Được bảo toàn trực tiếp: Toàn bộ X ∪ Y = " + hopXY
                        + " nằm trọn trong lược đồ con R" + quanHeChuaTron + " = " + phanRa.get(quanHeChuaTron - 1) + ".";
                danhSachChiTiet.add(new ChiTietBaoToan(fd, true, hopXY, ghiChu));
                buocGiai.add(new StepLog(buocGiai.size() + 1, "Kiểm tra " + fd, ghiChu));
                continue;
            }

            // Thuật toán lan truyền đa thức kiểm tra bao đóng Z
            Set<String> Z = new LinkedHashSet<>(fd.getVT());
            boolean thayDoi;
            int vongLap = 0;

            do {
                thayDoi = false;
                vongLap++;
                for (int i = 0; i < phanRa.size(); i++) {
                    Set<String> Ri = phanRa.get(i);
                    Set<String> giaoZRi = new LinkedHashSet<>(Z);
                    giaoZRi.retainAll(Ri);

                    if (!giaoZRi.isEmpty()) {
                        Set<String> baoDongGiao = baoDongAlgorithm.tinhBaoDong(giaoZRi, tapF);
                        Set<String> phanDongGop = new LinkedHashSet<>(baoDongGiao);
                        phanDongGop.retainAll(Ri);

                        if (Z.addAll(phanDongGop)) {
                            thayDoi = true;
                        }
                    }
                }
            } while (thayDoi);

            boolean duocBaoToan = Z.containsAll(fd.getVP());
            if (!duocBaoToan) {
                baoToanTatCa = false;
                phuThuocBiMat.add(fd);
            }

            Set<String> thieu = new LinkedHashSet<>(fd.getVP());
            thieu.removeAll(Z);

            String ghiChu = duocBaoToan
                    ? "Bảo toàn gián tiếp: Bao đóng lan truyền Z = " + Z + " chứa trọn vế phải Y = " + fd.getVP() + "."
                    : "BỊ MẤT: Bao đóng lan truyền Z = " + Z + " KHÔNG chứa các thuộc tính " + thieu + " của vế phải Y.";

            danhSachChiTiet.add(new ChiTietBaoToan(fd, duocBaoToan, Z, ghiChu));
            buocGiai.add(new StepLog(buocGiai.size() + 1, "Kiểm tra " + fd,
                    "Lan truyền qua các lược đồ con kết thúc tại Z = " + Z + " => " + ghiChu));
        }

        buocGiai.add(new StepLog(buocGiai.size() + 1, "Kết luận chung về bảo toàn phụ thuộc",
                baoToanTatCa
                        ? "Mọi phụ thuộc hàm trong F đều được bảo toàn qua phép phân rã (BẢO TOÀN PHỤ THUỘC)."
                        : "Phép phân rã KHÔNG BẢO TOÀN PHỤ THUỘC. Có " + phuThuocBiMat.size() + " phụ thuộc bị mất: " + phuThuocBiMat + "."));

        return new KetQuaBaoToanPhuThuoc(
                baoToanTatCa,
                danhSachChiTiet,
                phuThuocBiMat,
                buocGiai
        );
    }

    public record ChiTietBaoToan(
            PhuThuocHam phuThuoc,
            boolean duocBaoToan,
            Set<String> baoDongLanTruyen,
            String ghiChu
    ) {}

    public record KetQuaBaoToanPhuThuoc(
            boolean baoToanTatCa,
            List<ChiTietBaoToan> danhSachChiTiet,
            List<PhuThuocHam> phuThuocBiMat,
            List<StepLog> buocGiai
    ) {}
}
