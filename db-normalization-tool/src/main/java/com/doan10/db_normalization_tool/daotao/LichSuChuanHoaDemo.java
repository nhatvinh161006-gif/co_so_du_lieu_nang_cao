package com.doan10.db_normalization_tool.daotao;

import com.doan10.db_normalization_tool.algorithm.BaoDongAlgorithm;
import com.doan10.db_normalization_tool.entity.LichSuChuanHoa;
import com.doan10.db_normalization_tool.model.PhuThuocHam;
import com.doan10.db_normalization_tool.service.LichSuChuanHoaService;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.*;

/**
 * Demo kiểm thử và nghiệm thu tích hợp Module Lưu Trữ Lịch Sử Chuẩn Hóa (Task 4)
 * Thực hiện:
 * 1. Gọi trực tiếp giải thuật BaoDongAlgorithm hiện có của Người 2
 * 2. Đo thời gian thực thi (milliseconds / nanoseconds)
 * 3. Lưu tự động kết quả giải thuật vào ObjectDB qua LichSuChuanHoaService
 * 4. Tạo thêm lịch sử mẫu cho các giải thuật khác (Phủ tối tiểu, Tìm khóa)
 * 5. Truy vấn và in danh sách lịch sử ra màn hình kiểm chứng
 */
public class LichSuChuanHoaDemo {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(Main.DB_PATH);
        try {
            runHistoryDemo(emf);
        } finally {
            emf.close();
        }
    }

    public static void runHistoryDemo(EntityManagerFactory emf) {
        System.out.println("======================================================================");
        System.out.println("  MODULE LƯU TRỮ LỊCH SỬ CHUẨN HÓA (OBJECTDB - TASK 4)");
        System.out.println("  Tích hợp giải thuật của Người 2 & Sẵn sàng cho Frontend Người 3");
        System.out.println("======================================================================\n");

        LichSuChuanHoaService service = new LichSuChuanHoaService(emf);

        // 1. Chạy thuật toán BaoDongAlgorithm của Người 2 trên tập thuộc tính mẫu
        System.out.println("----------------------------------------------------------------------");
        System.out.println(">>> 1. CHẠY THUẬT TOÁN TÍNH BAO ĐÓNG CỦA NGƯỜI 2 VÀ TỰ ĐỘNG LƯU OBJECTDB");
        System.out.println("----------------------------------------------------------------------");

        String tapU_Str = "A, B, C, D, E, G, H";
        Set<String> tapX = new HashSet<>(Arrays.asList("A", "B"));
        List<PhuThuocHam> tapF = new ArrayList<>();
        tapF.add(new PhuThuocHam(new HashSet<>(Arrays.asList("A", "B")), new HashSet<>(Collections.singletonList("C")))); // AB -> C
        tapF.add(new PhuThuocHam(new HashSet<>(Collections.singletonList("B")), new HashSet<>(Collections.singletonList("D"))));       // B -> D
        tapF.add(new PhuThuocHam(new HashSet<>(Arrays.asList("C", "D")), new HashSet<>(Collections.singletonList("E")))); // CD -> E
        tapF.add(new PhuThuocHam(new HashSet<>(Collections.singletonList("E")), new HashSet<>(Collections.singletonList("A"))));       // E -> A

        String tapF_Str = "AB -> C, B -> D, CD -> E, E -> A";
        String tapX_Str = "A, B";

        System.out.println("[-] Đầu vào:");
        System.out.println("    + Tập thuộc tính U: " + tapU_Str);
        System.out.println("    + Tập phụ thuộc hàm F: " + tapF_Str);
        System.out.println("    + Tính bao đóng cho tập X: {" + tapX_Str + "}+");

        // Đo thời gian tính toán của giải thuật
        long startNano = System.nanoTime();
        BaoDongAlgorithm baoDongAlgo = new BaoDongAlgorithm();
        Set<String> ketQuaBaoDong = baoDongAlgo.tinhBaoDong(tapX, tapF);
        long durationNano = System.nanoTime() - startNano;
        long durationMs = durationNano / 1_000_000;

        List<String> sortedResult = new ArrayList<>(ketQuaBaoDong);
        Collections.sort(sortedResult);
        String ketQuaChiTiet = "Bao đóng của {" + tapX_Str + "}+ = {" + String.join(", ", sortedResult) + "}. " +
                "Các bước duyệt phụ thuộc hàm: (1) Áp dụng AB->C => thêm C; (2) Áp dụng B->D => thêm D; (3) Áp dụng CD->E => thêm E; " +
                "Tập bao đóng cuối cùng đạt trạng thái bão hòa.";

        System.out.println("[-] Kết quả giải thuật tính được: {" + String.join(", ", sortedResult) + "}+");
        System.out.println("[-] Thời gian thực thi: " + durationNano + " ns (~" + durationMs + " ms)");

        // 2. Tự động lưu vào CSDL ObjectDB
        LichSuChuanHoa ls1 = service.luuLichSu(
                tapU_Str,
                tapF_Str,
                "TINH_BAO_DONG",
                "{" + tapX_Str + "}+",
                ketQuaChiTiet,
                durationMs,
                true
        );
        System.out.println(">>> ĐÃ LƯU THÀNH CÔNG VÀO OBJECTDB VỚI OID = " + ls1.getId());

        // 3. Giả lập lưu thêm 2 lượt chạy khác (Tìm Khóa và Phủ Tối Tiểu)
        System.out.println("\n[-] Đang nạp thêm các lượt chạy lịch sử của các giải thuật khác...");
        service.luuLichSu(
                "A, B, C, D, E",
                "A -> BC, CD -> E, B -> D, E -> A",
                "TIM_TAT_CA_KHOA",
                "Toàn bộ U",
                "Các khóa tối thiểu tìm được: {A}, {E}, {C, D}, {B, C}. Thuật toán đã duyệt cây nhị phân bao đóng và loại bỏ siêu khóa.",
                12,
                true
        );

        service.luuLichSu(
                "A, B, C, D",
                "A -> B, B -> C, A -> C, AB -> D",
                "PHU_TOI_TIEU",
                "Tối tiểu hóa tập F",
                "Bước 1: Tách vế phải đơn. Bước 2: Loại bỏ thuộc tính dư thừa vế trái (AB->D thành A->D). Bước 3: Loại bỏ phụ thuộc hàm dư thừa (A->C vì đã có A->B, B->C). Phủ tối tiểu F_min = {A->B, B->C, A->D}.",
                15,
                true
        );

        // 4. Truy vấn lại danh sách lịch sử từ ObjectDB
        System.out.println("\n----------------------------------------------------------------------");
        System.out.println(">>> 2. TRUY VẤN DANH SÁCH LỊCH SỬ CHUẨN HÓA TỪ OBJECTDB (MỚI NHẤT TRƯỚC)");
        System.out.println("----------------------------------------------------------------------");

        List<LichSuChuanHoa> danhSach = service.layTatCaLichSu();
        for (LichSuChuanHoa item : danhSach) {
            System.out.println("----------------------------------------------------------------------");
            System.out.println("[OID: " + item.getId() + "] Thuật toán: " + item.getLoaiThuatToan() 
                    + " | Thời gian chạy: " + item.getThoiGianChay() 
                    + " (" + item.getThoiGianThucThiMs() + " ms)");
            System.out.println("  + Tập thuộc tính U: " + item.getTapThuocTinhU());
            System.out.println("  + Phụ thuộc hàm F: " + item.getTapPhuThuocHamF());
            if (item.getDauVaoBoSung() != null && !item.getDauVaoBoSung().isEmpty()) {
                System.out.println("  + Đầu vào phụ:     " + item.getDauVaoBoSung());
            }
            System.out.println("  + Kết quả chi tiết: " + item.getKetQuaChiTiet());
            System.out.println("  + Trạng thái:       " + (item.isThanhCong() ? "THÀNH CÔNG" : "THẤT BẠI"));
        }

        System.out.println("\n======================================================================");
        System.out.println("  HOÀN THÀNH KIỂM CHỨNG MODULE LỊCH SỬ CHUẨN HÓA TRONG OBJECTDB!");
        System.out.println("======================================================================\n");
    }
}
