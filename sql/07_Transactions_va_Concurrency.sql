-- ============================================================
--  FILE 07: QUẢN LÝ GIAO DỊCH & ĐIỀU KHIỂN TƯƠNG TRANH
--  Nhóm N10 - Quản lý Đào tạo Đại học
--  Database: PostgreSQL (ACID, Locks, Savepoints)
-- ============================================================
SET search_path = qldt;

-- ============================================================
-- KỊCH BẢN 1: GIAO DỊCH VỚI ĐIỂM LƯU (SAVEPOINT)
-- Đăng ký gói môn học: Nếu 1 môn phụ bị lỗi, chỉ rollback môn đó
-- chứ không hủy toàn bộ các môn đã đăng ký thành công
-- ============================================================
DO $$
DECLARE
    v_SV CHAR(10) := 'SV2001004';
BEGIN
    RAISE NOTICE 'Bắt đầu giao dịch đăng ký nhiều môn cho sinh viên %...', v_SV;

    -- Đăng ký môn 1 (Bắt buộc)
    INSERT INTO DangKy (MaSV, MaLHP, NgayDangKy)
    VALUES (v_SV, 'LHP2401001', CURRENT_DATE);
    RAISE NOTICE 'Đăng ký thành công môn chính LHP2401001.';

    -- Thiết lập điểm lưu (Savepoint) trước khi đăng ký môn tự chọn
    SAVEPOINT sp_mon_tu_chon;

    BEGIN
        -- Thử đăng ký môn tự chọn (giả sử có thể gặp lỗi hết chỗ hoặc vi phạm)
        INSERT INTO DangKy (MaSV, MaLHP, NgayDangKy)
        VALUES (v_SV, 'LHP2401002', CURRENT_DATE);
        RAISE NOTICE 'Đăng ký thành công môn tự chọn LHP2401002.';
    EXCEPTION WHEN OTHERS THEN
        RAISE WARNING 'Lỗi khi đăng ký môn tự chọn: %. Rollback về Savepoint.', SQLERRM;
        ROLLBACK TO SAVEPOINT sp_mon_tu_chon;
    END;

    -- Giải phóng savepoint
    RELEASE SAVEPOINT sp_mon_tu_chon;

    RAISE NOTICE 'Hoàn tất giao dịch đăng ký theo khối.';
END;
$$;

-- ============================================================
-- KỊCH BẢN 2: CHUYỂN LỚP HỌC PHẦN (Atomic Swap)
-- Sinh viên muốn đổi từ Lớp A sang Lớp B:
-- Phải đảm bảo tính nguyên tử (Atomicity): Nếu lớp B hết chỗ hoặc
-- trùng lịch thì việc hủy lớp A cũng phải bị hủy (ROLLBACK).
-- ============================================================
CREATE OR REPLACE PROCEDURE sp_ChuyenLopHocPhan(
    p_MaSV    CHAR(10),
    p_MaLHP_Cu CHAR(15),
    p_MaLHP_Moi CHAR(15),
    OUT p_Success BOOLEAN,
    OUT p_Message TEXT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_SuccessReg BOOLEAN;
    v_MsgReg     TEXT;
BEGIN
    p_Success := FALSE;

    -- Kiểm tra SV có đang học lớp cũ không
    IF NOT EXISTS (SELECT 1 FROM DangKy WHERE MaSV = p_MaSV AND MaLHP = p_MaLHP_Cu) THEN
        p_Message := FORMAT('Sinh viên chưa đăng ký lớp cũ %s!', p_MaLHP_Cu);
        RETURN;
    END IF;

    -- BƯỚC 1: Xóa tạm đăng ký lớp cũ
    DELETE FROM DangKy WHERE MaSV = p_MaSV AND MaLHP = p_MaLHP_Cu;

    -- BƯỚC 2: Gọi thủ tục đăng ký lớp mới
    CALL sp_DangKyHocPhan(p_MaSV, p_MaLHP_Moi, v_SuccessReg, v_MsgReg);

    -- BƯỚC 3: Nếu đăng ký lớp mới thất bại -> hoàn tác (Exception rollback)
    IF NOT v_SuccessReg THEN
        p_Message := FORMAT('Chuyển lớp thất bại: %s. Đã giữ nguyên lớp cũ.', v_MsgReg);
        -- Raise exception để rollback toàn bộ thay đổi (phục hồi lại lớp cũ)
        RAISE EXCEPTION '%', p_Message;
    ELSE
        p_Success := TRUE;
        p_Message := FORMAT('Đã chuyển thành công từ %s sang %s.', p_MaLHP_Cu, p_MaLHP_Moi);
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        p_Success := FALSE;
        p_Message := SQLERRM;
END;
$$;

SELECT 'Tạo thành công kịch bản Transactions và thủ tục chuyển lớp nguyên tử.' AS ket_qua;
