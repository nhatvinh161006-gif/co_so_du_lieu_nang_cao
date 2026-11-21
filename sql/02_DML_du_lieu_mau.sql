-- ============================================================
--  FILE 02: DỮ LIỆU MẪU (Sample Data)
--  Nhóm N10 - Quản lý Đào tạo Đại học
-- ============================================================
SET search_path = qldt;

-- ---- KHOA ----
INSERT INTO Khoa VALUES
    ('CNTT',   'Công nghệ Thông tin',      'Nhà A1',   '0283.8123456'),
    ('QTKD',   'Quản trị Kinh doanh',      'Nhà B2',   '0283.8123457'),
    ('DTVT',   'Điện tử Viễn thông',       'Nhà C3',   '0283.8123458'),
    ('TOAN',   'Toán - Tin',               'Nhà D4',   '0283.8123459'),
    ('NGOAI',  'Ngoại ngữ',                'Nhà E5',   '0283.8123460');

-- ---- NGANH ----
INSERT INTO Nganh VALUES
    ('KHMT',   'Khoa học Máy tính',        'CNTT'),
    ('HTTT',   'Hệ thống Thông tin',       'CNTT'),
    ('KTPM',   'Kỹ thuật Phần mềm',        'CNTT'),
    ('QTKD01', 'Quản trị Kinh doanh',      'QTKD'),
    ('TMDT',   'Thương mại Điện tử',       'QTKD'),
    ('DTVT01', 'Điện tử Viễn thông',       'DTVT'),
    ('TINHOC', 'Toán Tin học',             'TOAN');

-- ---- GIẢNG VIÊN ----
INSERT INTO GiangVien VALUES
    ('GV001', 'Nguyễn Văn An',    'TS',  'CNTT',  'an.nv@university.edu.vn'),
    ('GV002', 'Trần Thị Bình',    'ThS', 'CNTT',  'binh.tt@university.edu.vn'),
    ('GV003', 'Lê Minh Cường',    'PGS', 'CNTT',  'cuong.lm@university.edu.vn'),
    ('GV004', 'Phạm Thị Dung',    'TS',  'QTKD',  'dung.pt@university.edu.vn'),
    ('GV005', 'Hoàng Văn Em',     'ThS', 'DTVT',  'em.hv@university.edu.vn'),
    ('GV006', 'Vũ Thị Phương',    'TS',  'CNTT',  'phuong.vt@university.edu.vn'),
    ('GV007', 'Đặng Quốc Hùng',   'GS',  'TOAN',  'hung.dq@university.edu.vn'),
    ('GV008', 'Bùi Thị Lan',      'ThS', 'CNTT',  'lan.bt@university.edu.vn');

-- ---- PHÒNG HỌC ----
INSERT INTO Phong VALUES
    ('P101', 'A', 60,  'LyThuyet'),
    ('P102', 'A', 45,  'ThucHanh'),
    ('P201', 'A', 80,  'LyThuyet'),
    ('P301', 'B', 120, 'HoiTruong'),
    ('P401', 'B', 40,  'ThucHanh'),
    ('P501', 'C', 60,  'LyThuyet'),
    ('P601', 'C', 50,  'ThucHanh');

-- ---- HỌC PHẦN ----
INSERT INTO HocPhan VALUES
    ('CS101', 'Lập trình Cơ bản',                    3, 'Giới thiệu lập trình với Python'),
    ('CS201', 'Cơ sở Dữ liệu',                       3, 'Thiết kế và quản lý CSDL quan hệ'),
    ('CS301', 'Cơ sở Dữ liệu Nâng cao',              3, 'SQL nâng cao, OQL, giao dịch'),
    ('CS202', 'Cấu trúc Dữ liệu và Giải thuật',      4, 'Các cấu trúc dữ liệu cổ điển'),
    ('CS401', 'Lập trình Hướng đối tượng',            3, 'OOP với Java'),
    ('CS501', 'Mạng Máy tính',                        3, 'Giao thức mạng và bảo mật'),
    ('MA101', 'Giải tích 1',                          4, 'Đạo hàm, tích phân'),
    ('MA201', 'Đại số Tuyến tính',                    3, 'Ma trận, không gian vectơ'),
    ('EN101', 'Tiếng Anh Cơ bản',                    3, 'TOEIC 400+'),
    ('CS601', 'Trí tuệ Nhân tạo',                    3, 'Machine learning cơ bản');

-- ---- LỚP HỌC PHẦN ----
INSERT INTO LopHocPhan VALUES
    ('LHP2401001', 'CS201', 1, '2024-2025', 2, 1, 2, 'GV001', 'P101', 50),
    ('LHP2401002', 'CS201', 1, '2024-2025', 4, 3, 2, 'GV001', 'P201', 60),
    ('LHP2401003', 'CS301', 1, '2024-2025', 3, 5, 2, 'GV003', 'P501', 40),
    ('LHP2401004', 'CS101', 1, '2024-2025', 2, 7, 2, 'GV002', 'P102', 45),
    ('LHP2401005', 'CS202', 1, '2024-2025', 5, 1, 3, 'GV006', 'P201', 55),
    ('LHP2401006', 'CS401', 1, '2024-2025', 6, 3, 2, 'GV008', 'P601', 40),
    ('LHP2401007', 'MA101', 1, '2024-2025', 4, 7, 2, 'GV007', 'P301', 80),
    ('LHP2401008', 'EN101', 1, '2024-2025', 3, 1, 2, 'GV005', 'P501', 50),
    ('LHP2401009', 'CS501', 2, '2024-2025', 2, 5, 2, 'GV006', 'P201', 45),
    ('LHP2401010', 'CS601', 2, '2024-2025', 5, 7, 2, 'GV003', 'P101', 35);

-- ---- SINH VIÊN ----
INSERT INTO SinhVien VALUES
    ('SV2001001', 'Nguyễn Thị Anh',   '2002-03-15', 'Nu',  'KHMT', 'anh.nt@student.edu.vn'),
    ('SV2001002', 'Trần Văn Bảo',     '2002-07-22', 'Nam', 'KHMT', 'bao.tv@student.edu.vn'),
    ('SV2001003', 'Lê Thị Cẩm',       '2001-11-05', 'Nu',  'HTTT', 'cam.lt@student.edu.vn'),
    ('SV2001004', 'Phạm Văn Dũng',    '2002-01-30', 'Nam', 'KTPM', 'dung.pv@student.edu.vn'),
    ('SV2001005', 'Hoàng Thị Ếm',     '2001-09-14', 'Nu',  'KTPM', 'em.ht@student.edu.vn'),
    ('SV2001006', 'Vũ Quốc Phong',    '2002-06-08', 'Nam', 'KHMT', 'phong.vq@student.edu.vn'),
    ('SV2001007', 'Đặng Thị Giang',   '2001-12-25', 'Nu',  'HTTT', 'giang.dt@student.edu.vn'),
    ('SV2001008', 'Bùi Văn Hải',      '2002-04-17', 'Nam', 'TMDT', 'hai.bv@student.edu.vn'),
    ('SV2001009', 'Ngô Thị Iris',     '2001-08-03', 'Nu',  'QTKD01','iris.nt@student.edu.vn'),
    ('SV2001010', 'Tô Văn Khoa',      '2002-02-19', 'Nam', 'KHMT', 'khoa.tv@student.edu.vn'),
    ('SV2001011', 'Lý Thị Lan',       '2001-10-11', 'Nu',  'KTPM', 'lan.lt@student.edu.vn'),
    ('SV2001012', 'Mã Văn Minh',      '2002-05-27', 'Nam', 'HTTT', 'minh.mv@student.edu.vn'),
    ('SV2001013', 'Nhan Thị Ngọc',    '2001-07-04', 'Nu',  'KHMT', 'ngoc.nt@student.edu.vn'),
    ('SV2001014', 'Ổn Văn Ổn',        '2002-09-13', 'Nam', 'DTVT01','on.ov@student.edu.vn'),
    ('SV2001015', 'Phan Thị Phụng',   '2001-03-28', 'Nu',  'TMDT', 'phung.pt@student.edu.vn');

-- ---- ĐĂNG KÝ + ĐIỂM ----
INSERT INTO DangKy (MaSV, MaLHP, NgayDangKy, Diem) VALUES
-- Sinh viên SV2001001
('SV2001001','LHP2401001','2024-08-20', 8.5),
('SV2001001','LHP2401007','2024-08-20', 7.0),
('SV2001001','LHP2401008','2024-08-20', 9.0),
-- Sinh viên SV2001002
('SV2001002','LHP2401001','2024-08-21', 6.5),
('SV2001002','LHP2401004','2024-08-21', 7.5),
('SV2001002','LHP2401007','2024-08-21', 5.0),
-- Sinh viên SV2001003
('SV2001003','LHP2401002','2024-08-20', 9.0),
('SV2001003','LHP2401003','2024-08-20', 8.0),
('SV2001003','LHP2401008','2024-08-20', 7.5),
-- Sinh viên SV2001004
('SV2001004','LHP2401004','2024-08-22', 4.5),
('SV2001004','LHP2401005','2024-08-22', 6.0),
('SV2001004','LHP2401006','2024-08-22', 7.0),
-- Sinh viên SV2001005
('SV2001005','LHP2401001','2024-08-19', 3.5),
('SV2001005','LHP2401003','2024-08-19', 5.5),
('SV2001005','LHP2401007','2024-08-19', 6.0),
-- Sinh viên SV2001006
('SV2001006','LHP2401001','2024-08-23', 9.5),
('SV2001006','LHP2401002','2024-08-23', 8.0),
('SV2001006','LHP2401005','2024-08-23', 9.0),
-- Sinh viên SV2001007
('SV2001007','LHP2401002','2024-08-20', 7.0),
('SV2001007','LHP2401006','2024-08-20', 6.5),
-- Sinh viên SV2001008
('SV2001008','LHP2401004','2024-08-21', 8.0),
('SV2001008','LHP2401008','2024-08-21', 7.0),
-- Sinh viên SV2001009
('SV2001009','LHP2401008','2024-08-22', 6.0),
('SV2001009','LHP2401009','2024-08-22', NULL), -- chưa thi
-- Sinh viên SV2001010
('SV2001010','LHP2401001','2024-08-20', 5.5),
('SV2001010','LHP2401003','2024-08-20', 7.0),
('SV2001010','LHP2401010','2024-08-20', NULL), -- chưa thi
-- Sinh viên SV2001011
('SV2001011','LHP2401005','2024-08-23', 8.5),
('SV2001011','LHP2401006','2024-08-23', 9.0),
-- Sinh viên SV2001012
('SV2001012','LHP2401002','2024-08-24', 6.0),
('SV2001012','LHP2401009','2024-08-24', NULL),
-- Sinh viên SV2001013
('SV2001013','LHP2401001','2024-08-25', 7.5),
('SV2001013','LHP2401007','2024-08-25', 8.0),
-- Sinh viên SV2001014
('SV2001014','LHP2401004','2024-08-20', 5.0),
('SV2001014','LHP2401008','2024-08-20', 4.0),
-- Sinh viên SV2001015
('SV2001015','LHP2401003','2024-08-26', 6.5),
('SV2001015','LHP2401010','2024-08-26', NULL);

SELECT 'Dữ liệu mẫu đã nạp: 5 khoa, 7 ngành, 8 GV, 7 phòng, 10 HP, 10 LHP, 15 SV, 36 đăng ký.' AS ket_qua;
