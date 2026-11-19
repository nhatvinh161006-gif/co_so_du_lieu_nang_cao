package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.model.PhuThuocHam;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BenchmarkAlgorithm {
    private final BaoDongAlgorithm baoDongAlgorithm;
    private final TimKhoaAlgorithm timKhoaAlgorithm;
    private final PhuToiThieuAlgorithm phuToiThieuAlgorithm;
    private final ChuanHoaAlgorithm chuanHoaAlgorithm;
    private final PhanRaBcnfAlgorithm phanRaBcnfAlgorithm;
    private final ChaseAlgorithm chaseAlgorithm;

    public BenchmarkAlgorithm(
            BaoDongAlgorithm baoDongAlgorithm,
            TimKhoaAlgorithm timKhoaAlgorithm,
            PhuToiThieuAlgorithm phuToiThieuAlgorithm,
            ChuanHoaAlgorithm chuanHoaAlgorithm,
            PhanRaBcnfAlgorithm phanRaBcnfAlgorithm,
            ChaseAlgorithm chaseAlgorithm) {
        this.baoDongAlgorithm = baoDongAlgorithm;
        this.timKhoaAlgorithm = timKhoaAlgorithm;
        this.phuToiThieuAlgorithm = phuToiThieuAlgorithm;
        this.chuanHoaAlgorithm = chuanHoaAlgorithm;
        this.phanRaBcnfAlgorithm = phanRaBcnfAlgorithm;
        this.chaseAlgorithm = chaseAlgorithm;
    }

    public KetQuaBenchmark chayDanhGiaHieuNang() {
        int[] kichThuocs = {5, 8, 12, 16, 20};
        List<BenchmarkPoint> diemDanhGia = new ArrayList<>();

        for (int n : kichThuocs) {
            Set<String> U = new LinkedHashSet<>();
            for (int i = 1; i <= n; i++) {
                U.add("A" + i);
            }

            // Tạo tập PTH tổng hợp dạng vòng xích + một số nhánh
            List<PhuThuocHam> F = new ArrayList<>();
            for (int i = 1; i < n; i++) {
                F.add(new PhuThuocHam(Set.of("A" + i), Set.of("A" + (i + 1))));
            }
            if (n >= 4) {
                F.add(new PhuThuocHam(Set.of("A" + n, "A1"), Set.of("A2")));
            }

            long t0 = System.nanoTime();
            baoDongAlgorithm.tinhBaoDong(Set.of("A1"), F);
            double thoiGianBaoDongMs = (System.nanoTime() - t0) / 1_000_000.0;

            t0 = System.nanoTime();
            timKhoaAlgorithm.timTatCaKhoa(U, F);
            double thoiGianTimKhoaMs = (System.nanoTime() - t0) / 1_000_000.0;

            t0 = System.nanoTime();
            phuToiThieuAlgorithm.timPhuToiThieu(F);
            double thoiGianPhuToiThieuMs = (System.nanoTime() - t0) / 1_000_000.0;

            t0 = System.nanoTime();
            chuanHoaAlgorithm.kiemTraDangChuan(U, F, true);
            double thoiGianKiemTraNfMs = (System.nanoTime() - t0) / 1_000_000.0;

            t0 = System.nanoTime();
            var decomp3nf = chuanHoaAlgorithm.phanRa3NF(U, F);
            double thoiGianPhanRa3nfMs = (System.nanoTime() - t0) / 1_000_000.0;

            t0 = System.nanoTime();
            phanRaBcnfAlgorithm.phanRaBcnf(U, F);
            double thoiGianPhanRaBcnfMs = (System.nanoTime() - t0) / 1_000_000.0;

            t0 = System.nanoTime();
            chaseAlgorithm.kiemTraLosslessJoin(U, decomp3nf.cacQuanHe(), F);
            double thoiGianChaseMs = (System.nanoTime() - t0) / 1_000_000.0;

            diemDanhGia.add(new BenchmarkPoint(
                    n,
                    F.size(),
                    thoiGianBaoDongMs,
                    thoiGianTimKhoaMs,
                    thoiGianPhuToiThieuMs,
                    thoiGianKiemTraNfMs,
                    thoiGianPhanRa3nfMs,
                    thoiGianPhanRaBcnfMs,
                    thoiGianChaseMs
            ));
        }

        String ketLuan = "Đánh giá độ phức tạp: Thuật toán Bao đóng X+ và Phủ tối thiểu tăng trưởng đa thức O(n*|F|). "
                + "Thuật toán tìm tất cả khóa có tính chất tổ hợp hàm mũ O(2^k) trong trường hợp xấu nhất khi số thuộc tính trung gian tăng cao; "
                + "tuy nhiên với phân loại thuộc tính Nguồn/Đích/Trung gian, thời gian chạy được tối ưu hóa đáng kể.";

        return new KetQuaBenchmark(diemDanhGia, ketLuan);
    }

    public record BenchmarkPoint(
            int soThuocTinh,
            int soPhuThuocHam,
            double baoDongMs,
            double timKhoaMs,
            double phuToiThieuMs,
            double kiemTraNfMs,
            double phanRa3nfMs,
            double phanRaBcnfMs,
            double chaseMs
    ) {}

    public record KetQuaBenchmark(
            List<BenchmarkPoint> cacDiemThuNghiem,
            String nhanXetDoPhucTap
    ) {}
}
