package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.model.PhuThuocHam;
import com.doan10.db_normalization_tool.model.StepLog;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class TimKhoaAlgorithm {
    private final BaoDongAlgorithm baoDongAlgorithm;

    public TimKhoaAlgorithm(BaoDongAlgorithm baoDongAlgorithm) {
        this.baoDongAlgorithm = baoDongAlgorithm;
    }

    public KetQuaTimKhoa timTatCaKhoa(Set<String> tapThuocTinh, List<PhuThuocHam> tapF) {
        List<String> thuocTinh = new ArrayList<>(tapThuocTinh);
        Set<String> xuatHienVePhai = new LinkedHashSet<>();
        Set<String> xuatHienVeTrai = new LinkedHashSet<>();
        for (PhuThuocHam fd : tapF) {
            xuatHienVePhai.addAll(fd.getVP());
            xuatHienVeTrai.addAll(fd.getVT());
        }
        Set<String> nguon = new LinkedHashSet<>(tapThuocTinh);
        nguon.removeAll(xuatHienVePhai);
        Set<String> dich = new LinkedHashSet<>(xuatHienVePhai);
        dich.removeAll(xuatHienVeTrai);

        Set<String> trungGian = new LinkedHashSet<>(tapThuocTinh);
        trungGian.removeAll(nguon);
        trungGian.removeAll(dich);

        List<StepLog> buocGiai = new ArrayList<>();
        buocGiai.add(new StepLog(1, "Phân loại thuộc tính",
                "Nguồn (không ở vế phải): " + nguon + "; đích (chỉ ở vế phải): " + dich
                        + "; trung gian (xuất hiện ở cả hai vế): " + trungGian + "."));

        var ketQuaBaoDongNguon = baoDongAlgorithm.tinhBaoDongChiTiet(nguon, tapF);
        appendClosureSteps(buocGiai, "Tính bao đóng tập nguồn", ketQuaBaoDongNguon.buocGiai());
        Set<String> baoDongNguon = ketQuaBaoDongNguon.baoDong();
        buocGiai.add(new StepLog(buocGiai.size() + 1, "Thử tập nguồn",
                "Tập nguồn " + nguon + " có bao đóng " + baoDongNguon + "."));
        if (baoDongNguon.containsAll(tapThuocTinh)) {
            buocGiai.add(new StepLog(buocGiai.size() + 1, "Kết luận",
                    "Tập nguồn đã bao phủ U nên chính nó là khóa ứng viên duy nhất."));
            return new KetQuaTimKhoa(List.of(nguon), nguon, dich, trungGian, buocGiai);
        }

        List<String> ungVienTrungGian = new ArrayList<>();
        for (String attribute : thuocTinh) {
            if (trungGian.contains(attribute)) {
                ungVienTrungGian.add(attribute);
            }
        }
        List<Set<String>> khoa = new ArrayList<>();
        for (int size = 1; size <= ungVienTrungGian.size(); size++) {
            timToHop(ungVienTrungGian, 0, size, new LinkedHashSet<>(), nguon,
                    tapThuocTinh, tapF, khoa, buocGiai);
        }
        buocGiai.add(new StepLog(buocGiai.size() + 1, "Kết luận",
                "Các tập siêu khóa không tối tiểu đã bị loại; tìm được " + khoa.size() + " khóa ứng viên."));
        return new KetQuaTimKhoa(khoa, nguon, dich, trungGian, buocGiai);
    }

    private void timToHop(
            List<String> trungGian,
            int start,
            int soLuongCanChon,
            Set<String> daChon,
            Set<String> nguon,
            Set<String> tapThuocTinh,
            List<PhuThuocHam> tapF,
            List<Set<String>> khoa,
            List<StepLog> buocGiai) {
        if (daChon.size() == soLuongCanChon) {
            Set<String> tapDangXet = new LinkedHashSet<>(nguon);
            tapDangXet.addAll(daChon);
            if (khoa.stream().anyMatch(tapDangXet::containsAll)) {
                buocGiai.add(new StepLog(buocGiai.size() + 1, "Loại tổ hợp",
                        tapDangXet + " chứa một khóa đã tìm thấy nên không tối tiểu."));
                return;
            }

            var ketQuaBaoDong = baoDongAlgorithm.tinhBaoDongChiTiet(tapDangXet, tapF);
            appendClosureSteps(buocGiai, "Tính bao đóng " + tapDangXet, ketQuaBaoDong.buocGiai());
            Set<String> baoDong = ketQuaBaoDong.baoDong();
            boolean laSieuKhoa = baoDong.containsAll(tapThuocTinh);
            buocGiai.add(new StepLog(buocGiai.size() + 1, "Kiểm tra tổ hợp",
                    tapDangXet + " có bao đóng " + baoDong + (laSieuKhoa
                            ? "; bao phủ U nên là khóa ứng viên tối tiểu."
                            : "; chưa bao phủ U nên không phải siêu khóa.")));
            if (laSieuKhoa) {
                khoa.add(tapDangXet);
            }

            return;
        }

        int remaining = soLuongCanChon - daChon.size();
        for (int i = start; i <= trungGian.size() - remaining; i++) {
            daChon.add(trungGian.get(i));
            timToHop(trungGian, i + 1, soLuongCanChon, daChon, nguon,
                    tapThuocTinh, tapF, khoa, buocGiai);
            daChon.remove(trungGian.get(i));
        }
    }

    private void appendClosureSteps(List<StepLog> target, String phase, List<StepLog> source) {
        for (StepLog step : source) {
            target.add(new StepLog(target.size() + 1, phase, step.detail()));
        }
    }

    public record KetQuaTimKhoa(
            List<Set<String>> khoaUngVien,
            Set<String> tapNguon,
            Set<String> tapDich,
            Set<String> tapTrungGian,
            List<StepLog> buocGiai) {}
}
