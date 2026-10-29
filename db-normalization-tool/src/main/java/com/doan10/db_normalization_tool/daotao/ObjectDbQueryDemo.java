package com.doan10.db_normalization_tool.daotao;

import com.doan10.db_normalization_tool.entity.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Class chuyên trách thực thi Bộ 5 Truy Vấn OQL / JPQL Chuyên Sâu (Task 3)
 * Minh họa 5 đặc trưng ưu việt của CSDL Hướng đối tượng:
 * 1. Truy vấn Đa hình (Polymorphism Query) trên lớp cha Nguoi
 * 2. Điều hướng đường dẫn (Path Navigation không dùng JOIN) qua 3 cấp đối tượng
 * 3. Thao tác trên thuộc tính tập hợp (Collection: SIZE)
 * 4. Truy vấn lọc nâng cao theo công thức điểm nghiệp vụ đào tạo
 * 5. Truy vấn mối quan hệ tự liên kết phân cấp (Môn học tiên quyết)
 */
public class ObjectDbQueryDemo {

    public static final String DB_PATH = "db/daotao.odb";

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(DB_PATH);
        try {
            runAllQueries(emf);
        } finally {
            emf.close();
        }
    }

    public static void runAllQueries(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            System.out.println("======================================================================");
            System.out.println("  BỘ TRUY VẤN OQL / JPQL CHUYÊN SÂU TRÊN CSDL OBJECTDB (TASK 3)");
            System.out.println("  Đồ án: Đề tài 10 - Quản lý đào tạo & Chuẩn hóa CSDL | Nhánh: MaGiaVy");
            System.out.println("======================================================================\n");

            query1_Polymorphism(em);
            query2_PathNavigation(em);
            query3_CollectionOperation(em);
            query4_AdvancedBusinessFilter(em);
            query5_PrerequisiteHierarchy(em);

            System.out.println("======================================================================");
            System.out.println("  HOÀN THÀNH THỰC THI 100% CÁC CÂU TRUY VẤN OQL / JPQL CỦA TASK 3!");
            System.out.println("======================================================================");

        } finally {
            em.close();
        }
    }

    /**
     * CÂU 1: TRUY VẤN ĐA HÌNH (POLYMORPHISM)
     * Quét trực tiếp trên lớp cha Nguoi, lấy ra cả Giảng viên và Sinh viên
     */
    public static void query1_Polymorphism(EntityManager em) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println(">>> CÂU 1: TRUY VẤN ĐA HÌNH (POLYMORPHISM QUERY)");
        System.out.println("----------------------------------------------------------------------");
        System.out.println("[*] Ý nghĩa: Truy vấn lớp cha Nguoi để lấy tất cả người có giới tính là 'Nữ'.");
        System.out.println("[*] OQL lý thuyết:  SELECT p FROM Nguoi p WHERE p.gioiTinh = 'Nữ'");
        System.out.println("[*] JPQL ObjectDB: SELECT p FROM Nguoi p WHERE p.gioiTinh = 'Nữ'");
        System.out.println("[-] KẾT QUẢ THỰC THI:");

        TypedQuery<Nguoi> query = em.createQuery(
                "SELECT p FROM Nguoi p WHERE p.gioiTinh = 'Nữ'", Nguoi.class);
        List<Nguoi> list = query.getResultList();

        for (Nguoi p : list) {
            String vaiTro = (p instanceof GiangVien) ? "GIẢNG VIÊN" : 
                            (p instanceof SinhVien) ? "SINH VIÊN" : "NGƯỜI DÙNG";
            String maDinhDanh = (p instanceof GiangVien) ? ((GiangVien) p).getMaGV() :
                                (p instanceof SinhVien) ? ((SinhVien) p).getMaSV() : "N/A";
            System.out.printf("   + [%-10s] Mã: %-6s | Họ tên: %-18s | Ngày sinh: %-10s | Giới tính: %s\n",
                    vaiTro, maDinhDanh, p.getHoTen(), p.getNgaySinh(), p.getGioiTinh());
        }
        System.out.println("   => Ưu việt OODBMS: Quét 1 câu duy nhất lấy được cả 2 lớp con kế thừa mà không cần UNION/JOIN.\n");
    }

    /**
     * CÂU 2: ĐIỀU HƯỚNG ĐƯỜNG DẪN (PATH NAVIGATION - KHÔNG DÙNG JOIN)
     * Truy vết qua 3 cấp đối tượng: DangKyMon -> LopHocPhan -> PhongHoc
     */
    public static void query2_PathNavigation(EntityManager em) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println(">>> CÂU 2: ĐIỀU HƯỚNG ĐƯỜNG DẪN (PATH NAVIGATION - KHÔNG DÙNG JOIN)");
        System.out.println("----------------------------------------------------------------------");
        System.out.println("[*] Ý nghĩa: Tìm danh sách sinh viên và môn học được bố trí học tại các phòng thuộc 'Tòa A'.");
        System.out.println("[*] OQL lý thuyết:  SELECT d.sinhVien.hoTen, d.lopHocPhan.hocPhan.tenHP, d.lopHocPhan.phongHoc.maPhong, d.lopHocPhan.phongHoc.toaNha FROM DangKyMon d WHERE d.lopHocPhan.phongHoc.toaNha = 'Tòa A'");
        System.out.println("[*] JPQL ObjectDB: SELECT d.sinhVien.hoTen, d.lopHocPhan.hocPhan.tenHP, d.lopHocPhan.phongHoc.maPhong, d.lopHocPhan.phongHoc.toaNha FROM DangKyMon d WHERE d.lopHocPhan.phongHoc.toaNha = 'Tòa A'");
        System.out.println("[-] KẾT QUẢ THỰC THI:");

        TypedQuery<Object[]> query = em.createQuery(
                "SELECT d.sinhVien.hoTen, d.lopHocPhan.hocPhan.tenHP, d.lopHocPhan.phongHoc.maPhong, d.lopHocPhan.phongHoc.toaNha " +
                "FROM DangKyMon d " +
                "WHERE d.lopHocPhan.phongHoc.toaNha = 'Tòa A'", Object[].class);
        List<Object[]> list = query.getResultList();

        for (Object[] row : list) {
            System.out.printf("   + Sinh viên: %-18s | Môn: %-25s | Phòng: %-5s (%s)\n",
                    row[0], row[1], row[2], row[3]);
        }
        System.out.println("   => Ưu việt OODBMS: Dùng toán tử dấu chấm (.) duyệt cây đối tượng, loại bỏ hoàn toàn các phép JOIN.\n");
    }

    /**
     * CÂU 3: THAO TÁC TRÊN THUỘC TÍNH TẬP HỢP (COLLECTION: SIZE)
     * Thao tác trực tiếp trên List<DangKyMon> dsDangKy lồng trong LopHocPhan
     */
    public static void query3_CollectionOperation(EntityManager em) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println(">>> CÂU 3: THAO TÁC TRÊN THUỘC TÍNH TẬP HỢP (COLLECTION OPERATION)");
        System.out.println("----------------------------------------------------------------------");
        System.out.println("[*] Ý nghĩa: Tìm các lớp học phần có sĩ số đăng ký đông (từ 3 sinh viên trở lên).");
        System.out.println("[*] OQL lý thuyết:  SELECT l FROM LopHocPhan l WHERE count(l.dsDangKy) >= 3");
        System.out.println("[*] JPQL ObjectDB: SELECT l FROM LopHocPhan l WHERE SIZE(l.dsDangKy) >= 3");
        System.out.println("[-] KẾT QUẢ THỰC THI:");

        TypedQuery<LopHocPhan> query = em.createQuery(
                "SELECT l FROM LopHocPhan l WHERE SIZE(l.dsDangKy) >= 3", LopHocPhan.class);
        List<LopHocPhan> list = query.getResultList();

        for (LopHocPhan l : list) {
            System.out.printf("   + Lớp: %-6s | Môn học: %-25s | Sĩ số đăng ký: %d sinh viên\n",
                    l.getMaLHP(), l.getHocPhan().getTenHP(), l.getSiSoHienTai());
        }
        System.out.println("   => Ưu việt OODBMS: Hỗ trợ thuộc tính tập hợp (List lồng đối tượng), dùng hàm SIZE() trong WHERE không cần GROUP BY.\n");
    }

    /**
     * CÂU 4: TRUY VẤN LỌC NÂNG CAO THEO ĐIỀU KIỆN NGHIỆP VỤ ĐÀO TẠO
     * Tìm sinh viên học môn CSDL Nâng cao đạt kết quả loại Giỏi trở lên (Điểm tổng kết >= 8.0)
     */
    public static void query4_AdvancedBusinessFilter(EntityManager em) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println(">>> CÂU 4: TRUY VẤN LỌC NÂNG CAO THEO ĐIỀU KIỆN NGHIỆP VỤ");
        System.out.println("----------------------------------------------------------------------");
        System.out.println("[*] Ý nghĩa: Tìm sinh viên học môn 'Cơ Sở Dữ Liệu Nâng Cao' (CSDL201) đạt điểm tổng kết Giỏi/Xuất sắc (>= 8.0).");
        System.out.println("[*] OQL lý thuyết:  SELECT d FROM DangKyMon d WHERE d.lopHocPhan.hocPhan.maHP = 'CSDL201' AND (d.diemChuyenCan*0.1 + d.diemGiuaKy*0.3 + d.diemCuoiKy*0.6) >= 8.0");
        System.out.println("[*] JPQL ObjectDB: SELECT d FROM DangKyMon d WHERE d.lopHocPhan.hocPhan.maHP = 'CSDL201' AND (d.diemChuyenCan*0.1 + d.diemGiuaKy*0.3 + d.diemCuoiKy*0.6) >= 8.0");
        System.out.println("[-] KẾT QUẢ THỰC THI:");

        TypedQuery<DangKyMon> query = em.createQuery(
                "SELECT d FROM DangKyMon d " +
                "WHERE d.lopHocPhan.hocPhan.maHP = 'CSDL201' " +
                "  AND (d.diemChuyenCan * 0.1 + d.diemGiuaKy * 0.3 + d.diemCuoiKy * 0.6) >= 8.0", DangKyMon.class);
        List<DangKyMon> list = query.getResultList();

        for (DangKyMon d : list) {
            double diemTK = d.tinhDiemTongKet();
            System.out.printf("   + Mã SV: %-6s | Họ tên: %-18s | Điểm tổng kết: %.2f (%s)\n",
                    d.getSinhVien().getMaSV(), d.getSinhVien().getHoTen(), diemTK, d.getDiemChu());
        }
        System.out.println("   => Ưu việt OODBMS: Tính toán biểu thức số học phức tạp kết hợp điều hướng đối tượng mượt mà.\n");
    }

    /**
     * CÂU 5: TRUY VẤN MỐI QUAN HỆ TỰ LIÊN KẾT (MÔN HỌC TIÊN QUYẾT)
     * Tìm tất cả các môn học có yêu cầu môn học tiên quyết
     */
    public static void query5_PrerequisiteHierarchy(EntityManager em) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println(">>> CÂU 5: TRUY VẤN CẤU TRÚC PHÂN CẤP & ĐIỀU KIỆN TIÊN QUYẾT");
        System.out.println("----------------------------------------------------------------------");
        System.out.println("[*] Ý nghĩa: Liệt kê các môn học bắt buộc phải có môn học tiên quyết và tên môn tiên quyết.");
        System.out.println("[*] OQL lý thuyết:  SELECT h FROM HocPhan h WHERE h.hocPhanTienQuyet IS NOT NULL");
        System.out.println("[*] JPQL ObjectDB: SELECT h FROM HocPhan h WHERE h.hocPhanTienQuyet IS NOT NULL");
        System.out.println("[-] KẾT QUẢ THỰC THI:");

        TypedQuery<HocPhan> query = em.createQuery(
                "SELECT h FROM HocPhan h WHERE h.hocPhanTienQuyet IS NOT NULL", HocPhan.class);
        List<HocPhan> list = query.getResultList();

        for (HocPhan h : list) {
            System.out.printf("   + Môn học: %-8s - %-25s | Yêu cầu tiên quyết: %-8s - %s\n",
                    h.getMaHP(), h.getTenHP(), h.getHocPhanTienQuyet().getMaHP(), h.getHocPhanTienQuyet().getTenHP());
        }
        System.out.println("   => Ưu việt OODBMS: Quan hệ tự liên kết (Self-reference) dạng con trỏ đối tượng, không cần Self-Join.\n");
    }
}
