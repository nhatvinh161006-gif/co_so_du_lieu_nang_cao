-- ============================================================
--  FILE 05: STORED PROCEDURES & FUNCTIONS (Hàm và Thủ tục lưu trữ)
--  Nhóm N10 - Quản lý Đào tạo Đại học
--  Database: PostgreSQL (PL/pgSQL)
-- ============================================================
SET search_path = qldt;

-- ============================================================
-- HÀM 1: fn_TinhDiemTrungBinhTichLuy
-- Tính điểm trung bình tích lũy hệ 10 có trọng số tín chỉ của một SV
-- ============================================================
CREATE OR REPLACE FUNCTION fn_TinhDiemTrungBinhTichLuy(p_MaSV CHAR(10))
RETURNS NUMERIC(4,2)
LANGUAGE plpgsql
AS $$
DECLARE
    v_GPA NUMERIC(4,2);
BEGIN
    -- Kiểm tra sinh viên có tồn tại không
    IF NOT EXISTS (SELECT 1 FROM SinhVien WHERE MaSV = p_MaSV) THEN
        RAISE EXCEPTION 'Sinh viên có mã % không tồn tại trong hệ thống!', p_MaSV;
    END IF;

    SELECT
        ROUND(SUM(dk.Diem * hp.SoTC) / NULLIF(SUM(hp.SoTC), 0), 2)
    INTO v_GPA
    FROM DangKy dk
    JOIN LopHocPhan lhp ON dk.MaLHP = lhp.MaLHP
    JOIN HocPhan hp ON lhp.MaHP = hp.MaHP
    WHERE dk.MaSV = p_MaSV AND dk.Diem IS NOT NULL;

    RETURN COALESCE(v_GPA, 0.00);
END;
$$;

-- ============================================================
-- HÀM 2: fn_KiemTraXungDotLichSV
-- Kiểm tra xem sinh viên đăng ký lớp mới có bị trùng lịch học
-- với các lớp đã đăng ký trong cùng học kỳ không
-- Trả về: TRUE nếu có xung đột, FALSE nếu lịch học hợp lệ
-- ============================================================
CREATE OR REPLACE FUNCTION fn_KiemTraXungDotLichSV(
    p_MaSV  CHAR(10),
    p_MaLHP CHAR(15)
)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
DECLARE
    v_HocKy   SMALLINT;
    v_NamHoc  CHAR(9);
    v_Thu     SMALLINT;
    v_TietBD  SMALLINT;
    v_SoTiet  SMALLINT;
    v_ConflictCount INT;
BEGIN
    -- Lấy thông tin lịch của lớp định đăng ký
    SELECT HocKy, NamHoc, Thu, TietBD, SoTiet
    INTO v_HocKy, v_NamHoc, v_Thu, v_TietBD, v_SoTiet
    FROM LopHocPhan
    WHERE MaLHP = p_MaLHP;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Lớp học phần % không tồn tại!', p_MaLHP;
    END IF;

    -- Kiểm tra giao nhau về khoảng thời gian tiết học trên cùng Thứ, cùng Học kỳ
    -- Tiết lớp mới: [TietBD, TietBD + SoTiet - 1]
    -- Tiết lớp cũ: [TietBD, TietBD + SoTiet - 1]
    -- Hai khoảng giao nhau khi: TietBD_1 <= TietKT_2 AND TietBD_2 <= TietKT_1
    SELECT COUNT(*)
    INTO v_ConflictCount
    FROM DangKy dk
    JOIN LopHocPhan lhp ON dk.MaLHP = lhp.MaLHP
    WHERE dk.MaSV = p_MaSV
      AND lhp.MaLHP <> p_MaLHP
      AND lhp.HocKy = v_HocKy
      AND lhp.NamHoc = v_NamHoc
      AND lhp.Thu = v_Thu
      AND (v_TietBD <= (lhp.TietBD + lhp.SoTiet - 1))
      AND (lhp.TietBD <= (v_TietBD + v_SoTiet - 1));

    RETURN v_ConflictCount > 0;
END;
$$;

-- ============================================================
-- THỦ TỤC 3: sp_DangKyHocPhan (Transaction-safe)
-- Thủ tục an toàn cho sinh viên đăng ký lớp học phần:
-- 1. Kiểm tra SV và LHP tồn tại
-- 2. Kiểm tra SV đã đăng ký lớp này chưa
-- 3. Kiểm tra sĩ số lớp còn chỗ không (khóa bi quan SELECT FOR UPDATE)
-- 4. Kiểm tra xung đột thời khóa biểu
-- 5. Thực hiện đăng ký
-- ============================================================
CREATE OR REPLACE PROCEDURE sp_DangKyHocPhan(
    IN  p_MaSV    CHAR(10),
    IN  p_MaLHP   CHAR(15),
    OUT p_Success BOOLEAN,
    OUT p_Message TEXT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_SiSoHienTai INT;
    v_SiSoToiDa   INT;
    v_TrungLich   BOOLEAN;
BEGIN
    p_Success := FALSE;

    -- 1. Kiểm tra tồn tại
    IF NOT EXISTS (SELECT 1 FROM SinhVien WHERE MaSV = p_MaSV) THEN
        p_Message := 'Lỗi: Sinh viên không tồn tại.';
        RETURN;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM LopHocPhan WHERE MaLHP = p_MaLHP) THEN
        p_Message := 'Lỗi: Lớp học phần không tồn tại.';
        RETURN;
    END IF;

    -- 2. Kiểm tra đã đăng ký chưa
    IF EXISTS (SELECT 1 FROM DangKy WHERE MaSV = p_MaSV AND MaLHP = p_MaLHP) THEN
        p_Message := 'Thông báo: Sinh viên đã đăng ký lớp học phần này trước đó.';
        RETURN;
    END IF;

    -- 3. Kiểm tra sĩ số (khóa dòng để tránh tương tranh khi nhiều SV đăng ký cùng lúc)
    SELECT SiSoToiDa INTO v_SiSoToiDa
    FROM LopHocPhan
    WHERE MaLHP = p_MaLHP
    FOR UPDATE;

    SELECT COUNT(*) INTO v_SiSoHienTai
    FROM DangKy
    WHERE MaLHP = p_MaLHP;

    IF v_SiSoHienTai >= v_SiSoToiDa THEN
        p_Message := FORMAT('Lỗi: Lớp học phần đã hết chỗ (%s/%s).', v_SiSoHienTai, v_SiSoToiDa);
        RETURN;
    END IF;

    -- 4. Kiểm tra xung đột thời khóa biểu
    v_TrungLich := fn_KiemTraXungDotLichSV(p_MaSV, p_MaLHP);
    IF v_TrungLich THEN
        p_Message := 'Lỗi: Trùng lịch học với một lớp học phần khác đã đăng ký!';
        RETURN;
    END IF;

    -- 5. Đăng ký thành công
    INSERT INTO DangKy (MaSV, MaLHP, NgayDangKy, Diem)
    VALUES (p_MaSV, p_MaLHP, CURRENT_DATE, NULL);

    p_Success := TRUE;
    p_Message := FORMAT('Đăng ký thành công lớp %s cho sinh viên %s.', p_MaLHP, p_MaSV);
END;
$$;

-- ============================================================
-- HÀM 4: fn_ThongKePhoDiemLHP (Table-valued Function)
-- Trả về bảng thống kê phổ điểm chi tiết của một lớp học phần
-- ============================================================
CREATE OR REPLACE FUNCTION fn_ThongKePhoDiemLHP(p_MaLHP CHAR(15))
RETURNS TABLE (
    MaLop         CHAR(15),
    TenMonHoc     VARCHAR(200),
    TongDangKy    BIGINT,
    SoDaCoDiem    BIGINT,
    DiemCaoNhat   NUMERIC(4,2),
    DiemThapNhat  NUMERIC(4,2),
    DiemTrungBinh NUMERIC(4,2),
    SoXuatSac     BIGINT,   -- [9.0 - 10]
    SoGioi        BIGINT,   -- [8.0 - 8.9]
    SoKha         BIGINT,   -- [6.5 - 7.9]
    SoTrungBinh   BIGINT,   -- [5.0 - 6.4]
    SoTruotMon    BIGINT    -- < 5.0
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        lhp.MaLHP,
        hp.TenHP,
        COUNT(dk.MaSV) AS TongDangKy,
        COUNT(dk.Diem) AS SoDaCoDiem,
        MAX(dk.Diem)   AS DiemCaoNhat,
        MIN(dk.Diem)   AS DiemThapNhat,
        ROUND(AVG(dk.Diem), 2) AS DiemTrungBinh,
        COUNT(dk.Diem) FILTER (WHERE dk.Diem >= 9.0) AS SoXuatSac,
        COUNT(dk.Diem) FILTER (WHERE dk.Diem >= 8.0 AND dk.Diem < 9.0) AS SoGioi,
        COUNT(dk.Diem) FILTER (WHERE dk.Diem >= 6.5 AND dk.Diem < 8.0) AS SoKha,
        COUNT(dk.Diem) FILTER (WHERE dk.Diem >= 5.0 AND dk.Diem < 6.5) AS SoTrungBinh,
        COUNT(dk.Diem) FILTER (WHERE dk.Diem < 5.0)  AS SoTruotMon
    FROM LopHocPhan lhp
    JOIN HocPhan hp ON lhp.MaHP = hp.MaHP
    LEFT JOIN DangKy dk ON lhp.MaLHP = dk.MaLHP
    WHERE lhp.MaLHP = p_MaLHP
    GROUP BY lhp.MaLHP, hp.TenHP;
END;
$$;

SELECT 'Tạo thành công các Stored Procedures & Functions PL/pgSQL.' AS ket_qua;
