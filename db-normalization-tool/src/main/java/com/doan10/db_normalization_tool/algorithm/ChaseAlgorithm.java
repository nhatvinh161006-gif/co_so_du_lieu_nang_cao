package com.doan10.db_normalization_tool.algorithm;

import com.doan10.db_normalization_tool.model.PhuThuocHam;
import com.doan10.db_normalization_tool.model.StepLog;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChaseAlgorithm {

    public KetQuaChase kiemTraLosslessJoin(
            Set<String> tapThuocTinh,
            List<Set<String>> phanRa,
            List<PhuThuocHam> tapF) {
        List<StepLog> buocGiai = new ArrayList<>();
        List<String> thuocTinh = new ArrayList<>(tapThuocTinh);
        int n = thuocTinh.size();
        int k = phanRa.size();

        buocGiai.add(new StepLog(1, "Khởi tạo bảng Chase",
                "Khởi tạo bảng kích thước " + k + " hàng x " + n + " cột ứng với "
                        + k + " quan hệ con và " + n + " thuộc tính của U."));

        // Ma trận bảng Chase: T[k][n]
        String[][] T = new String[k][n];
        for (int i = 0; i < k; i++) {
            Set<String> Ri = phanRa.get(i);
            for (int j = 0; j < n; j++) {
                String attr = thuocTinh.get(j);
                if (Ri.contains(attr)) {
                    T[i][j] = "a_" + attr; // Ký hiệu phân biệt
                } else {
                    T[i][j] = "b_" + (i + 1) + "_" + attr; // Ký hiệu không phân biệt
                }
            }
        }

        List<String> bangBanDau = inBang(T, thuocTinh, phanRa);
        buocGiai.add(new StepLog(buocGiai.size() + 1, "Bảng Chase ban đầu",
                "Đã điền ký hiệu phân biệt (a) cho thuộc tính có mặt trong quan hệ con, và (b) cho thuộc tính vắng mặt."));

        boolean thayDoi;
        int vongLap = 0;
        Integer hangChuaToanA = null;

        do {
            thayDoi = false;
            vongLap++;

            for (PhuThuocHam fd : tapF) {
                // Lấy danh sách chỉ số cột thuộc vế trái X và vế phải Y
                List<Integer> cotX = new ArrayList<>();
                for (String attrX : fd.getVT()) {
                    int idx = thuocTinh.indexOf(attrX);
                    if (idx != -1) cotX.add(idx);
                }
                List<Integer> cotY = new ArrayList<>();
                for (String attrY : fd.getVP()) {
                    int idx = thuocTinh.indexOf(attrY);
                    if (idx != -1) cotY.add(idx);
                }

                if (cotX.isEmpty() || cotY.isEmpty()) continue;

                // Gom nhóm các hàng có cùng giá trị trên các cột X
                Map<String, List<Integer>> nhomHang = new LinkedHashMap<>();
                for (int i = 0; i < k; i++) {
                    StringBuilder key = new StringBuilder();
                    for (int cX : cotX) {
                        key.append(T[i][cX]).append("|");
                    }
                    nhomHang.computeIfAbsent(key.toString(), ignore -> new ArrayList<>()).add(i);
                }

                // Với mỗi nhóm có >= 2 hàng, đồng nhất giá trị trên các cột Y
                for (List<Integer> hangCungX : nhomHang.values()) {
                    if (hangCungX.size() < 2) continue;

                    for (int cY : cotY) {
                        String attrY = thuocTinh.get(cY);

                        // Tìm giá trị mục tiêu (ưu tiên ký hiệu a)
                        String giaTriMucTieu = null;
                        for (int h : hangCungX) {
                            if (T[h][cY].startsWith("a_")) {
                                giaTriMucTieu = T[h][cY];
                                break;
                            }
                        }
                        if (giaTriMucTieu == null) {
                            // Nếu không có 'a', chọn ký hiệu b có chỉ số hàng nhỏ nhất
                            giaTriMucTieu = T[hangCungX.get(0)][cY];
                            for (int h : hangCungX) {
                                if (T[h][cY].compareTo(giaTriMucTieu) < 0) {
                                    giaTriMucTieu = T[h][cY];
                                }
                            }
                        }

                        // Áp dụng đồng nhất
                        List<Integer> hangBiDoi = new ArrayList<>();
                        for (int h : hangCungX) {
                            if (!T[h][cY].equals(giaTriMucTieu)) {
                                T[h][cY] = giaTriMucTieu;
                                hangBiDoi.add(h + 1);
                                thayDoi = true;
                            }
                        }

                        if (!hangBiDoi.isEmpty()) {
                            List<Integer> cacHang = hangCungX.stream().map(h -> h + 1).toList();
                            buocGiai.add(new StepLog(buocGiai.size() + 1, "Đồng nhất bảng (Vòng " + vongLap + ")",
                                    "Áp dụng " + fd + ": Các hàng R" + cacHang
                                            + " có cùng giá trị trên " + fd.getVT()
                                            + "; đồng nhất cột " + attrY + " của các hàng R" + hangBiDoi
                                            + " thành ký hiệu " + giaTriMucTieu + "."));
                        }
                    }
                }

                // Kiểm tra xem có hàng nào đã đạt toàn bộ 'a' chưa
                for (int i = 0; i < k; i++) {
                    boolean toanA = true;
                    for (int j = 0; j < n; j++) {
                        if (!T[i][j].startsWith("a_")) {
                            toanA = false;
                            break;
                        }
                    }
                    if (toanA) {
                        hangChuaToanA = i + 1;
                        break;
                    }
                }
                if (hangChuaToanA != null) {
                    break;
                }
            }
        } while (thayDoi && hangChuaToanA == null);

        List<String> bangKetQua = inBang(T, thuocTinh, phanRa);
        boolean baoToan = hangChuaToanA != null;

        if (baoToan) {
            buocGiai.add(new StepLog(buocGiai.size() + 1, "Kết luận: Nối không mất mát thông tin (Lossless Join)",
                    "Hàng R" + hangChuaToanA
                            + " đã chuyển thành toàn bộ ký hiệu phân biệt (a). Theo thuật toán Chase, phép phân rã ĐẢM BẢO NỐI KHÔNG MẤT MÁT THÔNG TIN."));
        } else {
            buocGiai.add(new StepLog(buocGiai.size() + 1, "Kết luận: Phép nối BỊ MẤT MÁT (Lossy Join)",
                    "Bảng Chase đã đạt trạng thái dừng cố định nhưng không có hàng nào chứa toàn bộ ký hiệu phân biệt (a). Phép phân rã BỊ MẤT MÁT THÔNG TIN khi kết nối lại."));
        }

        return new KetQuaChase(
                baoToan,
                hangChuaToanA,
                thuocTinh,
                bangBanDau,
                bangKetQua,
                buocGiai
        );
    }

    private List<String> inBang(String[][] T, List<String> thuocTinh, List<Set<String>> phanRa) {
        List<String> lines = new ArrayList<>();
        StringBuilder header = new StringBuilder(String.format("%-8s | ", "Lược đồ"));
        for (String attr : thuocTinh) {
            header.append(String.format("%-12s | ", attr));
        }
        lines.add(header.toString());

        StringBuilder sep = new StringBuilder("-".repeat(header.length()));
        lines.add(sep.toString());

        for (int i = 0; i < T.length; i++) {
            StringBuilder row = new StringBuilder(String.format("%-8s | ", "R" + (i + 1)));
            for (int j = 0; j < T[i].length; j++) {
                row.append(String.format("%-12s | ", T[i][j]));
            }
            lines.add(row.toString());
        }
        return lines;
    }

    public record KetQuaChase(
            boolean baoToanThongTin,
            Integer hangThoaManToanA,
            List<String> danhSachThuocTinh,
            List<String> bangBanDau,
            List<String> bangKetQua,
            List<StepLog> buocGiai
    ) {}
}
