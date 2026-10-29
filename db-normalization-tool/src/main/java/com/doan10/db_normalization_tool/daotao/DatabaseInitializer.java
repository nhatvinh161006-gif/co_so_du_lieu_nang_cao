package com.doan10.db_normalization_tool.daotao;

import com.doan10.db_normalization_tool.entity.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Class chuyên trách khởi tạo cơ sở dữ liệu mẫu và kiểm thử Transaction (Task 2)
 * Thể hiện:
 * 1. Khởi tạo CSDL ObjectDB và nạp dữ liệu mẫu phong phú trong 1 Transaction Commit thành công
 * 2. Kiểm thử tính toàn vẹn và nguyên tử (ACID - Atomicity) qua kịch bản Transaction Rollback
 */
public class DatabaseInitializer {

    /**
     * Nạp toàn bộ dữ liệu mẫu theo đúng yêu cầu đồ án:
     * - 4 Phòng học
     * - 4 Môn học (Học phần) có quan hệ tiên quyết
     * - 3 Giảng viên
     * - 5 Lớp học phần
     * - 5 Sinh viên với các đăng ký môn học và 3 đầu điểm (CC 10%, GK 30%, CK 60%)
     */
    public static void initSampleData(EntityManagerFactory emf) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println(">>> 1. KHỞI TẠO DỮ LIỆU MẪU BAN ĐẦU QUA TRANSACTION (COMMIT THÀNH CÔNG)");
        System.out.println("----------------------------------------------------------------------");

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // 1. Khởi tạo 4 phòng học
            PhongHoc phA101 = new PhongHoc("A101", "Tòa A", 60, "Lý thuyết");
            PhongHoc phA102 = new PhongHoc("A102", "Tòa A", 80, "Lý thuyết");
            PhongHoc phLAB01 = new PhongHoc("LAB01", "Tòa B", 40, "Thực hành");
            PhongHoc phLAB02 = new PhongHoc("LAB02", "Tòa B", 45, "Thực hành");
            em.persist(phA101);
            em.persist(phA102);
            em.persist(phLAB01);
            em.persist(phLAB02);

            // 2. Khởi tạo 4 học phần (Có tự liên kết tiên quyết)
            HocPhan hpCSDL101 = new HocPhan("CSDL101", "Cơ Sở Dữ Liệu Cơ Bản", 3);
            em.persist(hpCSDL101);

            HocPhan hpCSDL201 = new HocPhan("CSDL201", "Cơ Sở Dữ Liệu Nâng Cao", 3, hpCSDL101); // CSDL101 là tiên quyết
            HocPhan hpCNPM101 = new HocPhan("CNPM101", "Công Nghệ Phần Mềm", 4);
            HocPhan hpTRR102  = new HocPhan("TRR102", "Toán Rời Rạc", 3);
            HocPhan hpAI301   = new HocPhan("AI301", "Trí Tuệ Nhân Tạo", 3); // Môn học chưa mở lớp (phục vụ test IS EMPTY)
            em.persist(hpCSDL201);
            em.persist(hpCNPM101);
            em.persist(hpTRR102);
            em.persist(hpAI301);

            // 3. Khởi tạo 3 giảng viên
            GiangVien gv1 = new GiangVien("TS. Tran Van B", "1980-05-12", "Nam", "GV001", "Tiến sĩ");
            GiangVien gv2 = new GiangVien("ThS. Le Thi C", "1985-09-20", "Nữ", "GV002", "Thạc sĩ");
            GiangVien gv3 = new GiangVien("PGS.TS. Pham Van D", "1975-12-08", "Nam", "GV003", "Phó Giáo Sư");
            em.persist(gv1);
            em.persist(gv2);
            em.persist(gv3);

            // 4. Khởi tạo 5 lớp học phần
            // LHP00: CSDL Cơ Bản - Thứ 3 Tiết 1 - Phòng A101
            LopHocPhan lhp00 = new LopHocPhan("LHP00", "HK2-2025", 3, 1, hpCSDL101, gv1, phA101);
            // LHP01: CSDL Nâng Cao (Lớp 1) - Thứ 2 Tiết 1 - Phòng A101
            LopHocPhan lhp01 = new LopHocPhan("LHP01", "HK1-2026", 2, 1, hpCSDL201, gv1, phA101);
            // LHP02: Công Nghệ Phần Mềm - Thứ 4 Tiết 7 - Phòng LAB01
            LopHocPhan lhp02 = new LopHocPhan("LHP02", "HK1-2026", 4, 7, hpCNPM101, gv2, phLAB01);
            // LHP03: Toán Rời Rạc - Thứ 6 Tiết 1 - Phòng A102
            LopHocPhan lhp03 = new LopHocPhan("LHP03", "HK1-2026", 6, 1, hpTRR102, gv3, phA102);
            // LHP04: CSDL Nâng Cao (Lớp 2) - Thứ 5 Tiết 1 - Phòng LAB02
            LopHocPhan lhp04 = new LopHocPhan("LHP04", "HK1-2026", 5, 1, hpCSDL201, gv2, phLAB02);

            // Thiết lập quan hệ 2 chiều
            hpCSDL101.themLopHocPhan(lhp00);
            hpCSDL201.themLopHocPhan(lhp01);
            hpCNPM101.themLopHocPhan(lhp02);
            hpTRR102.themLopHocPhan(lhp03);
            hpCSDL201.themLopHocPhan(lhp04);

            gv1.phanCongLopDay(lhp00);
            gv1.phanCongLopDay(lhp01);
            gv2.phanCongLopDay(lhp02);
            gv3.phanCongLopDay(lhp03);
            gv2.phanCongLopDay(lhp04);

            em.persist(lhp00);
            em.persist(lhp01);
            em.persist(lhp02);
            em.persist(lhp03);
            em.persist(lhp04);

            // 5. Khởi tạo 5 sinh viên & các lượt đăng ký môn học
            // SV001: Nguyen Van A (Nam) - Đạt điểm cao
            SinhVien sv1 = new SinhVien("Nguyen Van A", "2004-11-15", "Nam", "SV001");
            DangKyMon dk1_0 = new DangKyMon(sv1, lhp00, "2025-01-10", 8.0, 8.5, 8.0); // CSDL Cơ bản: 8.15
            DangKyMon dk1_1 = new DangKyMon(sv1, lhp01, "2026-09-01", 9.0, 8.0, 8.5); // CSDL NC: 8.4
            DangKyMon dk1_2 = new DangKyMon(sv1, lhp02, "2026-09-02", 10.0, 9.0, 9.0); // CNPM: 9.1
            sv1.themDangKy(dk1_0);
            sv1.themDangKy(dk1_1);
            sv1.themDangKy(dk1_2);
            lhp00.themDangKy(dk1_0);
            lhp01.themDangKy(dk1_1);
            lhp02.themDangKy(dk1_2);
            em.persist(sv1);

            // SV002: Tran Thi Hoa (Nữ) - Học lực Khá
            SinhVien sv2 = new SinhVien("Tran Thi Hoa", "2004-03-22", "Nữ", "SV002");
            DangKyMon dk2_1 = new DangKyMon(sv2, lhp01, "2026-09-03", 8.0, 7.0, 7.0); // CSDL NC: 7.1
            DangKyMon dk2_3 = new DangKyMon(sv2, lhp03, "2026-09-03", 7.0, 6.5, 6.5); // TRR: 6.55
            sv2.themDangKy(dk2_1);
            sv2.themDangKy(dk2_3);
            lhp01.themDangKy(dk2_1);
            lhp03.themDangKy(dk2_3);
            em.persist(sv2);

            // SV003: Le Hoang Nam (Nam) - Học lực Xuất sắc (Điểm CSDL NC đạt 9.5)
            SinhVien sv3 = new SinhVien("Le Hoang Nam", "2004-08-10", "Nam", "SV003");
            DangKyMon dk3_0 = new DangKyMon(sv3, lhp00, "2025-01-11", 9.5, 9.0, 9.5); // CSDL Cơ bản: 9.35
            DangKyMon dk3_1 = new DangKyMon(sv3, lhp01, "2026-09-01", 10.0, 9.5, 9.5); // CSDL NC: 9.55
            DangKyMon dk3_2 = new DangKyMon(sv3, lhp02, "2026-09-02", 9.0, 9.0, 9.0); // CNPM: 9.0
            sv3.themDangKy(dk3_0);
            sv3.themDangKy(dk3_1);
            sv3.themDangKy(dk3_2);
            lhp00.themDangKy(dk3_0);
            lhp01.themDangKy(dk3_1);
            lhp02.themDangKy(dk3_2);
            em.persist(sv3);

            // SV004: Pham Minh Tuan (Nam) - Học lực Yếu (Rớt môn CSDL Nâng Cao)
            SinhVien sv4 = new SinhVien("Pham Minh Tuan", "2004-12-05", "Nam", "SV004");
            DangKyMon dk4_1 = new DangKyMon(sv4, lhp04, "2026-09-04", 5.0, 4.0, 3.5); // CSDL NC: 3.8 (F - Rớt)
            DangKyMon dk4_3 = new DangKyMon(sv4, lhp03, "2026-09-04", 6.0, 5.0, 5.0); // TRR: 5.1
            sv4.themDangKy(dk4_1);
            sv4.themDangKy(dk4_3);
            lhp04.themDangKy(dk4_1);
            lhp03.themDangKy(dk4_3);
            em.persist(sv4);

            // SV005: Vu Thi Mai (Nữ) - Học lực Giỏi
            SinhVien sv5 = new SinhVien("Vu Thi Mai", "2004-05-18", "Nữ", "SV005");
            DangKyMon dk5_0 = new DangKyMon(sv5, lhp00, "2025-01-12", 8.5, 8.0, 8.5); // CSDL Cơ bản: 8.35
            DangKyMon dk5_1 = new DangKyMon(sv5, lhp01, "2026-09-01", 9.0, 9.0, 8.5); // CSDL NC: 8.7
            DangKyMon dk5_2 = new DangKyMon(sv5, lhp02, "2026-09-02", 8.5, 8.0, 8.0); // CNPM: 8.05
            sv5.themDangKy(dk5_0);
            sv5.themDangKy(dk5_1);
            sv5.themDangKy(dk5_2);
            lhp00.themDangKy(dk5_0);
            lhp01.themDangKy(dk5_1);
            lhp02.themDangKy(dk5_2);
            em.persist(sv5);

            em.getTransaction().commit();
            System.out.println(">>> TRANSACTION COMMIT THÀNH CÔNG! Đã khởi tạo đầy đủ đồ thị đối tượng vào ObjectDB.");

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    /**
     * Kịch bản kiểm thử Transaction Rollback (Chứng minh tính nguyên tử Atomicity):
     * Sinh viên SV004 thực hiện giao dịch đăng ký đồng thời 2 môn học:
     * - Môn 1: LHP02 (CNPM) -> Hợp lệ, đã nạp vào persistence context.
     * - Môn 2: LHP01 (CSDL NC) -> Bị lỗi (Trùng giờ học hoặc chưa thỏa điều kiện tiên quyết).
     * -> Kích hoạt ngoại lệ, Transaction bị ROLLBACK toàn bộ.
     * -> Kiểm chứng: Môn 1 cũng bị hủy bỏ, số lượng đăng ký của hệ thống không bị thay đổi.
     */
    public static void testTransactionRollback(EntityManagerFactory emf) {
        System.out.println("\n----------------------------------------------------------------------");
        System.out.println(">>> 2. KIỂM THỬ TRANSACTION ROLLBACK (CHỨNG MINH TÍNH NGUYÊN TỬ ATOMICITY)");
        System.out.println("----------------------------------------------------------------------");

        EntityManager em = emf.createEntityManager();
        long soDangKyBanDau = 0;

        try {
            // Đếm số lượng đăng ký trước giao dịch
            TypedQuery<Long> countQ = em.createQuery("SELECT COUNT(d) FROM DangKyMon d", Long.class);
            soDangKyBanDau = countQ.getSingleResult();
            System.out.println("[-] Tổng số bản ghi DangKyMon TRƯỚC giao dịch: " + soDangKyBanDau);

            // Bắt đầu Transaction giao dịch đăng ký học phần
            em.getTransaction().begin();
            System.out.println("[-] Bắt đầu Transaction đăng ký học phần cho sinh viên SV004...");

            TypedQuery<SinhVien> svQuery = em.createQuery("SELECT s FROM SinhVien s WHERE s.maSV = 'SV004'", SinhVien.class);
            SinhVien sv4 = svQuery.getSingleResult();

            TypedQuery<LopHocPhan> lhpQuery1 = em.createQuery("SELECT l FROM LopHocPhan l WHERE l.maLHP = 'LHP02'", LopHocPhan.class);
            LopHocPhan lhpHopLe = lhpQuery1.getSingleResult();

            TypedQuery<LopHocPhan> lhpQuery2 = em.createQuery("SELECT l FROM LopHocPhan l WHERE l.maLHP = 'LHP01'", LopHocPhan.class);
            LopHocPhan lhpLoi = lhpQuery2.getSingleResult();

            // Bước 1: Đăng ký môn thứ nhất (Hợp lệ)
            DangKyMon dkHopLe = new DangKyMon(sv4, lhpHopLe, "2026-09-10", 0.0, 0.0, 0.0);
            sv4.themDangKy(dkHopLe);
            lhpHopLe.themDangKy(dkHopLe);
            em.persist(dkHopLe);
            System.out.println("    [+] Bước 1: Đăng ký LHP02 (" + lhpHopLe.getHocPhan().getTenHP() + ") -> Thành công tạm thời trong bộ nhớ");

            // Bước 2: Đăng ký môn thứ hai (Kiểm tra nghiệp vụ tiên quyết)
            System.out.println("    [+] Bước 2: Đăng ký LHP01 (" + lhpLoi.getHocPhan().getTenHP() + ") -> Đang kiểm tra điều kiện tiên quyết...");
            if (!sv4.daDatMonTienQuyet(lhpLoi.getHocPhan())) {
                System.out.println("    [!] CẢNH BÁO: Sinh viên SV004 chưa đạt môn tiên quyết " 
                        + lhpLoi.getHocPhan().getHocPhanTienQuyet().getTenHP() + "!");
                throw new IllegalStateException("GIAO DỊCH THẤT BẠI: Sinh viên không đủ điều kiện đăng ký môn học này!");
            }

            // Nếu không lỗi thì mới đến commit
            em.getTransaction().commit();

        } catch (Exception ex) {
            System.out.println("    [x] BẮT ĐƯỢC NGOẠI LỆ: " + ex.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                System.out.println("    >>> TRANSACTION ĐÃ ROLLBACK THÀNH CÔNG VỀ TRẠNG THÁI GỐC!");
            }
        } finally {
            em.close();
        }

        // Kiểm tra lại dữ liệu trong database để chứng minh tính nguyên tử
        EntityManager emKiemTra = emf.createEntityManager();
        try {
            TypedQuery<Long> countQ = emKiemTra.createQuery("SELECT COUNT(d) FROM DangKyMon d", Long.class);
            long soDangKySauRollback = countQ.getSingleResult();
            System.out.println("[-] Tổng số bản ghi DangKyMon SAU KHI ROLLBACK: " + soDangKySauRollback);

            if (soDangKyBanDau == soDangKySauRollback) {
                System.out.println(">>> CHỨNG MINH THÀNH CÔNG: Dữ liệu môn 1 (LHP02) đã bị hủy hoàn toàn, "
                        + "tính nguyên tử (Atomicity) được đảm bảo 100%!");
            } else {
                System.err.println(">>> LỖI: Dữ liệu bị ghi rác vào CSDL!");
            }
        } finally {
            emKiemTra.close();
        }
    }
}
