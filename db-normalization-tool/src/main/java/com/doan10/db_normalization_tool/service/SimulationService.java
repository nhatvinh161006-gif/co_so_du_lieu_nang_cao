package com.doan10.db_normalization_tool.service;

import com.doan10.db_normalization_tool.daotao.DatabaseInitializer;
import com.doan10.db_normalization_tool.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.*;

@Service
public class SimulationService {

    private final LichSuChuanHoaService lichSuChuanHoaService;

    @Autowired
    public SimulationService(LichSuChuanHoaService lichSuChuanHoaService) {
        this.lichSuChuanHoaService = lichSuChuanHoaService;
    }

    private EntityManagerFactory getEmf() {
        return lichSuChuanHoaService.getEntityManagerFactory();
    }

    public synchronized void ensureInitialized() {
        EntityManager em = getEmf().createEntityManager();
        try {
            long count = em.createQuery("SELECT COUNT(s) FROM SinhVien s", Long.class).getSingleResult();
            if (count == 0) {
                DatabaseInitializer.initSampleData(getEmf());
            }
        } catch (Exception e) {
            DatabaseInitializer.initSampleData(getEmf());
        } finally {
            em.close();
        }
    }

    public synchronized void resetDatabase() {
        DatabaseInitializer.initSampleData(getEmf());
    }

    public List<Map<String, Object>> getStudents() {
        ensureInitialized();
        EntityManager em = getEmf().createEntityManager();
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            TypedQuery<SinhVien> query = em.createQuery("SELECT s FROM SinhVien s ORDER BY s.maSV", SinhVien.class);
            List<SinhVien> students = query.getResultList();
            for (SinhVien sv : students) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("maSV", sv.getMaSV());
                map.put("hoTen", sv.getHoTen());
                map.put("ngaySinh", sv.getNgaySinh());
                map.put("gioiTinh", sv.getGioiTinh());
                map.put("diemTB", Math.round(sv.tinhDiemTrungBinh() * 100.0) / 100.0);
                map.put("tinChiTichLuy", sv.tongTinChiTichLuy());
                map.put("xepLoai", sv.xepLoaiHocLuc());

                List<Map<String, Object>> dsDK = new ArrayList<>();
                if (sv.getDsDangKy() != null) {
                    for (DangKyMon dk : sv.getDsDangKy()) {
                        Map<String, Object> dkMap = new LinkedHashMap<>();
                        dkMap.put("maLHP", dk.getLopHocPhan() != null ? dk.getLopHocPhan().getMaLHP() : "N/A");
                        dkMap.put("tenHP", (dk.getLopHocPhan() != null && dk.getLopHocPhan().getHocPhan() != null)
                                ? dk.getLopHocPhan().getHocPhan().getTenHP() : "N/A");
                        dkMap.put("maHP", (dk.getLopHocPhan() != null && dk.getLopHocPhan().getHocPhan() != null)
                                ? dk.getLopHocPhan().getHocPhan().getMaHP() : "N/A");
                        dkMap.put("diemTongKet", Math.round(dk.tinhDiemTongKet() * 100.0) / 100.0);
                        dkMap.put("laMonDat", dk.laMonDat());
                        dsDK.add(dkMap);
                    }
                }
                map.put("dsDangKy", dsDK);
                result.add(map);
            }
        } finally {
            em.close();
        }
        return result;
    }

    public List<Map<String, Object>> getCourses() {
        ensureInitialized();
        EntityManager em = getEmf().createEntityManager();
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            TypedQuery<LopHocPhan> query = em.createQuery("SELECT l FROM LopHocPhan l ORDER BY l.maLHP", LopHocPhan.class);
            List<LopHocPhan> classes = query.getResultList();
            for (LopHocPhan lhp : classes) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("maLHP", lhp.getMaLHP());
                map.put("hocKy", lhp.getHocKy());
                map.put("thu", lhp.getThu());
                map.put("tietBD", lhp.getTietBD());
                map.put("siSoHienTai", lhp.getSiSoHienTai());

                if (lhp.getHocPhan() != null) {
                    map.put("maHP", lhp.getHocPhan().getMaHP());
                    map.put("tenHP", lhp.getHocPhan().getTenHP());
                    map.put("soTC", lhp.getHocPhan().getSoTC());
                    if (lhp.getHocPhan().coMonTienQuyet()) {
                        map.put("maHPTienQuyet", lhp.getHocPhan().getHocPhanTienQuyet().getMaHP());
                        map.put("tenHPTienQuyet", lhp.getHocPhan().getHocPhanTienQuyet().getTenHP());
                    } else {
                        map.put("maHPTienQuyet", null);
                        map.put("tenHPTienQuyet", null);
                    }
                }

                if (lhp.getGiangVien() != null) {
                    map.put("maGV", lhp.getGiangVien().getMaGV());
                    map.put("tenGV", lhp.getGiangVien().getHoTen());
                }

                if (lhp.getPhongHoc() != null) {
                    map.put("maPhong", lhp.getPhongHoc().getMaPhong());
                    map.put("toaNha", lhp.getPhongHoc().getToaNha());
                    map.put("sucChua", lhp.getPhongHoc().getSucChua());
                }

                map.put("thongTinLichHoc", lhp.getThongTinLichHoc());
                result.add(map);
            }
        } finally {
            em.close();
        }
        return result;
    }

    public Map<String, Object> executeRegistration(String maSV, String maLHP) {
        ensureInitialized();
        EntityManager em = getEmf().createEntityManager();
        Map<String, Object> response = new LinkedHashMap<>();
        List<String> steps = new ArrayList<>();

        long countBefore = 0;
        try {
            countBefore = em.createQuery("SELECT COUNT(d) FROM DangKyMon d", Long.class).getSingleResult();
        } catch (Exception ignored) {}

        response.put("countBefore", countBefore);
        response.put("maSV", maSV);
        response.put("maLHP", maLHP);

        try {
            steps.add("1. [BEGIN TRANSACTION] Mở kết nối EntityManager và bắt đầu giao dịch với ObjectDB.");
            em.getTransaction().begin();

            TypedQuery<SinhVien> svQuery = em.createQuery("SELECT s FROM SinhVien s WHERE s.maSV = :maSV", SinhVien.class);
            svQuery.setParameter("maSV", maSV);
            List<SinhVien> svList = svQuery.getResultList();
            if (svList.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy sinh viên có mã " + maSV);
            }
            SinhVien sv = svList.get(0);
            response.put("tenSV", sv.getHoTen());

            TypedQuery<LopHocPhan> lhpQuery = em.createQuery("SELECT l FROM LopHocPhan l WHERE l.maLHP = :maLHP", LopHocPhan.class);
            lhpQuery.setParameter("maLHP", maLHP);
            List<LopHocPhan> lhpList = lhpQuery.getResultList();
            if (lhpList.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy lớp học phần có mã " + maLHP);
            }
            LopHocPhan lhp = lhpList.get(0);
            response.put("tenHP", lhp.getHocPhan() != null ? lhp.getHocPhan().getTenHP() : "N/A");

            steps.add(String.format("2. [LOAD ENTITIES] Nạp đối tượng SinhVien '%s' (%s) và LopHocPhan '%s' (%s).",
                    sv.getMaSV(), sv.getHoTen(), lhp.getMaLHP(), lhp.getHocPhan().getTenHP()));

            // 1. Kiểm tra đã đăng ký lớp này chưa
            boolean daDangKy = false;
            if (sv.getDsDangKy() != null) {
                for (DangKyMon dk : sv.getDsDangKy()) {
                    if (dk.getLopHocPhan() != null && dk.getLopHocPhan().getMaLHP().equalsIgnoreCase(maLHP)) {
                        daDangKy = true;
                        break;
                    }
                }
            }
            if (daDangKy) {
                steps.add("3. [CHECK DUPLICATE] Sinh viên đã đăng ký lớp học phần này trước đó.");
                throw new IllegalStateException("Sinh viên " + sv.getHoTen() + " (" + maSV + ") đã đăng ký lớp " + maLHP + " rồi!");
            }

            // 2. Kiểm tra trùng lịch học
            steps.add(String.format("3. [CHECK SCHEDULE] Kiểm tra thời khóa biểu: Thứ %d, Tiết %d...", lhp.getThu(), lhp.getTietBD()));
            if (sv.kiemTraTrungLich(lhp)) {
                steps.add("   -> [XUNG ĐỘT] Trùng lịch học với một môn khác sinh viên đã đăng ký!");
                throw new IllegalStateException("Trùng thời khóa biểu! Sinh viên đã có lớp học khác vào Thứ " + lhp.getThu() + ", Tiết " + lhp.getTietBD() + ".");
            }
            steps.add("   -> [HỢP LỆ] Lịch học không bị trùng.");

            // 3. Kiểm tra môn tiên quyết (Constraint chính)
            HocPhan hp = lhp.getHocPhan();
            steps.add("4. [CHECK PREREQUISITE] Kiểm tra điều kiện tiên quyết cho môn: " + hp.getTenHP() + "...");
            if (hp.coMonTienQuyet()) {
                HocPhan tq = hp.getHocPhanTienQuyet();
                steps.add(String.format("   -> Môn học yêu cầu hoàn thành tiên quyết: %s (%s, điểm tổng kết >= 4.0)", tq.getTenHP(), tq.getMaHP()));
                if (!sv.daDatMonTienQuyet(hp)) {
                    steps.add(String.format("   -> [VI PHẠM] Sinh viên %s chưa đạt môn tiên quyết '%s'!", sv.getHoTen(), tq.getTenHP()));
                    throw new IllegalStateException("RÀNG BUỘC TOÀN VẸN BỊ VI PHẠM: Sinh viên " + sv.getHoTen()
                            + " chưa hoàn thành môn học tiên quyết " + tq.getTenHP() + " (" + tq.getMaHP() + ")!");
                }
                steps.add("   -> [HỢP LỆ] Sinh viên đã đạt môn tiên quyết " + tq.getTenHP() + ".");
            } else {
                steps.add("   -> [HỢP LỆ] Môn học không yêu cầu môn tiên quyết.");
            }

            // 4. Kiểm tra sức chứa phòng học
            steps.add(String.format("5. [CHECK CAPACITY] Kiểm tra sĩ số lớp hiện tại (%d) so với sức chứa phòng (%d)...",
                    lhp.getSiSoHienTai(), lhp.getPhongHoc() != null ? lhp.getPhongHoc().getSucChua() : 100));
            if (lhp.getPhongHoc() != null && lhp.getSiSoHienTai() >= lhp.getPhongHoc().getSucChua()) {
                steps.add("   -> [QUÁ TẢI] Lớp học phần đã đủ hoặc vượt quá sức chứa phòng!");
                throw new IllegalStateException("Lớp học phần đã đạt sĩ số tối đa (" + lhp.getSiSoHienTai() + "/" + lhp.getPhongHoc().getSucChua() + ")!");
            }
            steps.add("   -> [HỢP LỆ] Còn chỗ trống trong phòng học.");

            // Ghi nhận đăng ký
            DangKyMon dk = new DangKyMon(sv, lhp, LocalDate.now().toString(), 0.0, 0.0, 0.0);
            sv.themDangKy(dk);
            lhp.themDangKy(dk);
            em.persist(dk);
            steps.add("6. [PERSIST] Tạo đối tượng DangKyMon liên kết 2 chiều và đưa vào Persistence Context.");

            // Commit transaction
            em.getTransaction().commit();
            steps.add("7. [COMMIT TRANSACTION] Đã ghi nhận thành công mọi thay đổi vào file CSDL daotao.odb!");

            long countAfter = em.createQuery("SELECT COUNT(d) FROM DangKyMon d", Long.class).getSingleResult();
            response.put("status", "COMMIT");
            response.put("success", true);
            response.put("countAfter", countAfter);
            response.put("message", "Giao dịch Đăng ký môn học thành công! Transaction đã COMMIT.");
            response.put("explanation", "Tính nhất quán & bền vững (Consistency & Durability): Tất cả các ràng buộc nghiệp vụ đều thỏa mãn. Dữ liệu đã được ghi nhận an toàn vào ObjectDB.");

        } catch (Exception ex) {
            steps.add("(!) [EXCEPTION RAISED] Ngoại lệ nghiệp vụ xuất hiện: " + ex.getMessage());
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                steps.add("8. [ROLLBACK TRANSACTION] Kích hoạt cơ chế Rollback! Toàn bộ thay đổi tạm thời trong bộ nhớ bị hủy bỏ.");
                steps.add("9. [ATOMICITY PRESERVED] Tính nguyên tử được bảo toàn: Cơ sở dữ liệu trở về đúng trạng thái ban đầu trước giao dịch.");
            }

            long countAfter = 0;
            try {
                countAfter = em.createQuery("SELECT COUNT(d) FROM DangKyMon d", Long.class).getSingleResult();
            } catch (Exception ignored) {}

            response.put("status", "ROLLBACK");
            response.put("success", false);
            response.put("countAfter", countAfter);
            response.put("message", ex.getMessage());
            response.put("explanation", "Tính nguyên tử (Atomicity): Khi gặp lỗi hoặc vi phạm điều kiện tiên quyết/sĩ số, hệ thống tự động hủy bỏ (Rollback) toàn bộ giao dịch, đảm bảo không có bản ghi 'nửa vời' hay dữ liệu rác nào lọt vào CSDL.");
        } finally {
            em.close();
        }

        response.put("steps", steps);
        return response;
    }

    public List<Map<String, Object>> getOqlQueries() {
        List<Map<String, Object>> queries = new ArrayList<>();

        // Câu 1
        Map<String, Object> q1 = new LinkedHashMap<>();
        q1.put("id", 1);
        q1.put("title", "Truy vấn Đa hình (Polymorphism Query)");
        q1.put("description", "Quét trực tiếp trên Extent của lớp cha Nguoi để lấy tất cả người dùng có giới tính 'Nữ'. Tự động trả về thể hiện đa hình của cả GiangVien và SinhVien mà không cần UNION.");
        q1.put("oql", "SELECT p\nFROM Nguoi p\nWHERE p.gioiTinh = 'Nữ'");
        q1.put("jpql", "SELECT p\nFROM Nguoi p\nWHERE p.gioiTinh = 'Nữ'");
        q1.put("advantage", "Trong RDBMS, bắt buộc tạo 2 bảng riêng SinhVien và GiangVien rồi dùng UNION ALL. Trong OODBMS, quét 1 câu duy nhất lấy được cả 2 lớp con kế thừa.");
        q1.put("resultSample", "Tìm thấy 2 đối tượng Nguoi (1 Giảng viên ThS. Le Thi C, 2 Sinh viên Tran Thi Hoa & Vu Thi Mai).");
        queries.add(q1);

        // Câu 2
        Map<String, Object> q2 = new LinkedHashMap<>();
        q2.put("id", 2);
        q2.put("title", "Điều hướng Đường dẫn (Path Navigation)");
        q2.put("description", "Liệt kê sinh viên và môn học được xếp lịch học tại các phòng thuộc 'Tòa A'. Truy vết liên cấp: DangKyMon -> LopHocPhan -> PhongHoc.");
        q2.put("oql", "SELECT d.sinhVien.hoTen,\n       d.lopHocPhan.hocPhan.tenHP,\n       d.lopHocPhan.phongHoc.maPhong\nFROM DangKyMon d\nWHERE d.lopHocPhan.phongHoc.toaNha = 'Tòa A'");
        q2.put("jpql", "SELECT d.sinhVien.hoTen,\n       d.lopHocPhan.hocPhan.tenHP,\n       d.lopHocPhan.phongHoc.maPhong\nFROM DangKyMon d\nWHERE d.lopHocPhan.phongHoc.toaNha = 'Tòa A'");
        q2.put("advantage", "Trong RDBMS phải JOIN qua 3 bảng DangKy, LopHocPhan, PhongHoc tốn chi phí quét bảng. OODBMS duyệt con trỏ tham chiếu trực tiếp bằng toán tử dấu chấm '.', loại bỏ hoàn toàn các lệnh JOIN.");
        q2.put("resultSample", "Các sinh viên học tại Tòa A (Phòng A101, A102): Nguyen Van A, Tran Thi Hoa, Le Hoang Nam, Pham Minh Tuan, Vu Thi Mai.");
        queries.add(q2);

        // Câu 3
        Map<String, Object> q3 = new LinkedHashMap<>();
        q3.put("id", 3);
        q3.put("title", "Thao tác trên Thuộc tính Tập hợp (Collection Operation)");
        q3.put("description", "Tìm các lớp học phần có sĩ số từ 3 sinh viên trở lên dựa trên thuộc tính tập hợp List<DangKyMon> dsDangKy lồng trong LopHocPhan.");
        q3.put("oql", "SELECT l\nFROM LopHocPhan l\nWHERE count(l.dsDangKy) >= 3");
        q3.put("jpql", "SELECT l\nFROM LopHocPhan l\nWHERE SIZE(l.dsDangKy) >= 3");
        q3.put("advantage", "RDBMS phải dùng GROUP BY và HAVING COUNT(*) >= 3 do vi phạm 1NF. OODBMS lưu trực tiếp thuộc tính tập hợp trong đối tượng, dùng hàm SIZE() rất tự nhiên.");
        q3.put("resultSample", "LHP00 (CSDL Cơ Bản: 3 SV), LHP01 (CSDL Nâng Cao: 4 SV), LHP02 (Công Nghệ Phần Mềm: 3 SV).");
        queries.add(q3);

        // Câu 4
        Map<String, Object> q4 = new LinkedHashMap<>();
        q4.put("id", 4);
        q4.put("title", "Truy vấn Lọc Nâng cao Theo Nghiệp vụ");
        q4.put("description", "Tìm sinh viên học môn 'Cơ Sở Dữ Liệu Nâng Cao' (CSDL201) đạt loại Giỏi/Xuất sắc (Điểm tổng kết >= 8.0 tính theo công thức: 10% CC + 30% GK + 60% CK).");
        q4.put("oql", "SELECT d\nFROM DangKyMon d\nWHERE d.lopHocPhan.hocPhan.maHP = 'CSDL201'\n  AND (d.diemChuyenCan * 0.1 + d.diemGiuaKy * 0.3 + d.diemCuoiKy * 0.6) >= 8.0");
        q4.put("jpql", "SELECT d\nFROM DangKyMon d\nWHERE d.lopHocPhan.hocPhan.maHP = 'CSDL201'\n  AND (d.diemChuyenCan * 0.1 + d.diemGiuaKy * 0.3 + d.diemCuoiKy * 0.6) >= 8.0");
        q4.put("advantage", "Cho phép tính toán biểu thức đại số trực tiếp trong mệnh đề WHERE và trả về đối tượng DangKyMon với trọn vẹn các phương thức nghiệp vụ.");
        q4.put("resultSample", "Nguyen Van A (8.4), Le Hoang Nam (9.55), Vu Thi Mai (8.7).");
        queries.add(q4);

        // Câu 5
        Map<String, Object> q5 = new LinkedHashMap<>();
        q5.put("id", 5);
        q5.put("title", "Truy vấn Phân cấp & Điều kiện Tiên quyết (Prerequisite Query)");
        q5.put("description", "Tìm tất cả các môn học có yêu cầu môn học tiên quyết và lấy ra mã môn, tên môn học tiên quyết tương ứng.");
        q5.put("oql", "SELECT h\nFROM HocPhan h\nWHERE h.hocPhanTienQuyet IS NOT NULL");
        q5.put("jpql", "SELECT h\nFROM HocPhan h\nWHERE h.hocPhanTienQuyet IS NOT NULL");
        q5.put("advantage", "RDBMS phải dùng Self-Join nối bảng HocPhan với chính nó. OODBMS biểu diễn tự nhiên dạng con trỏ đối tượng tự liên kết (Self-reference) h.hocPhanTienQuyet.");
        q5.put("resultSample", "Môn: CSDL201 (Cơ Sở Dữ Liệu Nâng Cao) -> Yêu cầu tiên quyết: CSDL101 (Cơ Sở Dữ Liệu Cơ Bản).");
        queries.add(q5);

        // Câu 6
        Map<String, Object> q6 = new LinkedHashMap<>();
        q6.put("id", 6);
        q6.put("title", "Kiểm tra Thuộc tính Tập hợp Rỗng (Collection IS EMPTY)");
        q6.put("description", "Tìm các môn học chưa mở bất kỳ lớp học phần nào trong học kỳ để nhà trường có kế hoạch xếp thời khóa biểu.");
        q6.put("oql", "SELECT h\nFROM HocPhan h\nWHERE is_empty(h.dsLopHocPhan)");
        q6.put("jpql", "SELECT h\nFROM HocPhan h\nWHERE h.dsLopHocPhan IS EMPTY");
        q6.put("advantage", "RDBMS phải dùng LEFT JOIN ... WHERE l.MaLHP IS NULL phức tạp. OODBMS có toán tử IS EMPTY trực quan trên thuộc tính tập hợp.");
        q6.put("resultSample", "AI301 (Trí Tuệ Nhân Tạo) - Chưa mở lớp học phần nào.");
        queries.add(q6);

        // Câu 7
        Map<String, Object> q7 = new LinkedHashMap<>();
        q7.put("id", 7);
        q7.put("title", "Thống kê Kết gộp Phân tích Điểm (Aggregation)");
        q7.put("description", "Thống kê điểm thi kết thúc học phần trung bình (AVG), nhỏ nhất (MIN) và lớn nhất (MAX) theo từng lớp học phần.");
        q7.put("oql", "SELECT d.lopHocPhan.maLHP,\n       d.lopHocPhan.hocPhan.tenHP,\n       avg(d.diemCuoiKy),\n       min(d.diemCuoiKy),\n       max(d.diemCuoiKy)\nFROM DangKyMon d\nGROUP BY d.lopHocPhan.maLHP,\n         d.lopHocPhan.hocPhan.tenHP");
        q7.put("jpql", "SELECT d.lopHocPhan.maLHP,\n       d.lopHocPhan.hocPhan.tenHP,\n       AVG(d.diemCuoiKy),\n       MIN(d.diemCuoiKy),\n       MAX(d.diemCuoiKy)\nFROM DangKyMon d\nGROUP BY d.lopHocPhan.maLHP,\n         d.lopHocPhan.hocPhan.tenHP");
        q7.put("advantage", "Hỗ trợ kết gộp theo nhóm đối tượng liên kết mà không cần lệnh JOIN tường minh.");
        q7.put("resultSample", "LHP00: AVG=8.67, MIN=8.0, MAX=9.5 | LHP01: AVG=8.38, MIN=7.0, MAX=9.5 | LHP02: AVG=8.67, MIN=8.0, MAX=9.0");
        queries.add(q7);

        // Câu 8
        Map<String, Object> q8 = new LinkedHashMap<>();
        q8.put("id", 8);
        q8.put("title", "Truy vấn Phân công Giảng dạy Từ Giảng viên");
        q8.put("description", "Liệt kê thời khóa biểu, phòng học và môn học được phân công của từng Giảng viên qua thuộc tính tập hợp dsLopGiangDay.");
        q8.put("oql", "SELECT g.hoTen, g.hocVi,\n       l.maLHP, l.hocPhan.tenHP,\n       l.phongHoc.maPhong\nFROM GiangVien g, g.dsLopGiangDay l");
        q8.put("jpql", "SELECT g.hoTen, g.hocVi,\n       l.maLHP, l.hocPhan.tenHP,\n       l.phongHoc.maPhong\nFROM GiangVien g JOIN g.dsLopGiangDay l");
        q8.put("advantage", "Duyệt trực tiếp thuộc tính tập hợp lồng trong đối tượng Giảng viên thay vì nối bảng qua khóa ngoại MaGV.");
        q8.put("resultSample", "TS. Tran Van B: LHP00 (CSDL Cơ Bản - A101), LHP01 (CSDL Nâng Cao - A101) | ThS. Le Thi C: LHP02 (CNPM - LAB01)...");
        queries.add(q8);

        // Câu 9
        Map<String, Object> q9 = new LinkedHashMap<>();
        q9.put("id", 9);
        q9.put("title", "Truy vấn Cảnh báo Học vụ (Academic Warning)");
        q9.put("description", "Rà soát các sinh viên có nguy cơ bị cảnh cáo học vụ do rớt môn (Điểm tổng kết < 4.0) hoặc điểm chuyên cần bị điểm liệt (< 5.0).");
        q9.put("oql", "SELECT d\nFROM DangKyMon d\nWHERE (d.diemChuyenCan * 0.1 +\n       d.diemGiuaKy * 0.3 +\n       d.diemCuoiKy * 0.6) < 4.0\n   OR d.diemChuyenCan < 5.0");
        q9.put("jpql", "SELECT d\nFROM DangKyMon d\nWHERE (d.diemChuyenCan * 0.1 +\n       d.diemGiuaKy * 0.3 +\n       d.diemCuoiKy * 0.6) < 4.0\n   OR d.diemChuyenCan < 5.0");
        q9.put("advantage", "Trả về trực tiếp thực thể DangKyMon kèm quan hệ đối tượng SinhVien để gửi thông báo cảnh báo tự động.");
        q9.put("resultSample", "SV004 - Pham Minh Tuan: Môn CSDL Nâng Cao (LHP04), Điểm TK: 3.8 (F - Rớt môn).");
        queries.add(q9);

        // Câu 10
        Map<String, Object> q10 = new LinkedHashMap<>();
        q10.put("id", 10);
        q10.put("title", "Kiểm tra Đối soát Sức chứa Phòng học (Capacity Check)");
        q10.put("description", "Thống kê sĩ số thực tế đối chiếu với sức chứa tối đa của phòng học để tính tỷ lệ lấp đầy chỗ ngồi.");
        q10.put("oql", "SELECT l\nFROM LopHocPhan l\nWHERE count(l.dsDangKy) > 0\nORDER BY l.maLHP");
        q10.put("jpql", "SELECT l\nFROM LopHocPhan l\nWHERE SIZE(l.dsDangKy) > 0\nORDER BY l.maLHP");
        q10.put("advantage", "Lấy đối tượng LopHocPhan và gọi trực tiếp các phương thức nghiệp vụ nội tại getSiSoHienTai() và getPhongHoc().getSucChua() thời gian thực.");
        q10.put("resultSample", "LHP00: 3/60 (5.0%) | LHP01: 4/60 (6.7%) | LHP02: 3/40 (7.5%) | LHP03: 2/45 (4.4%) | LHP04: 1/80 (1.3%).");
        queries.add(q10);

        return queries;
    }
}
