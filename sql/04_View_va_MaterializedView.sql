-- ============================================================
--  FILE 04: VIEW VÀ MATERIALIZED VIEW (Khung nhìn nâng cao)
--  Nhóm N10 - Quản lý Đào tạo Đại học
--  Database: PostgreSQL
-- ============================================================
SET search_path = qldt;

-- ============================================================
-- VIEW 1: V_ThongTinSinhVien
-- Tổng hợp thông tin cá nhân, ngành, khoa và tình trạng học tập
-- ============================================================
CREATE OR REPLACE VIEW V_ThongTinSinhVien AS
SELECT
    sv.MaSV,
    sv.TenSV,
    sv.NgaySinh,
    sv.GioiTinh,
    sv.Email,
    n.MaNganh,
    n.TenNganh,
    k.MaKhoa,
    k.TenKhoa,
    COUNT(dk.MaLHP) AS SoMonDaDangKy,
    COUNT(dk.Diem)  AS SoMonDaCoDiem,
    ROUND(AVG(dk.Diem), 2) AS DiemTrungBinh
FROM SinhVien sv
JOIN Nganh n ON sv.MaNganh = n.MaNganh
JOIN Khoa k ON n.MaKhoa = k.MaKhoa
LEFT JOIN DangKy dk ON sv.MaSV = dk.MaSV
GROUP BY sv.MaSV, sv.TenSV, sv.NgaySinh, sv.GioiTinh, sv.Email, n.MaNganh, n.TenNganh, k.MaKhoa, k.TenKhoa;

COMMENT ON VIEW V_ThongTinSinhVien IS 'View tổng hợp thông tin chi tiết sinh viên và điểm trung bình';

-- ============================================================
-- VIEW 2: V_ThoiKhoaBieuChiTiet
-- Thời khóa biểu chi tiết của từng lớp học phần
-- ============================================================
CREATE OR REPLACE VIEW V_ThoiKhoaBieuChiTiet AS
SELECT
    lhp.MaLHP,
    hp.MaHP,
    hp.TenHP,
    hp.SoTC,
    lhp.NamHoc,
    lhp.HocKy,
    CASE lhp.Thu
        WHEN 2 THEN 'Thứ Hai'
        WHEN 3 THEN 'Thứ Ba'
        WHEN 4 THEN 'Thứ Tư'
        WHEN 5 THEN 'Thứ Năm'
        WHEN 6 THEN 'Thứ Sáu'
        WHEN 7 THEN 'Thứ Bảy'
    END AS ThuTrongTuan,
    lhp.TietBD,
    (lhp.TietBD + lhp.SoTiet - 1) AS TietKetThuc,
    lhp.SoTiet,
    gv.MaGV,
    gv.TenGV,
    gv.HocVi,
    p.MaPhong,
    p.ToaNha,
    p.SucChua,
    lhp.SiSoToiDa,
    COUNT(dk.MaSV) AS SiSoHienTai,
    (lhp.SiSoToiDa - COUNT(dk.MaSV)) AS ChoTrong
FROM LopHocPhan lhp
JOIN HocPhan hp ON lhp.MaHP = hp.MaHP
JOIN GiangVien gv ON lhp.MaGV = gv.MaGV
JOIN Phong p ON lhp.MaPhong = p.MaPhong
LEFT JOIN DangKy dk ON lhp.MaLHP = dk.MaLHP
GROUP BY lhp.MaLHP, hp.MaHP, hp.TenHP, hp.SoTC, lhp.NamHoc, lhp.HocKy,
         lhp.Thu, lhp.TietBD, lhp.SoTiet, gv.MaGV, gv.TenGV, gv.HocVi,
         p.MaPhong, p.ToaNha, p.SucChua, lhp.SiSoToiDa;

COMMENT ON VIEW V_ThoiKhoaBieuChiTiet IS 'Thời khóa biểu chi tiết và tình trạng sĩ số còn trống của lớp học phần';

-- ============================================================
-- MATERIALIZED VIEW: MV_BangXepHangSinhVien
-- Khung nhìn vật lý hóa (lưu trữ kết quả trên đĩa để tăng tốc)
-- Dùng cho báo cáo quản lý, xếp hạng học lực toàn trường
-- ============================================================
DROP MATERIALIZED VIEW IF EXISTS MV_BangXepHangSinhVien;
CREATE MATERIALIZED VIEW MV_BangXepHangSinhVien AS
WITH TongHopDiem AS (
    SELECT
        sv.MaSV,
        sv.TenSV,
        n.MaNganh,
        n.TenNganh,
        k.TenKhoa,
        SUM(hp.SoTC) FILTER (WHERE dk.Diem >= 4.0) AS SoTinChiTichLuy,
        ROUND(
            SUM(dk.Diem * hp.SoTC) / NULLIF(SUM(hp.SoTC FILTER (WHERE dk.Diem IS NOT NULL)), 0),
            2
        ) AS GPA,
        COUNT(dk.MaLHP) AS TongSoMon
    FROM SinhVien sv
    JOIN Nganh n ON sv.MaNganh = n.MaNganh
    JOIN Khoa k ON n.MaKhoa = k.MaKhoa
    LEFT JOIN DangKy dk ON sv.MaSV = dk.MaSV
    LEFT JOIN LopHocPhan lhp ON dk.MaLHP = lhp.MaLHP
    LEFT JOIN HocPhan hp ON lhp.MaHP = hp.MaHP
    GROUP BY sv.MaSV, sv.TenSV, n.MaNganh, n.TenNganh, k.TenKhoa
)
SELECT
    MaSV,
    TenSV,
    TenNganh,
    TenKhoa,
    COALESCE(SoTinChiTichLuy, 0) AS SoTinChiTichLuy,
    GPA,
    CASE
        WHEN GPA >= 9.0 THEN 'Xuất sắc'
        WHEN GPA >= 8.0 THEN 'Giỏi'
        WHEN GPA >= 7.0 THEN 'Khá'
        WHEN GPA >= 5.0 THEN 'Trung bình'
        WHEN GPA < 5.0  THEN 'Yếu'
        ELSE 'Chưa xếp loại'
    END AS XepLoaiHocLuc,
    DENSE_RANK() OVER (PARTITION BY MaNganh ORDER BY GPA DESC NULLS LAST) AS HangTrongNganh,
    DENSE_RANK() OVER (ORDER BY GPA DESC NULLS LAST) AS HangToanTruong
FROM TongHopDiem;

-- Tạo chỉ mục duy nhất trên Materialized View để hỗ trợ REFRESH CONCURRENTLY
CREATE UNIQUE INDEX idx_mv_bxh_masv ON MV_BangXepHangSinhVien(MaSV);
CREATE INDEX idx_mv_bxh_nganh ON MV_BangXepHangSinhVien(TenNganh, HangTrongNganh);

-- Lệnh làm mới dữ liệu Materialized View khi có cập nhật điểm:
-- REFRESH MATERIALIZED VIEW CONCURRENTLY qldt.MV_BangXepHangSinhVien;

SELECT 'Tạo thành công 2 Views và 1 Materialized View kèm chỉ mục.' AS ket_qua;
