package com.doan10.db_normalization_tool.daotao;

import com.doan10.db_normalization_tool.entity.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.io.File;
import java.util.List;

/**
 * Điểm khởi chạy (Entry Point) thực thi và nghiệm thu CSDL Hướng đối tượng (ObjectDB)
 * Thực hiện:
 * 1. Khởi tạo / Làm sạch file CSDL db/daotao.odb
 * 2. Gọi DatabaseInitializer nạp dữ liệu mẫu phong phú (Transaction Commit)
 * 3. Gọi DatabaseInitializer kiểm thử tính nguyên tử (Transaction Rollback)
 * 4. Thống kê tổng quan dữ liệu đối tượng trong CSDL ObjectDB
 */
public class Main {
    public static final String DB_PATH = "db/daotao.odb";

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("  CSDL HƯỚNG ĐỐI TƯỢNG (OBJECTDB) - PHÂN HỆ QUẢN LÝ ĐÀO TẠO");
        System.out.println("  Học phần: Cơ sở dữ liệu nâng cao | Nhánh thực hiện: MaGiaVy");
        System.out.println("======================================================================");

        // Làm sạch file CSDL cũ để bảo đảm tính tái lập kết quả
        File dbFile = new File(DB_PATH);
        if (dbFile.exists()) {
            dbFile.delete();
            File dbFile$ = new File(DB_PATH + "$");
            if (dbFile$.exists()) dbFile$.delete();
            System.out.println("[-] Đã làm mới file CSDL: " + DB_PATH + "\n");
        }

        EntityManagerFactory emf = Persistence.createEntityManagerFactory(DB_PATH);

        try {
            // Bước 1: Nạp dữ liệu mẫu bằng Transaction Commit
            DatabaseInitializer.initSampleData(emf);

            // Bước 2: Kiểm thử Transaction Rollback khi gặp ngoại lệ nghiệp vụ
            DatabaseInitializer.testTransactionRollback(emf);

            // Bước 3: Thống kê số lượng đối tượng trong ObjectDB
            System.out.println("\n----------------------------------------------------------------------");
            System.out.println(">>> 3. THỐNG KÊ TỔNG QUAN DỮ LIỆU ĐỐI TƯỢNG TRONG OBJECTDB");
            System.out.println("----------------------------------------------------------------------");

            EntityManager em = emf.createEntityManager();
            try {
                long countGV = em.createQuery("SELECT COUNT(g) FROM GiangVien g", Long.class).getSingleResult();
                long countSV = em.createQuery("SELECT COUNT(s) FROM SinhVien s", Long.class).getSingleResult();
                long countHP = em.createQuery("SELECT COUNT(h) FROM HocPhan h", Long.class).getSingleResult();
                long countLHP = em.createQuery("SELECT COUNT(l) FROM LopHocPhan l", Long.class).getSingleResult();
                long countPH = em.createQuery("SELECT COUNT(p) FROM PhongHoc p", Long.class).getSingleResult();
                long countDK = em.createQuery("SELECT COUNT(d) FROM DangKyMon d", Long.class).getSingleResult();

                System.out.println("  + Tổng số Giảng Viên:   " + countGV + " (Yêu cầu: >= 3) -> ĐẠT");
                System.out.println("  + Tổng số Sinh Viên:    " + countSV + " (Yêu cầu: >= 5) -> ĐẠT");
                System.out.println("  + Tổng số Môn Học (HP): " + countHP + " (Yêu cầu: >= 3) -> ĐẠT");
                System.out.println("  + Tổng số Lớp Học Phần: " + countLHP + " (Yêu cầu: >= 4) -> ĐẠT");
                System.out.println("  + Tổng số Phòng Học:    " + countPH);
                System.out.println("  + Tổng số Lượt Đăng Ký: " + countDK);

                System.out.println("\n>>> CHI TIẾT DANH SÁCH SINH VIÊN & XẾP LOẠI HỌC LỰC:");
                TypedQuery<SinhVien> svQuery = em.createQuery("SELECT s FROM SinhVien s ORDER BY s.maSV", SinhVien.class);
                for (SinhVien sv : svQuery.getResultList()) {
                    System.out.println("  - " + sv.getMaSV() + ": " + sv.getHoTen() 
                            + " (" + sv.getGioiTinh() + ") | ĐTB: " + sv.tinhDiemTrungBinh() 
                            + " | Tích lũy: " + sv.tongTinChiTichLuy() + " TC | Xếp loại: " + sv.xepLoaiHocLuc()
                            + " | Số môn học: " + sv.getDsDangKy().size());
                }

                System.out.println("\n>>> CHI TIẾT DANH SÁCH LỚP HỌC PHẦN & PHÒNG HỌC:");
                TypedQuery<LopHocPhan> lhpQuery = em.createQuery("SELECT l FROM LopHocPhan l ORDER BY l.maLHP", LopHocPhan.class);
                for (LopHocPhan lhp : lhpQuery.getResultList()) {
                    System.out.println("  - " + lhp.getMaLHP() + ": " + lhp.getHocPhan().getTenHP() 
                            + " | GV: " + lhp.getGiangVien().getHoTen() 
                            + " | Phòng: " + lhp.getPhongHoc().getMaPhong() + " (" + lhp.getPhongHoc().getToaNha() + ")"
                            + " | Lịch: " + lhp.getThongTinLichHoc()
                            + " | Sĩ số: " + lhp.getSiSoHienTai());
                }

            } finally {
                em.close();
            }

            // Bước 4: Thực thi bộ 10 truy vấn OQL / JPQL chuyên sâu (Task 3)
            System.out.println();
            ObjectDbQueryDemo.runAllQueries(emf);

            // Bước 5: Thực thi module lưu trữ lịch sử chuẩn hóa (Task 4)
            System.out.println();
            LichSuChuanHoaDemo.runHistoryDemo(emf);

        } finally {
            emf.close();
        }

        System.out.println("\n======================================================================");
        System.out.println("  HOÀN THÀNH NGHIỆM THU TẤT CẢ CÁC MODULE OBJECTDB (TASK 1 -> TASK 4)!");
        System.out.println("======================================================================");
    }
}
