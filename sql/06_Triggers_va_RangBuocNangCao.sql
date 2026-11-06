-- ============================================================
--  FILE 06: TRIGGERS VÀ RÀNG BUỘC TOÀN VẸN NÂNG CAO
--  Nhóm N10 - Quản lý Đào tạo Đại học
--  Database: PostgreSQL (PL/pgSQL)
-- ============================================================
SET search_path = qldt;

-- ============================================================
-- BẢNG PHỤ TRỢ: LichSuThayDoiDiem (Audit Trail Table)
-- Lưu vết toàn bộ lịch sử chỉnh sửa điểm số
-- ============================================================
CREATE TABLE IF NOT EXISTS LichSuThayDoiDiem (
    MaLog        BIGSERIAL PRIMARY KEY,
    MaSV         CHAR(10)     NOT NULL,
    MaLHP        CHAR(15)     NOT NULL,
    DiemCu       NUMERIC(4,2),
    DiemMoi      NUMERIC(4,2),
    NguoiThayDoi VARCHAR(50)  DEFAULT CURRENT_USER,
    ThoiGian     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    HanhDong     VARCHAR(20)  DEFAULT 'UPDATE'
);
COMMENT ON TABLE LichSuThayDoiDiem IS 'Bảng ghi vết kiểm toán (Audit Trail) lịch sử chỉnh sửa điểm thi';

-- ============================================================
-- TRIGGER 1: KIỂM TRA SĨ SỐ TỐI ĐA TRƯỚC KHI THÊM ĐĂNG KÝ
-- Ngăn chặn tuyệt đối việc thêm sinh viên vượt quá SiSoToiDa
-- ============================================================
CREATE OR REPLACE FUNCTION fn_tg_KiemTraSiSoToiDa()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_SiSoHienTai INT;
    v_SiSoToiDa   INT;
BEGIN
    SELECT SiSoToiDa INTO v_SiSoToiDa
    FROM LopHocPhan
    WHERE MaLHP = NEW.MaLHP;

    SELECT COUNT(*) INTO v_SiSoHienTai
    FROM DangKy
    WHERE MaLHP = NEW.MaLHP;

    IF v_SiSoHienTai >= v_SiSoToiDa THEN
        RAISE EXCEPTION 'TRIGGER VIOLATION: Lớp học phần % đã đủ sĩ số tối đa (%/%)!',
            NEW.MaLHP, v_SiSoHienTai, v_SiSoToiDa;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS tg_KiemTraSiSoDangKy ON DangKy;
CREATE TRIGGER tg_KiemTraSiSoDangKy
BEFORE INSERT ON DangKy
FOR EACH ROW
EXECUTE FUNCTION fn_tg_KiemTraSiSoToiDa();

-- ============================================================
-- TRIGGER 2: CHỐNG TRÙNG LỊCH PHÒNG HỌC VÀ GIẢNG VIÊN
-- Khi thêm hoặc cập nhật Lớp học phần, kiểm tra:
-- a) Giảng viên đó đã có lớp khác cùng Thứ, cùng khoảng tiết chưa?
-- b) Phòng học đó đã có lớp khác cùng Thứ, cùng khoảng tiết chưa?
-- ============================================================
CREATE OR REPLACE FUNCTION fn_tg_KiemTraLichLopHocPhan()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_TietKT_New INT;
    v_ConflictGV INT;
    v_ConflictPhong INT;
BEGIN
    v_TietKT_New := NEW.TietBD + NEW.SoTiet - 1;

    -- a) Kiểm tra trùng lịch giảng viên
    SELECT COUNT(*) INTO v_ConflictGV
    FROM LopHocPhan
    WHERE MaLHP <> COALESCE(NEW.MaLHP, '')
      AND MaGV = NEW.MaGV
      AND NamHoc = NEW.NamHoc
      AND HocKy = NEW.HocKy
      AND Thu = NEW.Thu
      AND (NEW.TietBD <= (TietBD + SoTiet - 1))
      AND (TietBD <= v_TietKT_New);

    IF v_ConflictGV > 0 THEN
        RAISE EXCEPTION 'TRIGGER VIOLATION: Giảng viên % đã có lịch dạy lớp khác vào Thứ %, tiết %-%!',
            NEW.MaGV, NEW.Thu, NEW.TietBD, v_TietKT_New;
    END IF;

    -- b) Kiểm tra trùng lịch phòng học
    SELECT COUNT(*) INTO v_ConflictPhong
    FROM LopHocPhan
    WHERE MaLHP <> COALESCE(NEW.MaLHP, '')
      AND MaPhong = NEW.MaPhong
      AND NamHoc = NEW.NamHoc
      AND HocKy = NEW.HocKy
      AND Thu = NEW.Thu
      AND (NEW.TietBD <= (TietBD + SoTiet - 1))
      AND (TietBD <= v_TietKT_New);

    IF v_ConflictPhong > 0 THEN
        RAISE EXCEPTION 'TRIGGER VIOLATION: Phòng học % đã có lớp khác sử dụng vào Thứ %, tiết %-%!',
            NEW.MaPhong, NEW.Thu, NEW.TietBD, v_TietKT_New;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS tg_KiemTraLichLopHocPhan ON LopHocPhan;
CREATE TRIGGER tg_KiemTraLichLopHocPhan
BEFORE INSERT OR UPDATE ON LopHocPhan
FOR EACH ROW
EXECUTE FUNCTION fn_tg_KiemTraLichLopHocPhan();

-- ============================================================
-- TRIGGER 3: AUDIT TRAIL LỊCH SỬ THAY ĐỔI ĐIỂM
-- Tự động ghi lại điểm cũ, điểm mới, user thực hiện và timestamp
-- ============================================================
CREATE OR REPLACE FUNCTION fn_tg_GhiLichSuSuaDiem()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    -- Chỉ ghi nhận nếu điểm thực sự có thay đổi
    IF (OLD.Diem IS DISTINCT FROM NEW.Diem) THEN
        INSERT INTO LichSuThayDoiDiem (MaSV, MaLHP, DiemCu, DiemMoi, NguoiThayDoi, ThoiGian, HanhDong)
        VALUES (
            NEW.MaSV,
            NEW.MaLHP,
            OLD.Diem,
            NEW.Diem,
            CURRENT_USER,
            CURRENT_TIMESTAMP,
            CASE
                WHEN OLD.Diem IS NULL AND NEW.Diem IS NOT NULL THEN 'NHAP_DIEM_LAN_DAU'
                ELSE 'SUA_DIEM'
            END
        );
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS tg_GhiLichSuSuaDiem ON DangKy;
CREATE TRIGGER tg_GhiLichSuSuaDiem
AFTER UPDATE OF Diem ON DangKy
FOR EACH ROW
EXECUTE FUNCTION fn_tg_GhiLichSuSuaDiem();

SELECT 'Tạo thành công 3 Trigger nâng cao và bảng Audit Log.' AS ket_qua;
