package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.model.PhuThuocHam;
import com.doan10.db_normalization_tool.model.StepLog;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class TuongDuongAlgorithm {
    private final BaoDongAlgorithm baoDongAlgorithm;

    public TuongDuongAlgorithm(BaoDongAlgorithm baoDongAlgorithm) {
        this.baoDongAlgorithm = baoDongAlgorithm;
    }

    public KetQuaTuongDuong kiemTraTuongDuong(List<PhuThuocHam> tapF1, List<PhuThuocHam> tapF2) {
        List<StepLog> buocGiai = new ArrayList<>();
        buocGiai.add(new StepLog(1, "Khởi tạo kiểm tra tương đương",
                "Kiểm tra F1 ≡ F2 bằng cách chứng minh cả hai chiều: (1) F1 ⊨ F2 và (2) F2 ⊨ F1."));

        // Chiều 1: F1 suy ra F2?
        buocGiai.add(new StepLog(buocGiai.size() + 1, "Chiều 1: Kiểm tra F1 ⊨ F2",
                "Với mỗi phụ thuộc X -> Y trong F2, kiểm tra xem Y có nằm trong bao đóng X+(F1) hay không."));
        List<ChiTietKiemTra> chiTietF1SuyRaF2 = new ArrayList<>();
        boolean f1SuyRaF2 = true;

        for (PhuThuocHam fd : tapF2) {
            Set<String> baoDong = baoDongAlgorithm.tinhBaoDong(fd.getVT(), tapF1);
            boolean thoaMan = baoDong.containsAll(fd.getVP());
            if (!thoaMan) {
                f1SuyRaF2 = false;
            }
            Set<String> thieu = new LinkedHashSet<>(fd.getVP());
            thieu.removeAll(baoDong);
            String ghiChu = thoaMan
                    ? "Thỏa mãn: vế phải " + fd.getVP() + " ⊆ " + fd.getVT() + "+(F1) = " + baoDong
                    : "Vi phạm: vế phải thiếu các thuộc tính " + thieu + " không thuộc " + fd.getVT() + "+(F1) = " + baoDong;

            chiTietF1SuyRaF2.add(new ChiTietKiemTra(fd, baoDong, thoaMan, ghiChu));
            buocGiai.add(new StepLog(buocGiai.size() + 1, "Xét phụ thuộc F2",
                    "Kiểm tra " + fd + ": " + ghiChu));
        }

        buocGiai.add(new StepLog(buocGiai.size() + 1, "Kết luận Chiều 1",
                f1SuyRaF2
                        ? "Mọi phụ thuộc trong F2 đều được suy diễn từ F1 => F1 ⊨ F2."
                        : "Có phụ thuộc trong F2 KHÔNG được suy diễn từ F1 => F1 KHÔNG suy ra F2."));

        // Chiều 2: F2 suy ra F1?
        buocGiai.add(new StepLog(buocGiai.size() + 1, "Chiều 2: Kiểm tra F2 ⊨ F1",
                "Với mỗi phụ thuộc X -> Y trong F1, kiểm tra xem Y có nằm trong bao đóng X+(F2) hay không."));
        List<ChiTietKiemTra> chiTietF2SuyRaF1 = new ArrayList<>();
        boolean f2SuyRaF1 = true;

        for (PhuThuocHam fd : tapF1) {
            Set<String> baoDong = baoDongAlgorithm.tinhBaoDong(fd.getVT(), tapF2);
            boolean thoaMan = baoDong.containsAll(fd.getVP());
            if (!thoaMan) {
                f2SuyRaF1 = false;
            }
            Set<String> thieu = new LinkedHashSet<>(fd.getVP());
            thieu.removeAll(baoDong);
            String ghiChu = thoaMan
                    ? "Thỏa mãn: vế phải " + fd.getVP() + " ⊆ " + fd.getVT() + "+(F2) = " + baoDong
                    : "Vi phạm: vế phải thiếu các thuộc tính " + thieu + " không thuộc " + fd.getVT() + "+(F2) = " + baoDong;

            chiTietF2SuyRaF1.add(new ChiTietKiemTra(fd, baoDong, thoaMan, ghiChu));
            buocGiai.add(new StepLog(buocGiai.size() + 1, "Xét phụ thuộc F1",
                    "Kiểm tra " + fd + ": " + ghiChu));
        }

        buocGiai.add(new StepLog(buocGiai.size() + 1, "Kết luận Chiều 2",
                f2SuyRaF1
                        ? "Mọi phụ thuộc trong F1 đều được suy diễn từ F2 => F2 ⊨ F1."
                        : "Có phụ thuộc trong F1 KHÔNG được suy diễn từ F2 => F2 KHÔNG suy ra F1."));

        boolean tuongDuong = f1SuyRaF2 && f2SuyRaF1;
        buocGiai.add(new StepLog(buocGiai.size() + 1, "Kết luận chung",
                tuongDuong
                        ? "Cả hai chiều F1 ⊨ F2 và F2 ⊨ F1 đều thỏa mãn => Hai tập phụ thuộc hàm TƯƠNG ĐƯƠNG (F1 ≡ F2)."
                        : "Ít nhất một chiều không thỏa mãn => Hai tập phụ thuộc hàm KHÔNG TƯƠNG ĐƯƠNG (F1 ≢ F2)."));

        return new KetQuaTuongDuong(
                tuongDuong,
                f1SuyRaF2,
                f2SuyRaF1,
                chiTietF1SuyRaF2,
                chiTietF2SuyRaF1,
                buocGiai
        );
    }

    public record ChiTietKiemTra(
            PhuThuocHam phuThuoc,
            Set<String> baoDongVeTrai,
            boolean duocSuyDien,
            String lyDo
    ) {}

    public record KetQuaTuongDuong(
            boolean tuongDuong,
            boolean f1SuyRaF2,
            boolean f2SuyRaF1,
            List<ChiTietKiemTra> chiTietF1SuyRaF2,
            List<ChiTietKiemTra> chiTietF2SuyRaF1,
            List<StepLog> buocGiai
    ) {}
}
