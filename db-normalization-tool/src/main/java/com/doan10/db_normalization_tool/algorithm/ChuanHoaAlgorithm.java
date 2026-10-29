package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.algorithm.PhuToiThieuAlgorithm.KetQuaPhuToiThieu;
import com.doan10.db_normalization_tool.algorithm.TimKhoaAlgorithm.KetQuaTimKhoa;
import com.doan10.db_normalization_tool.model.PhuThuocHam;
import com.doan10.db_normalization_tool.model.StepLog;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ChuanHoaAlgorithm {
    private final BaoDongAlgorithm baoDongAlgorithm;
    private final TimKhoaAlgorithm timKhoaAlgorithm;
    private final PhuToiThieuAlgorithm phuToiThieuAlgorithm;

    public ChuanHoaAlgorithm(
            BaoDongAlgorithm baoDongAlgorithm,
            TimKhoaAlgorithm timKhoaAlgorithm,
            PhuToiThieuAlgorithm phuToiThieuAlgorithm) {
        this.baoDongAlgorithm = baoDongAlgorithm;
        this.timKhoaAlgorithm = timKhoaAlgorithm;
        this.phuToiThieuAlgorithm = phuToiThieuAlgorithm;
    }

    public KetQuaKiemTraDangChuan kiemTraDangChuan(
            Set<String> tapThuocTinh,
            List<PhuThuocHam> tapF,
            boolean giaTriNguyenTu) {
        List<StepLog> buocGiai = new ArrayList<>();
        List<Violation> viPham = new ArrayList<>();
        if (!giaTriNguyenTu) {
            viPham.add(new Violation("1NF", "Dữ liệu đầu vào",
                    "Không thể khẳng định 1NF vì có thuộc tính chứa giá trị không nguyên tử."));
            buocGiai.add(new StepLog(1, "Kiểm tra 1NF",
                    "Có thuộc tính chứa giá trị không nguyên tử; lược đồ chưa đạt 1NF."));
            return new KetQuaKiemTraDangChuan("Chưa đạt 1NF", List.of(), Set.of(), viPham, buocGiai);
        }

        buocGiai.add(new StepLog(1, "Giả định 1NF",
                "Các giá trị thuộc tính được giả định là nguyên tử; tiếp tục kiểm tra 2NF, 3NF và BCNF."));
        KetQuaTimKhoa ketQuaKhoa = timKhoaAlgorithm.timTatCaKhoa(tapThuocTinh, tapF);
        appendSteps(buocGiai, "Tìm khóa", ketQuaKhoa.buocGiai());
        Set<String> thuocTinhKhoa = new LinkedHashSet<>();
        ketQuaKhoa.khoaUngVien().forEach(thuocTinhKhoa::addAll);
        Set<String> thuocTinhKhongKhoa = new LinkedHashSet<>(tapThuocTinh);
        thuocTinhKhongKhoa.removeAll(thuocTinhKhoa);

        boolean viPham2NF = false;
        for (Set<String> khoa : ketQuaKhoa.khoaUngVien()) {
            // Every proper subset is contained in one of these maximal proper subsets.
            for (String attribute : khoa) {
                Set<String> tapConMaximal = new LinkedHashSet<>(khoa);
                tapConMaximal.remove(attribute);
                Set<String> baoDong = baoDongAlgorithm.tinhBaoDong(tapConMaximal, tapF);
                Set<String> phuThuocBoPhan = new LinkedHashSet<>(baoDong);
                phuThuocBoPhan.retainAll(thuocTinhKhongKhoa);
                if (!phuThuocBoPhan.isEmpty()) {
                    viPham2NF = true;
                    viPham.add(new Violation("2NF", tapConMaximal + " -> " + phuThuocBoPhan,
                            "Một tập con thực sự của khóa ứng viên " + khoa
                                    + " xác định thuộc tính không khóa " + phuThuocBoPhan + "."));
                    buocGiai.add(new StepLog(buocGiai.size() + 1, "Vi phạm 2NF",
                            "Khóa " + khoa + " bỏ " + attribute + " còn " + tapConMaximal
                                    + "; bao đóng chứa thuộc tính không khóa " + phuThuocBoPhan + "."));
                }
            }
        }
        if (!viPham2NF) {
            buocGiai.add(new StepLog(buocGiai.size() + 1, "Đạt 2NF",
                    "Không có thuộc tính không khóa nào phụ thuộc vào tập con thực sự của khóa ứng viên."));
        }

        boolean viPham3NF = false;
        boolean viPhamBCNF = false;
        for (PhuThuocHam fd : tachVePhai(tapF)) {
            if (fd.getVT().containsAll(fd.getVP())) {
                continue;
            }
            Set<String> baoDong = baoDongAlgorithm.tinhBaoDong(fd.getVT(), tapF);
            boolean veTraiLaSieuKhoa = baoDong.containsAll(tapThuocTinh);
            boolean vePhaiLaThuocTinhKhoa = thuocTinhKhoa.containsAll(fd.getVP());
            if (!veTraiLaSieuKhoa && !vePhaiLaThuocTinhKhoa) {
                viPham3NF = true;
                viPham.add(new Violation("3NF", fd.toString(),
                        "Vế trái không là siêu khóa và vế phải chứa thuộc tính không khóa."));
                buocGiai.add(new StepLog(buocGiai.size() + 1, "Vi phạm 3NF",
                        fd + ": vế trái có bao đóng " + baoDong
                                + " không chứa U, vế phải không phải thuộc tính khóa."));
            } else {
                buocGiai.add(new StepLog(buocGiai.size() + 1, "Đạt điều kiện 3NF",
                        fd + (veTraiLaSieuKhoa ? " có vế trái là siêu khóa."
                                : " có vế phải là thuộc tính khóa.")));
            }
            if (!veTraiLaSieuKhoa) {
                viPhamBCNF = true;
                viPham.add(new Violation("BCNF", fd.toString(),
                        "Vế trái không là siêu khóa."));
                buocGiai.add(new StepLog(buocGiai.size() + 1, "Vi phạm BCNF",
                        fd + " có vế trái với bao đóng " + baoDong + " không chứa toàn bộ U."));
            }
        }
        if (!viPhamBCNF) {
            buocGiai.add(new StepLog(buocGiai.size() + 1, "Đạt BCNF",
                    "Vế trái của mọi phụ thuộc hàm không tầm thường đều là siêu khóa."));
        }

        String dangChuanCaoNhat = viPham2NF ? "1NF"
                : viPham3NF ? "2NF"
                : viPhamBCNF ? "3NF" : "BCNF";
        buocGiai.add(new StepLog(buocGiai.size() + 1, "Kết luận",
                "Dạng chuẩn cao nhất là " + dangChuanCaoNhat + "."));
        return new KetQuaKiemTraDangChuan(
                dangChuanCaoNhat, ketQuaKhoa.khoaUngVien(), thuocTinhKhoa, viPham, buocGiai);
    }

    public KetQuaPhanRa3NF phanRa3NF(Set<String> tapThuocTinh, List<PhuThuocHam> tapF) {
        KetQuaPhuToiThieu phuToiThieu = phuToiThieuAlgorithm.timPhuToiThieu(tapF);
        List<StepLog> buocGiai = new ArrayList<>();
        appendSteps(buocGiai, "Tìm phủ tối thiểu", phuToiThieu.buocGiai());

        Map<Set<String>, Set<String>> nhomTheoVeTrai = new LinkedHashMap<>();
        for (PhuThuocHam fd : phuToiThieu.phuToiThieu()) {
            Set<String> veTrai = new LinkedHashSet<>(fd.getVT());
            nhomTheoVeTrai.computeIfAbsent(veTrai, ignored -> new LinkedHashSet<>(veTrai))
                    .addAll(fd.getVP());
        }
        List<Set<String>> quanHe = new ArrayList<>();
        for (Set<String> nhom : nhomTheoVeTrai.values()) {
            quanHe.add(nhom);
            buocGiai.add(new StepLog(buocGiai.size() + 1, "Tạo lược đồ con",
                    "Tạo quan hệ từ nhóm phụ thuộc có cùng vế trái: " + nhom + "."));
        }

        for (int i = 0; i < quanHe.size(); i++) {
            Set<String> current = quanHe.get(i);
            boolean containedByAnother = false;
            for (int j = 0; j < quanHe.size(); j++) {
                if (i != j && quanHe.get(j).containsAll(current)
                        && !current.containsAll(quanHe.get(j))) {
                    containedByAnother = true;
                    break;
                }
            }
            if (containedByAnother) {
                buocGiai.add(new StepLog(buocGiai.size() + 1, "Loại lược đồ con dư",
                        "Loại " + current + " vì tập thuộc tính đã nằm trong một quan hệ lớn hơn."));
                quanHe.remove(i--);
            }
        }

        KetQuaTimKhoa ketQuaKhoa = timKhoaAlgorithm.timTatCaKhoa(tapThuocTinh, tapF);
        appendSteps(buocGiai, "Kiểm tra khóa", ketQuaKhoa.buocGiai());
        boolean daCoQuanHeChuaKhoa = ketQuaKhoa.khoaUngVien().stream()
                .anyMatch(key -> quanHe.stream().anyMatch(relation -> relation.containsAll(key)));
        if (!daCoQuanHeChuaKhoa && !ketQuaKhoa.khoaUngVien().isEmpty()) {
            Set<String> khoa = new LinkedHashSet<>(ketQuaKhoa.khoaUngVien().get(0));
            quanHe.add(khoa);
            buocGiai.add(new StepLog(buocGiai.size() + 1, "Bảo đảm có khóa",
                    "Thêm quan hệ " + khoa + " vì chưa có quan hệ con nào chứa một khóa ứng viên."));
        } else {
            buocGiai.add(new StepLog(buocGiai.size() + 1, "Kiểm tra khóa",
                    "Đã có ít nhất một quan hệ con chứa trọn khóa ứng viên."));
        }
        return new KetQuaPhanRa3NF(quanHe, ketQuaKhoa.khoaUngVien(), buocGiai);
    }

    private List<PhuThuocHam> tachVePhai(List<PhuThuocHam> tapF) {
        List<PhuThuocHam> ketQua = new ArrayList<>();
        for (PhuThuocHam fd : tapF) {
            for (String attribute : fd.getVP()) {
                ketQua.add(new PhuThuocHam(
                        new LinkedHashSet<>(fd.getVT()), new LinkedHashSet<>(Set.of(attribute))));
            }
        }
        return ketQua;
    }

    private void appendSteps(List<StepLog> target, String phase, List<StepLog> source) {
        for (StepLog step : source) {
            target.add(new StepLog(target.size() + 1, phase, step.detail()));
        }
    }

    public record Violation(String normalForm, String dependency, String reason) {}

    public record KetQuaKiemTraDangChuan(
            String dangChuanCaoNhat,
            List<Set<String>> khoaUngVien,
            Set<String> thuocTinhKhoa,
            List<Violation> viPham,
            List<StepLog> buocGiai) {}

    public record KetQuaPhanRa3NF(
            List<Set<String>> cacQuanHe,
            List<Set<String>> khoaUngVien,
            List<StepLog> buocGiai) {}
}
