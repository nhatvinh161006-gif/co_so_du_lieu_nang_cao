-- ============================================================
--  FILE 01: THIẾT KẾ VẬT LÝ (Physical Design)
--  Hệ thống Quản lý Đào tạo Đại học
--  Nhóm N10 - Môn Cơ sở Dữ liệu Nâng cao
--  Database: PostgreSQL
-- ============================================================

-- Xóa schema cũ nếu có để chạy lại sạch
DROP SCHEMA IF EXISTS qldt CASCADE;
CREATE SCHEMA qldt;
SET search_path = qldt;

-- ============================================================
-- BẢNG 1: KHOA
-- Lưu thông tin các khoa trong trường
-- ============================================================
CREATE TABLE Khoa (
    MaKhoa      CHAR(10)     PRIMARY KEY,
    TenKhoa     VARCHAR(100) NOT NULL UNIQUE,
    DiaChi      VARCHAR(200),
    DienThoai   VARCHAR(15),
    CONSTRAINT chk_khoa_ma CHECK (MaKhoa ~ '^[A-Z0-9]+$')
);
COMMENT ON TABLE  Khoa IS 'Bảng lưu thông tin các khoa trong trường';
COMMENT ON COLUMN Khoa.MaKhoa IS 'Mã khoa - chỉ chứa chữ hoa và số';

-- ============================================================
-- BẢNG 2: NGANH
-- Mỗi ngành thuộc một khoa
-- ============================================================
CREATE TABLE Nganh (
    MaNganh     CHAR(10)     PRIMARY KEY,
    TenNganh    VARCHAR(100) NOT NULL,
    MaKhoa      CHAR(10)     NOT NULL,
    CONSTRAINT fk_nganh_khoa FOREIGN KEY (MaKhoa)
        REFERENCES Khoa(MaKhoa) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT uq_nganh_ten_khoa UNIQUE (TenNganh, MaKhoa)
);
COMMENT ON TABLE Nganh IS 'Bảng lưu thông tin ngành học, mỗi ngành thuộc một khoa';

-- ============================================================
-- BẢNG 3: GIANGVIEN
-- Thông tin giảng viên, thuộc một khoa
-- ============================================================
CREATE TABLE GiangVien (
    MaGV        CHAR(10)     PRIMARY KEY,
    TenGV       VARCHAR(100) NOT NULL,
    HocVi       VARCHAR(20)  NOT NULL DEFAULT 'CN',
    MaKhoa      CHAR(10),
    Email       VARCHAR(100) UNIQUE,
    CONSTRAINT chk_hocvi CHECK (HocVi IN ('CN','ThS','TS','PGS','GS')),
    CONSTRAINT fk_gv_khoa FOREIGN KEY (MaKhoa)
        REFERENCES Khoa(MaKhoa) ON UPDATE CASCADE ON DELETE SET NULL
);
COMMENT ON COLUMN GiangVien.HocVi IS 'Học vị: CN, ThS, TS, PGS, GS';

-- ============================================================
-- BẢNG 4: PHONG
-- Phòng học với sức chứa
-- ============================================================
CREATE TABLE Phong (
    MaPhong     CHAR(10)     PRIMARY KEY,
    ToaNha      VARCHAR(10)  NOT NULL,
    SucChua     SMALLINT     NOT NULL DEFAULT 50,
    LoaiPhong   VARCHAR(20)  DEFAULT 'LyThuyet',
    CONSTRAINT chk_phong_suc_chua CHECK (SucChua BETWEEN 10 AND 500),
    CONSTRAINT chk_loai_phong CHECK (LoaiPhong IN ('LyThuyet','ThucHanh','HoiTruong'))
);
COMMENT ON TABLE Phong IS 'Phòng học: tòa nhà và sức chứa';

-- ============================================================
-- BẢNG 5: HOCPHAN
-- Học phần (môn học) với số tín chỉ
-- ============================================================
CREATE TABLE HocPhan (
    MaHP        CHAR(10)     PRIMARY KEY,
    TenHP       VARCHAR(200) NOT NULL,
    SoTC        SMALLINT     NOT NULL,
    MoTa        TEXT,
    CONSTRAINT chk_hp_sotc CHECK (SoTC BETWEEN 1 AND 10)
);
COMMENT ON TABLE HocPhan IS 'Bảng học phần (môn học), mỗi môn có số tín chỉ';

-- ============================================================
-- BẢNG 6: SINHVIEN
-- Thông tin sinh viên, thuộc một ngành
-- ============================================================
CREATE TABLE SinhVien (
    MaSV        CHAR(10)     PRIMARY KEY,
    TenSV       VARCHAR(100) NOT NULL,
    NgaySinh    DATE         NOT NULL,
    GioiTinh    CHAR(3)      DEFAULT 'Nam',
    MaNganh     CHAR(10)     NOT NULL,
    Email       VARCHAR(100) UNIQUE,
    CONSTRAINT chk_sv_gioitinh CHECK (GioiTinh IN ('Nam','Nu','Khac')),
    CONSTRAINT chk_sv_ngaysinh CHECK (NgaySinh BETWEEN '1990-01-01' AND CURRENT_DATE),
    CONSTRAINT fk_sv_nganh FOREIGN KEY (MaNganh)
        REFERENCES Nganh(MaNganh) ON UPDATE CASCADE ON DELETE RESTRICT
);
COMMENT ON TABLE SinhVien IS 'Thông tin sinh viên, mỗi sinh viên thuộc một ngành';

-- ============================================================
-- BẢNG 7: LOPHOCPHAN
-- Lớp học phần: một học phần mở trong một học kỳ
-- ============================================================
CREATE TABLE LopHocPhan (
    MaLHP       CHAR(15)     PRIMARY KEY,
    MaHP        CHAR(10)     NOT NULL,
    HocKy       SMALLINT     NOT NULL,
    NamHoc      CHAR(9)      NOT NULL,  -- Ví dụ: '2024-2025'
    Thu         SMALLINT     NOT NULL,  -- 2..7 (Thứ 2 đến Thứ 7)
    TietBD      SMALLINT     NOT NULL,  -- Tiết bắt đầu 1..12
    SoTiet      SMALLINT     NOT NULL DEFAULT 2,
    MaGV        CHAR(10)     NOT NULL,
    MaPhong     CHAR(10)     NOT NULL,
    SiSoToiDa   SMALLINT     NOT NULL DEFAULT 50,
    CONSTRAINT chk_lhp_hocky  CHECK (HocKy IN (1, 2, 3)),
    CONSTRAINT chk_lhp_thu    CHECK (Thu BETWEEN 2 AND 7),
    CONSTRAINT chk_lhp_tietbd CHECK (TietBD BETWEEN 1 AND 12),
    CONSTRAINT chk_lhp_namhoc CHECK (NamHoc ~ '^\d{4}-\d{4}$'),
    CONSTRAINT fk_lhp_hp     FOREIGN KEY (MaHP)
        REFERENCES HocPhan(MaHP) ON UPDATE CASCADE,
    CONSTRAINT fk_lhp_gv     FOREIGN KEY (MaGV)
        REFERENCES GiangVien(MaGV) ON UPDATE CASCADE,
    CONSTRAINT fk_lhp_phong  FOREIGN KEY (MaPhong)
        REFERENCES Phong(MaPhong) ON UPDATE CASCADE
);
COMMENT ON TABLE LopHocPhan IS 'Lớp học phần: một học phần được mở vào học kỳ cụ thể';
COMMENT ON COLUMN LopHocPhan.HocKy  IS '1 = HK1, 2 = HK2, 3 = HK hè';
COMMENT ON COLUMN LopHocPhan.Thu    IS '2 = Thứ Hai ... 7 = Thứ Bảy';
COMMENT ON COLUMN LopHocPhan.TietBD IS 'Tiết bắt đầu (1–12)';

-- ============================================================
-- BẢNG 8: DANGKY (Đăng ký + Điểm)
-- Sinh viên đăng ký lớp học phần và nhận điểm
-- ============================================================
CREATE TABLE DangKy (
    MaSV        CHAR(10)     NOT NULL,
    MaLHP       CHAR(15)     NOT NULL,
    NgayDangKy  DATE         NOT NULL DEFAULT CURRENT_DATE,
    Diem        NUMERIC(4,2),               -- NULL = chưa có điểm
    CONSTRAINT pk_dangky PRIMARY KEY (MaSV, MaLHP),
    CONSTRAINT chk_diem CHECK (Diem IS NULL OR Diem BETWEEN 0 AND 10),
    CONSTRAINT fk_dk_sv  FOREIGN KEY (MaSV)
        REFERENCES SinhVien(MaSV) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_dk_lhp FOREIGN KEY (MaLHP)
        REFERENCES LopHocPhan(MaLHP) ON UPDATE CASCADE ON DELETE CASCADE
);
COMMENT ON TABLE DangKy IS 'Đăng ký học phần: liên kết sinh viên và lớp học phần, lưu điểm';
COMMENT ON COLUMN DangKy.Diem IS 'Điểm số 0.0–10.0, NULL nếu chưa có điểm';

-- ============================================================
-- INDEX - Tăng tốc truy vấn thường gặp
-- ============================================================
CREATE INDEX idx_sinhvien_nganh   ON SinhVien(MaNganh);
CREATE INDEX idx_lhp_hp           ON LopHocPhan(MaHP);
CREATE INDEX idx_lhp_gv           ON LopHocPhan(MaGV);
CREATE INDEX idx_lhp_hocky_namhoc ON LopHocPhan(HocKy, NamHoc);
CREATE INDEX idx_dangky_masv      ON DangKy(MaSV);
CREATE INDEX idx_dangky_malh      ON DangKy(MaLHP);
CREATE INDEX idx_dangky_diem      ON DangKy(Diem) WHERE Diem IS NOT NULL;

-- ============================================================
-- VIEW - Lược đồ quan hệ đã chuẩn hóa (3NF)
-- Kết quả phân rã: 8 quan hệ con ↔ 8 bảng ở trên
-- ============================================================
-- Sơ đồ phụ thuộc hàm:
--   MaKhoa -> TenKhoa
--   MaNganh -> TenNganh, MaKhoa
--   MaGV -> TenGV, HocVi, MaKhoa
--   MaHP -> TenHP, SoTC
--   MaPhong -> ToaNha, SucChua
--   MaSV -> TenSV, NgaySinh, MaNganh
--   MaLHP -> MaHP, HocKy, NamHoc, Thu, TietBD, SoTiet, MaGV, MaPhong
--   (MaSV, MaLHP) -> Diem

SELECT 'Thiết kế vật lý hoàn tất: 8 bảng, 7 khóa ngoại, 12 ràng buộc CHECK, 7 index.' AS thong_bao;
