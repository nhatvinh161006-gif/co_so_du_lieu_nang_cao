package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.model.PhuThuocHam;
import com.doan10.db_normalization_tool.model.StepLog;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;

@Service
public class SieuKhoaAlgorithm {
    private static final int GIOI_HAN_MAU = 100;
    private final TimKhoaAlgorithm timKhoaAlgorithm;

    public SieuKhoaAlgorithm(TimKhoaAlgorithm timKhoaAlgorithm) {
        this.timKhoaAlgorithm = timKhoaAlgorithm;
    }

    public KetQuaSieuKhoa timTatCaSieuKhoa(Set<String> tapThuocTinh, List<PhuThuocHam> tapF, Integer limit) {
        List<StepLog> buocGiai = new ArrayList<>();
        int gioiHan = (limit == null || limit <= 0) ? GIOI_HAN_MAU : limit;

        buocGiai.add(new StepLog(1, "Tìm khóa ứng viên",
                "Mọi siêu khóa đều là tập cha của ít nhất một khóa ứng viên tối tiểu. Bắt đầu tìm tất cả khóa ứng viên."));

        var ketQuaKhoa = timKhoaAlgorithm.timTatCaKhoa(tapThuocTinh, tapF);
        List<Set<String>> khoaUngVien = ketQuaKhoa.khoaUngVien();

        for (StepLog step : ketQuaKhoa.buocGiai()) {
            buocGiai.add(new StepLog(buocGiai.size() + 1, "Tìm khóa: " + step.action(), step.detail()));
        }

        if (khoaUngVien.isEmpty()) {
            buocGiai.add(new StepLog(buocGiai.size() + 1, "Kết luận",
                    "Không tìm thấy khóa ứng viên nào => Không tồn tại siêu khóa."));
            return new KetQuaSieuKhoa(
                    List.of(),
                    BigInteger.ZERO,
                    Map.of(),
                    List.of(),
                    true,
                    buocGiai
            );
        }

        int n = tapThuocTinh.size();
        int m = khoaUngVien.size();
        buocGiai.add(new StepLog(buocGiai.size() + 1, "Tổng hợp khóa ứng viên",
                "Tìm được " + m + " khóa ứng viên: " + khoaUngVien
                        + ". Mọi tập cha của các khóa này trong tập thuộc tính (|U| = " + n + ") là một siêu khóa."));

        // Tính chính xác tổng số siêu khóa bằng nguyên lý bù trừ (Principle of Inclusion-Exclusion)
        BigInteger tongSoSieuKhoa = tinhTongSoSieuKhoaPIE(tapThuocTinh, khoaUngVien);
        buocGiai.add(new StepLog(buocGiai.size() + 1, "Tính tổng số siêu khóa",
                "Áp dụng Nguyên lý Bù trừ (Inclusion-Exclusion) trên " + m + " khóa ứng viên: Tổng cộng có "
                        + tongSoSieuKhoa + " siêu khóa."));

        // Tính phân bố số lượng siêu khóa theo từng kích thước
        Map<Integer, BigInteger> phanBoKichThuoc = tinhPhanBoTheoKichThuocPIE(tapThuocTinh, khoaUngVien);

        // Liệt kê mẫu các siêu khóa (sắp xếp theo kích thước tăng dần)
        List<Set<String>> danhSachMau = lietKeSieuKhoaMau(tapThuocTinh, khoaUngVien, gioiHan);
        boolean daLietKeHet = tongSoSieuKhoa.compareTo(BigInteger.valueOf(danhSachMau.size())) <= 0;

        buocGiai.add(new StepLog(buocGiai.size() + 1, "Liệt kê danh sách siêu khóa",
                (daLietKeHet
                        ? "Đã liệt kê toàn bộ " + danhSachMau.size() + " siêu khóa."
                        : "Đã liệt kê " + danhSachMau.size() + " siêu khóa đầu tiên (ưu tiên kích thước nhỏ) trong tổng số " + tongSoSieuKhoa + " siêu khóa.")));

        return new KetQuaSieuKhoa(
                khoaUngVien,
                tongSoSieuKhoa,
                phanBoKichThuoc,
                danhSachMau,
                daLietKeHet,
                buocGiai
        );
    }

    private BigInteger tinhTongSoSieuKhoaPIE(Set<String> U, List<Set<String>> keys) {
        int m = keys.size();
        int totalAttributes = U.size();
        BigInteger total = BigInteger.ZERO;

        int totalSubsets = 1 << m;
        for (int mask = 1; mask < totalSubsets; mask++) {
            Set<String> unionOfKeys = new LinkedHashSet<>();
            int bits = 0;
            for (int i = 0; i < m; i++) {
                if ((mask & (1 << i)) != 0) {
                    unionOfKeys.addAll(keys.get(i));
                    bits++;
                }
            }
            int remaining = totalAttributes - unionOfKeys.size();
            BigInteger supersetsCount = BigInteger.valueOf(2).pow(remaining);

            if (bits % 2 == 1) {
                total = total.add(supersetsCount);
            } else {
                total = total.subtract(supersetsCount);
            }
        }
        return total;
    }

    private Map<Integer, BigInteger> tinhPhanBoTheoKichThuocPIE(Set<String> U, List<Set<String>> keys) {
        Map<Integer, BigInteger> result = new LinkedHashMap<>();
        int m = keys.size();
        int totalAttributes = U.size();

        int minKeySize = keys.stream().mapToInt(Set::size).min().orElse(0);
        for (int k = minKeySize; k <= totalAttributes; k++) {
            BigInteger countForK = BigInteger.ZERO;
            int totalSubsets = 1 << m;

            for (int mask = 1; mask < totalSubsets; mask++) {
                Set<String> unionOfKeys = new LinkedHashSet<>();
                int bits = 0;
                for (int i = 0; i < m; i++) {
                    if ((mask & (1 << i)) != 0) {
                        unionOfKeys.addAll(keys.get(i));
                        bits++;
                    }
                }
                int unionSize = unionOfKeys.size();
                if (k >= unionSize) {
                    BigInteger combinations = toHop(totalAttributes - unionSize, k - unionSize);
                    if (bits % 2 == 1) {
                        countForK = countForK.add(combinations);
                    } else {
                        countForK = countForK.subtract(combinations);
                    }
                }
            }
            if (countForK.compareTo(BigInteger.ZERO) > 0) {
                result.put(k, countForK);
            }
        }
        return result;
    }

    private BigInteger toHop(int n, int k) {
        if (k < 0 || k > n) return BigInteger.ZERO;
        if (k == 0 || k == n) return BigInteger.ONE;
        k = Math.min(k, n - k);
        BigInteger c = BigInteger.ONE;
        for (int i = 1; i <= k; i++) {
            c = c.multiply(BigInteger.valueOf(n - i + 1)).divide(BigInteger.valueOf(i));
        }
        return c;
    }

    private List<Set<String>> lietKeSieuKhoaMau(Set<String> U, List<Set<String>> keys, int limit) {
        List<Set<String>> result = new ArrayList<>();
        Set<Set<String>> visited = new HashSet<>();

        // Bắt đầu từ các khóa ứng viên (siêu khóa tối tiểu)
        Queue<Set<String>> queue = new ArrayDeque<>();
        for (Set<String> key : keys) {
            Set<String> keyCopy = new LinkedHashSet<>(key);
            if (visited.add(keyCopy)) {
                result.add(keyCopy);
                queue.add(keyCopy);
                if (result.size() >= limit) return result;
            }
        }

        List<String> attributeList = new ArrayList<>(U);
        while (!queue.isEmpty() && result.size() < limit) {
            Set<String> current = queue.poll();
            for (String attr : attributeList) {
                if (!current.contains(attr)) {
                    Set<String> next = new LinkedHashSet<>(current);
                    next.add(attr);
                    if (visited.add(next)) {
                        result.add(next);
                        queue.add(next);
                        if (result.size() >= limit) return result;
                    }
                }
            }
        }
        return result;
    }

    public record KetQuaSieuKhoa(
            List<Set<String>> khoaUngVien,
            BigInteger tongSoSieuKhoa,
            Map<Integer, BigInteger> phanBoTheoKichThuoc,
            List<Set<String>> danhSachSieuKhoaMau,
            boolean daLietKeHet,
            List<StepLog> buocGiai
    ) {}
}
