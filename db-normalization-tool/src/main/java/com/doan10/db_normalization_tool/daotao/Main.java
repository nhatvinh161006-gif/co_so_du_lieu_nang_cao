package com.doan10.db_normalization_tool.daotao;

import com.doan10.db_normalization_tool.entity.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Chương trình thực thi kết nối và nghiệm thu CSDL Hướng đối tượng (ObjectDB):
 * 1. Khởi tạo kết nối tới file CSDL db/daotao.odb
 * 2. Lưu trữ các đối tượng trích xuất từ sơ đồ EER & Công việc.docx (Nguoi, SinhVien, GiangVien, HocPhan, LopHocPhan, PhongHoc, DangKyMon)
 * 3. Kiểm thử các phương thức xử lý nghiệp vụ đóng gói trong Entity
 * 4. Truy vấn JPQL nghiệm thu dữ liệu
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("  KHỞI TẠO VÀ THỰC THI CSDL HƯỚNG ĐỐI TƯỢNG (OBJECTDB) - ĐỀ TÀI 10");
        System.out.println("======================================================================");

        // 1. Kết nối tới file ObjectDB
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("db/daotao.odb");
        EntityManager em = emf.createEntityManager();

        try {
            // 2. Bắt đầu Transaction
            em.getTransaction().begin();

            // 3. Khởi tạo dữ liệu mẫu trích xuất từ sơ đồ EER và Công việc.docx
            PhongHoc ph1 = new PhongHoc("A101", "Tòa A", 60, "Lý thuyết");
            PhongHoc ph2 = new PhongHoc("LAB01", "Tòa B", 40, "Thực hành");
            em.persist(ph1);
            em.persist(ph2);

            HocPhan hp1 = new HocPhan("CSDL201", "Cơ Sở Dữ Liệu Nâng Cao", 3);
            HocPhan hp2 = new HocPhan("CNPM101", "Công Nghệ Phần Mềm", 4);
            HocPhan hp3 = new HocPhan("TRR102", "Toán Rời Rạc", 3);
            em.persist(hp1);
            em.persist(hp2);
            em.persist(hp3);

            GiangVien gv1 = new GiangVien("TS. Tran Van B", "1980-05-12", "Nam", "GV001", "Tiến sĩ");
            GiangVien gv2 = new GiangVien("ThS. Le Thi C", "1985-09-20", "Nữ", "GV002", "Thạc sĩ");

            LopHocPhan lhp1 = new LopHocPhan("LHP01", "HK1-2026", 2, 1, hp1, gv1, ph1);
            LopHocPhan lhp2 = new LopHocPhan("LHP02", "HK1-2026", 4, 7, hp2, gv2, ph2);
            gv1.phanCongLopDay(lhp1);
            gv2.phanCongLopDay(lhp2);
            hp1.themLopHocPhan(lhp1);
            hp2.themLopHocPhan(lhp2);

            em.persist(gv1);
            em.persist(gv2);
            em.persist(lhp1);
            em.persist(lhp2);

            SinhVien sv1 = new SinhVien("Nguyen Van A", "2004-11-15", "Nam", "SV001");
            // CC: 9.0, GK: 8.0, CK: 8.5 -> DTK = 0.9 + 2.4 + 5.1 = 8.4
            DangKyMon dk1 = new DangKyMon(sv1, lhp1, "2026-09-01", 9.0, 8.0, 8.5);
            // CC: 10.0, GK: 9.0, CK: 9.0 -> DTK = 1.0 + 2.7 + 5.4 = 9.1
            DangKyMon dk2 = new DangKyMon(sv1, lhp2, "2026-09-02", 10.0, 9.0, 9.0);
            sv1.themDangKy(dk1);
            sv1.themDangKy(dk2);
            lhp1.themDangKy(dk1);
            lhp2.themDangKy(dk2);

            SinhVien sv2 = new SinhVien("Tran Thi Hoa", "2004-03-22", "Nữ", "SV002");
            // CC: 8.0, GK: 7.0, CK: 7.0 -> DTK = 0.8 + 2.1 + 4.2 = 7.1
            DangKyMon dk3 = new DangKyMon(sv2, lhp1, "2026-09-03", 8.0, 7.0, 7.0);
            sv2.themDangKy(dk3);
            lhp1.themDangKy(dk3);

            em.persist(sv1);
            em.persist(sv2);

            // 4. Commit lưu dữ liệu vào file db/daotao.odb
            em.getTransaction().commit();
            System.out.println(">>> ĐÃ LƯU THÀNH CÔNG VÀO FILE: db/daotao.odb\n");

            // 5. Kiểm thử các PHƯƠNG THỨC XỬ LÝ NGHIỆP VỤ (OODBMS Methods)
            System.out.println("----------------------------------------------------------------------");
            System.out.println(">>> KIỂM THỬ PHƯƠNG THỨC XỬ LÝ NGHIỆP VỤ TRONG ENTITY:");
            System.out.println("----------------------------------------------------------------------");

            // Test phương thức kiểm tra trùng lịch
            LopHocPhan lhpTrung = new LopHocPhan("LHP03", "HK1-2026", 2, 1, hp3, gv2, ph1); // Trùng Thứ 2, Tiết 1
            LopHocPhan lhpKhongTrung = new LopHocPhan("LHP04", "HK1-2026", 5, 4, hp3, gv2, ph2);

            System.out.println("1. Sinh viên SV001 đăng ký thử môn mới:");
            System.out.println("   + Lớp LHP03 (" + lhpTrung.getThongTinLichHoc() + ") -> Trùng lịch: "
                    + sv1.kiemTraTrungLich(lhpTrung) + " (Đã chặn trùng lịch)");
            System.out.println("   + Lớp LHP04 (" + lhpKhongTrung.getThongTinLichHoc() + ") -> Trùng lịch: "
                    + sv1.kiemTraTrungLich(lhpKhongTrung) + " (Hợp lệ để đăng ký)");

            // Test phương thức tính toán học lực và điểm 3 thành phần
            System.out.println("\n2. Tính toán kết quả học tập của Sinh viên SV001:");
            System.out.println("   + Điểm môn CSDL (CC 9.0, GK 8.0, CK 8.5) -> Tổng kết: " + dk1.tinhDiemTongKet() + " (" + dk1.getDiemChu() + ")");
            System.out.println("   + Điểm môn CNPM (CC 10.0, GK 9.0, CK 9.0) -> Tổng kết: " + dk2.tinhDiemTongKet() + " (" + dk2.getDiemChu() + ")");
            System.out.println("   + Tổng tín chỉ tích lũy: " + sv1.tongTinChiTichLuy() + " TC");
            System.out.println("   + Điểm trung bình tích lũy: " + sv1.tinhDiemTrungBinh() + " / 10");
            System.out.println("   + Xếp loại học lực: " + sv1.xepLoaiHocLuc());

            // 6. Truy vấn dữ liệu thực tế bằng JPQL
            System.out.println("\n----------------------------------------------------------------------");
            System.out.println(">>> TRUY VẤN DỮ LIỆU ĐỐI TƯỢNG BẰNG JPQL (OQL MAPPING):");
            System.out.println("----------------------------------------------------------------------");

            TypedQuery<SinhVien> querySV = em.createQuery("SELECT s FROM SinhVien s", SinhVien.class);
            List<SinhVien> listSV = querySV.getResultList();
            for (SinhVien sv : listSV) {
                System.out.println("\n[Sinh Viên] Mã SV: " + sv.getMaSV() + " | Họ Tên: " + sv.getHoTen()
                        + " | Giới Tính: " + sv.getGioiTinh() + " | Ngày Sinh: " + sv.getNgaySinh());
                System.out.println("   -> Danh sách môn học đã đăng ký (" + sv.getDsDangKy().size() + " môn):");
                for (DangKyMon dk : sv.getDsDangKy()) {
                    LopHocPhan lhp = dk.getLopHocPhan();
                    System.out.println("      * " + lhp.getMaLHP() + " - " + lhp.getHocPhan().getTenHP()
                            + " (" + lhp.getHocPhan().getSoTC() + " TC) | " + lhp.getThongTinLichHoc()
                            + " | Điểm TK: " + dk.tinhDiemTongKet() + " (" + dk.getDiemChu() + ")"
                            + " | Trạng thái: " + (dk.laMonDat() ? "ĐẠT" : "RỚT"));
                }
            }

            // Truy vấn Lớp học phần
            System.out.println("\n>>> DANH SÁCH LỚP HỌC PHẦN & GIẢNG VIÊN & PHÒNG HỌC:");
            TypedQuery<LopHocPhan> queryLHP = em.createQuery("SELECT l FROM LopHocPhan l", LopHocPhan.class);
            for (LopHocPhan lhp : queryLHP.getResultList()) {
                System.out.println("   + Lớp: " + lhp.getMaLHP() + " | Môn: " + lhp.getHocPhan().getTenHP()
                        + " | GV: " + lhp.getGiangVien().getHoTen() + " (" + lhp.getGiangVien().getHocVi() + ")"
                        + " | Phòng: " + lhp.getPhongHoc().getMaPhong() + " (" + lhp.getPhongHoc().getToaNha() + ", " + lhp.getPhongHoc().getLoaiPhong() + ")"
                        + " | Sĩ số: " + lhp.getSiSoHienTai());
            }

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                System.err.println(">>> ĐÃ ROLLBACK TRANSACTION DO GẶP LỖI!");
            }
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }

        System.out.println("\n======================================================================");
        System.out.println("  HOÀN TẤT THỰC THI NGHIỆM THU OBJECTDB!");
        System.out.println("======================================================================");
    }
}
