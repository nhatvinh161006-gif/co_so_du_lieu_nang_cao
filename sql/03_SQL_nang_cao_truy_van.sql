-- ============================================================
--  FILE 03: SQL NÂNG CAO - TRUY VẤN (10 truy vấn)
--  Nhóm N10 - Quản lý Đào tạo Đại học
--  Bao gồm: subquery, JOIN, GROUP BY, HAVING, CTE, Window Functions
-- ============================================================
SET search_path = qldt;

-- ============================================================
-- TRUY VẤN 1: JOIN nhiều bảng
-- Danh sách sinh viên, ngành học, khoa và điểm trung bình
-- (Minh hoạ INNER JOIN + LEFT JOIN + GROUP BY)
-- ============================================================
SELECT
    sv.MaSV,
    sv.TenSV,
    n.TenNganh,
    k.TenKhoa,
    COUNT(dk.MaLHP)                 AS so_mon_dang_ky,
    ROUND(AVG(dk.Diem), 2)          AS diem_trung_binh
FROM SinhVien  sv
JOIN Nganh     n  ON sv.MaNganh = n.MaNganh
JOIN Khoa      k  ON n.MaKhoa   = k.MaKhoa
LEFT JOIN DangKy dk ON sv.MaSV  = dk.MaSV AND dk.Diem IS NOT NULL
GROUP BY sv.MaSV, sv.TenSV, n.TenNganh, k.TenKhoa
ORDER BY diem_trung_binh DESC NULLS LAST;

-- ============================================================
-- TRUY VẤN 2: SUBQUERY trong WHERE
-- Sinh viên có điểm THẤP HƠN điểm trung bình chung của toàn trường
-- (Minh hoạ Correlated Subquery)
-- ============================================================
SELECT
    sv.MaSV,
    sv.TenSV,
    ROUND(AVG(dk.Diem), 2)  AS diem_tb_ca_nhan
FROM SinhVien sv
JOIN DangKy dk ON sv.MaSV = dk.MaSV
WHERE dk.Diem IS NOT NULL
GROUP BY sv.MaSV, sv.TenSV
HAVING AVG(dk.Diem) < (
    -- Subquery: tính điểm TB toàn trường
    SELECT AVG(Diem) FROM DangKy WHERE Diem IS NOT NULL
)
ORDER BY diem_tb_ca_nhan;

-- ============================================================
-- TRUY VẤN 3: SUBQUERY trong SELECT (Scalar Subquery)
-- Danh sách học phần kèm số lượng sinh viên đã đăng ký và
-- so sánh với trung bình toàn hệ thống
-- ============================================================
SELECT
    hp.MaHP,
    hp.TenHP,
    hp.SoTC,
    (SELECT COUNT(*) FROM LopHocPhan lhp2
     WHERE lhp2.MaHP = hp.MaHP)                            AS so_lop_mo,
    (SELECT COUNT(*) FROM DangKy dk
     JOIN LopHocPhan lhp ON dk.MaLHP = lhp.MaLHP
     WHERE lhp.MaHP = hp.MaHP)                             AS tong_sv_dang_ky,
    CASE
        WHEN (SELECT COUNT(*) FROM DangKy dk
              JOIN LopHocPhan lhp ON dk.MaLHP = lhp.MaLHP
              WHERE lhp.MaHP = hp.MaHP) >
             (SELECT AVG(cnt) FROM (
                SELECT COUNT(*) AS cnt FROM DangKy dk
                JOIN LopHocPhan lhp ON dk.MaLHP = lhp.MaLHP
                GROUP BY lhp.MaHP
             ) sub)
        THEN 'Phổ biến'
        ELSE 'Bình thường'
    END AS muc_do_pho_bien
FROM HocPhan hp
ORDER BY tong_sv_dang_ky DESC;

-- ============================================================
-- TRUY VẤN 4: KẾT HỢP (UNION)
-- Danh sách tất cả "người dùng hệ thống" (GV + SV) với vai trò
-- (Minh hoạ UNION)
-- ============================================================
SELECT MaGV AS MaID, TenGV AS HoTen, 'Giảng viên' AS VaiTro, HocVi AS ThongTinThem
FROM GiangVien
UNION
SELECT MaSV, TenSV, 'Sinh viên', MaNganh
FROM SinhVien
ORDER BY VaiTro, HoTen;

-- ============================================================
-- TRUY VẤN 5: WINDOW FUNCTION - RANK()
-- Xếp hạng sinh viên trong từng ngành theo điểm trung bình
-- (Minh hoạ PARTITION BY, RANK, DENSE_RANK)
-- ============================================================
SELECT
    sv.MaSV,
    sv.TenSV,
    n.TenNganh,
    ROUND(AVG(dk.Diem), 2)                                          AS diem_tb,
    RANK()       OVER (PARTITION BY n.MaNganh ORDER BY AVG(dk.Diem) DESC)  AS xep_hang_nganh,
    DENSE_RANK() OVER (ORDER BY AVG(dk.Diem) DESC)                         AS xep_hang_truong
FROM SinhVien sv
JOIN Nganh   n  ON sv.MaNganh = n.MaNganh
JOIN DangKy  dk ON sv.MaSV    = dk.MaSV
WHERE dk.Diem IS NOT NULL
GROUP BY sv.MaSV, sv.TenSV, n.MaNganh, n.TenNganh
ORDER BY n.TenNganh, xep_hang_nganh;

-- ============================================================
-- TRUY VẤN 6: CTE (Common Table Expression)
-- Tìm sinh viên CHƯA đăng ký bất kỳ học phần bắt buộc nào
-- trong học kỳ 1 năm 2024-2025
-- (Minh hoạ WITH ... AS)
-- ============================================================
WITH sv_dang_ky_hk1 AS (
    -- Lấy danh sách sinh viên đã đăng ký trong HK1 2024-2025
    SELECT DISTINCT dk.MaSV
    FROM DangKy dk
    JOIN LopHocPhan lhp ON dk.MaLHP = lhp.MaLHP
    WHERE lhp.HocKy = 1 AND lhp.NamHoc = '2024-2025'
),
sv_co_diem_duoi_5 AS (
    -- Sinh viên có ít nhất 1 môn dưới 5 điểm
    SELECT DISTINCT MaSV FROM DangKy WHERE Diem < 5
)
SELECT
    sv.MaSV,
    sv.TenSV,
    n.TenNganh,
    CASE
        WHEN sv.MaSV NOT IN (SELECT MaSV FROM sv_dang_ky_hk1) THEN 'Chưa đăng ký HK1'
        WHEN sv.MaSV IN (SELECT MaSV FROM sv_co_diem_duoi_5)  THEN 'Có môn dưới 5đ'
        ELSE 'Bình thường'
    END AS tinh_trang
FROM SinhVien sv
JOIN Nganh n ON sv.MaNganh = n.MaNganh
ORDER BY tinh_trang, n.TenNganh, sv.TenSV;

-- ============================================================
-- TRUY VẤN 7: KẾT NỐI NGOÀI (LEFT JOIN + COALESCE)
-- Danh sách tất cả lớp học phần và số SV đã đăng ký
-- (kể cả lớp chưa có ai đăng ký)
-- (Minh hoạ LEFT JOIN, COALESCE, FILTER)
-- ============================================================
SELECT
    lhp.MaLHP,
    hp.TenHP,
    gv.TenGV,
    p.ToaNha || p.MaPhong                          AS Phong,
    CASE lhp.Thu
        WHEN 2 THEN 'Thứ Hai'   WHEN 3 THEN 'Thứ Ba'
        WHEN 4 THEN 'Thứ Tư'    WHEN 5 THEN 'Thứ Năm'
        WHEN 6 THEN 'Thứ Sáu'   WHEN 7 THEN 'Thứ Bảy'
    END                                            AS Thu,
    lhp.TietBD,
    lhp.SiSoToiDa,
    COALESCE(COUNT(dk.MaSV), 0)                   AS so_sv_dang_ky,
    lhp.SiSoToiDa - COALESCE(COUNT(dk.MaSV), 0)  AS con_trong,
    ROUND(100.0 * COUNT(dk.MaSV) / lhp.SiSoToiDa, 1) AS ty_le_pct,
    COALESCE(ROUND(AVG(dk.Diem FILTER (WHERE dk.Diem IS NOT NULL)), 2), 0) AS diem_tb_lop
FROM LopHocPhan lhp
JOIN HocPhan   hp ON lhp.MaHP   = hp.MaHP
JOIN GiangVien gv ON lhp.MaGV   = gv.MaGV
JOIN Phong     p  ON lhp.MaPhong = p.MaPhong
LEFT JOIN DangKy dk ON lhp.MaLHP = dk.MaLHP
GROUP BY lhp.MaLHP, hp.TenHP, gv.TenGV, p.ToaNha, p.MaPhong, lhp.Thu, lhp.TietBD, lhp.SiSoToiDa
ORDER BY ty_le_pct DESC;

-- ============================================================
-- TRUY VẤN 8: RÀNG BUỘC + PHÂN TÍCH
-- Phát hiện "xung đột lịch": GV dạy 2 lớp cùng thứ, cùng tiết
-- (Minh hoạ Self-Join)
-- ============================================================
SELECT
    l1.MaGV,
    gv.TenGV,
    l1.MaLHP   AS LHP_1,
    l2.MaLHP   AS LHP_2,
    l1.Thu,
    l1.TietBD,
    l1.NamHoc,
    l1.HocKy
FROM LopHocPhan l1
JOIN LopHocPhan l2 ON
    l1.MaGV   = l2.MaGV      AND   -- Cùng giảng viên
    l1.NamHoc = l2.NamHoc    AND   -- Cùng năm học
    l1.HocKy  = l2.HocKy     AND   -- Cùng học kỳ
    l1.Thu    = l2.Thu        AND   -- Cùng thứ
    l1.TietBD = l2.TietBD    AND   -- Cùng tiết bắt đầu
    l1.MaLHP  < l2.MaLHP           -- Tránh lặp
JOIN GiangVien gv ON l1.MaGV = gv.MaGV;
-- Nếu kết quả rỗng -> không có xung đột lịch dạy ✓

-- ============================================================
-- TRUY VẤN 9: PHÂN TÍCH KẾT QUẢ HỌC TẬP
-- Thống kê theo học kỳ: số SV đạt/trượt/chưa thi từng môn
-- (Minh hoạ GROUP BY nhiều chiều + FILTER + CASE)
-- ============================================================
SELECT
    hp.TenHP,
    lhp.HocKy,
    lhp.NamHoc,
    COUNT(dk.MaSV)                                          AS tong_dk,
    COUNT(dk.Diem FILTER (WHERE dk.Diem >= 5))             AS so_dat,
    COUNT(dk.Diem FILTER (WHERE dk.Diem < 5))              AS so_truot,
    COUNT(*) FILTER (WHERE dk.Diem IS NULL)                AS chua_thi,
    ROUND(100.0 *
          COUNT(dk.Diem FILTER (WHERE dk.Diem >= 5)) /
          NULLIF(COUNT(dk.Diem), 0), 1)                    AS ty_le_dat_pct,
    ROUND(AVG(dk.Diem FILTER (WHERE dk.Diem IS NOT NULL)), 2) AS diem_tb
FROM DangKy dk
JOIN LopHocPhan lhp ON dk.MaLHP = lhp.MaLHP
JOIN HocPhan    hp  ON lhp.MaHP  = hp.MaHP
GROUP BY hp.MaHP, hp.TenHP, lhp.HocKy, lhp.NamHoc
ORDER BY hp.TenHP, lhp.NamHoc, lhp.HocKy;

-- ============================================================
-- TRUY VẤN 10: RECURSIVE CTE (Đệ quy)
-- Tính số tín chỉ tích lũy có thể đăng ký theo lộ trình
-- (Minh hoạ WITH RECURSIVE - cây môn học tiên quyết)
-- Giả sử MA101 -> MA201 -> CS202 -> CS301 (chain)
-- ============================================================
WITH RECURSIVE lo_trinh(MaHP, TenHP, SoTC, cap, duong_di) AS (
    -- Cơ sở: học phần gốc (không cần tiên quyết - giả lập)
    SELECT hp.MaHP, hp.TenHP, hp.SoTC,
           1 AS cap,
           hp.TenHP AS duong_di
    FROM HocPhan hp
    WHERE hp.MaHP IN ('MA101', 'EN101', 'CS101')  -- Môn không cần tiên quyết

    UNION ALL

    -- Đệ quy: các môn có thể học sau khi hoàn thành môn trước
    SELECT hp.MaHP, hp.TenHP, hp.SoTC,
           lt.cap + 1,
           lt.duong_di || ' → ' || hp.TenHP
    FROM HocPhan hp
    JOIN lo_trinh lt ON (
        -- Giả lập quan hệ tiên quyết
        (lt.MaHP = 'MA101' AND hp.MaHP = 'MA201') OR
        (lt.MaHP = 'CS101' AND hp.MaHP = 'CS202') OR
        (lt.MaHP = 'CS202' AND hp.MaHP = 'CS301') OR
        (lt.MaHP = 'CS201' AND hp.MaHP = 'CS301') OR
        (lt.MaHP = 'CS101' AND hp.MaHP = 'CS401') OR
        (lt.MaHP = 'CS201' AND hp.MaHP = 'CS601')
    )
    WHERE lt.cap < 5  -- Giới hạn độ sâu đệ quy
)
SELECT
    MaHP,
    TenHP,
    SoTC,
    cap      AS cap_hoc,
    duong_di AS lo_trinh_hoc
FROM lo_trinh
ORDER BY cap, MaHP;

SELECT 'Hoàn thành 10 truy vấn SQL nâng cao.' AS ket_qua;
