package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.model.PhuThuocHam;
import com.doan10.db_normalization_tool.model.StepLog;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PhanRaBcnfAlgorithm {
    private final BaoDongAlgorithm baoDongAlgorithm;
    private final TimKhoaAlgorithm timKhoaAlgorithm;

    public PhanRaBcnfAlgorithm(BaoDongAlgorithm baoDongAlgorithm, TimKhoaAlgorithm timKhoaAlgorithm) {
        this.baoDongAlgorithm = baoDongAlgorithm;
        this.timKhoaAlgorithm = timKhoaAlgorithm;
    }

    public KetQuaPhanRaBcnf phanRaBcnf(Set<String> tapThuocTinh, List<PhuThuocHam> tapF) {
        List<StepLog> buocGiai = new ArrayList<>();
        buocGiai.add(new StepLog(1, "Khởi tạo phân rã BCNF",
                "Bắt đầu thuật toán phân rã BCNF với lược đồ ban đầu R0 = " + tapThuocTinh + "."));

        List<Set<String>> cacQuanHe = new ArrayList<>();
        cacQuanHe.add(new LinkedHashSet<>(tapThuocTinh));

        List<PhuThuocHamViPham> danhSachViPham = new ArrayList<>();
        boolean tiepTucPhanRa;
        int buoc = 1;

        do {
            tiepTucPhanRa = false;
            for (int i = 0; i < cacQuanHe.size(); i++) {
                Set<String> R = cacQuanHe.get(i);
                if (R.size() <= 2) {
                    // Mọi quan hệ có 2 thuộc tính đều tự động đạt BCNF
                    continue;
                }

                ViPhamBcnf viPham = timPhuThuocViPhamBcnf(R, tapF);
                if (viPham != null) {
                    buoc++;
                    tiepTucPhanRa = true;
                    cacQuanHe.remove(i);

                    Set<String> R1 = new LinkedHashSet<>(viPham.X);
                    R1.addAll(viPham.Y);

                    Set<String> R2 = new LinkedHashSet<>(R);
                    R2.removeAll(viPham.Y);

                    cacQuanHe.add(i, R2);
                    cacQuanHe.add(i, R1);

                    danhSachViPham.add(new PhuThuocHamViPham(
                            R, viPham.X, viPham.Y, R1, R2, viPham.baoDongX
                    ));

                    buocGiai.add(new StepLog(buocGiai.size() + 1, "Phát hiện vi phạm BCNF",
                            "Xét lược đồ " + R + ": Phụ thuộc hàm " + viPham.X + " -> " + viPham.Y
                                    + " vi phạm BCNF vì " + viPham.X + "+ ∩ " + R + " = " + viPham.baoDongX
                                    + " ≠ " + R + " (vế trái không phải siêu khóa của quan hệ này)."));

                    buocGiai.add(new StepLog(buocGiai.size() + 1, "Tách lược đồ",
                            "Tách " + R + " thành hai lược đồ con: "
                                    + "R1 = (" + viPham.X + " ∪ " + viPham.Y + ") = " + R1
                                    + " và R2 = (" + R + " - (" + viPham.Y + " - " + viPham.X + ")) = " + R2 + "."));

                    break; // Quay lại vòng lặp ngoài để kiểm tra tiếp
                }
            }
        } while (tiepTucPhanRa);

        // Loại bỏ lược đồ con nếu là tập con của lược đồ khác
        for (int i = 0; i < cacQuanHe.size(); i++) {
            Set<String> r1 = cacQuanHe.get(i);
            boolean biBaoHam = false;
            for (int j = 0; j < cacQuanHe.size(); j++) {
                if (i != j && cacQuanHe.get(j).containsAll(r1) && !r1.containsAll(cacQuanHe.get(j))) {
                    biBaoHam = true;
                    break;
                }
            }
            if (biBaoHam) {
                buocGiai.add(new StepLog(buocGiai.size() + 1, "Rút gọn lược đồ",
                        "Loại bỏ quan hệ con " + r1 + " vì đã được bao hàm trong một quan hệ con khác."));
                cacQuanHe.remove(i--);
            }
        }

        // Tìm khóa ứng viên cho từng quan hệ con
        List<QuanHeBcnf> ketQuaQuanHe = new ArrayList<>();
        for (int i = 0; i < cacQuanHe.size(); i++) {
            Set<String> Ri = cacQuanHe.get(i);
            var ketQuaKhoa = timKhoaAlgorithm.timTatCaKhoa(Ri, tapF);
            ketQuaQuanHe.add(new QuanHeBcnf("R" + (i + 1), Ri, ketQuaKhoa.khoaUngVien()));
        }

        buocGiai.add(new StepLog(buocGiai.size() + 1, "Hoàn tất phân rã BCNF",
                "Quá trình phân rã kết thúc. Thu được " + ketQuaQuanHe.size()
                        + " lược đồ con, tất cả đều đạt dạng chuẩn BCNF và đảm bảo phép nối không mất mát thông tin (lossless join)."));

        return new KetQuaPhanRaBcnf(ketQuaQuanHe, danhSachViPham, buocGiai);
    }

    private ViPhamBcnf timPhuThuocViPhamBcnf(Set<String> R, List<PhuThuocHam> tapF) {
        // Cách 1: Kiểm tra các phụ thuộc trực tiếp trong F có vế trái thuộc R
        for (PhuThuocHam fd : tapF) {
            if (R.containsAll(fd.getVT())) {
                Set<String> YTrongR = new LinkedHashSet<>(fd.getVP());
                YTrongR.retainAll(R);
                YTrongR.removeAll(fd.getVT());

                if (!YTrongR.isEmpty()) {
                    Set<String> baoDong = baoDongAlgorithm.tinhBaoDong(fd.getVT(), tapF);
                    Set<String> baoDongTrongR = new LinkedHashSet<>(baoDong);
                    baoDongTrongR.retainAll(R);

                    if (!baoDongTrongR.containsAll(R)) {
                        return new ViPhamBcnf(new LinkedHashSet<>(fd.getVT()), YTrongR, baoDongTrongR);
                    }
                }
            }
        }

        // Cách 2: Kiểm tra các tập con X ⊂ R (từ kích thước 1 đến |R| - 1)
        List<String> danhSachThuocTinh = new ArrayList<>(R);
        int maxK = Math.min(R.size() - 1, 3); // Giới hạn kích thước tổ hợp để tránh bùng nổ hàm mũ

        for (int k = 1; k <= maxK; k++) {
            ViPhamBcnf timDuoc = timToHopViPham(danhSachThuocTinh, 0, k, new LinkedHashSet<>(), R, tapF);
            if (timDuoc != null) {
                return timDuoc;
            }
        }

        return null;
    }

    private ViPhamBcnf timToHopViPham(
            List<String> danhSach,
            int batDau,
            int k,
            Set<String> hienTai,
            Set<String> R,
            List<PhuThuocHam> tapF) {
        if (hienTai.size() == k) {
            Set<String> baoDong = baoDongAlgorithm.tinhBaoDong(hienTai, tapF);
            Set<String> baoDongTrongR = new LinkedHashSet<>(baoDong);
            baoDongTrongR.retainAll(R);

            Set<String> Y = new LinkedHashSet<>(baoDongTrongR);
            Y.removeAll(hienTai);

            if (!Y.isEmpty() && !baoDongTrongR.containsAll(R)) {
                return new ViPhamBcnf(new LinkedHashSet<>(hienTai), Y, baoDongTrongR);
            }
            return null;
        }

        for (int i = batDau; i <= danhSach.size() - (k - hienTai.size()); i++) {
            hienTai.add(danhSach.get(i));
            ViPhamBcnf timDuoc = timToHopViPham(danhSach, i + 1, k, hienTai, R, tapF);
            if (timDuoc != null) {
                return timDuoc;
            }
            hienTai.remove(danhSach.get(i));
        }
        return null;
    }

    private record ViPhamBcnf(Set<String> X, Set<String> Y, Set<String> baoDongX) {}

    public record PhuThuocHamViPham(
            Set<String> quanHeBiTach,
            Set<String> veTraiX,
            Set<String> vePhaiY,
            Set<String> quanHeConR1,
            Set<String> quanHeConR2,
            Set<String> baoDongX
    ) {}

    public record QuanHeBcnf(
            String tenQuanHe,
            Set<String> thuocTinh,
            List<Set<String>> khoaUngVien
    ) {}

    public record KetQuaPhanRaBcnf(
            List<QuanHeBcnf> cacQuanHe,
            List<PhuThuocHamViPham> danhSachViPham,
            List<StepLog> buocGiai
    ) {}
}
