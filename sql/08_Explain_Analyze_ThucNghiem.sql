-- ============================================================
--  FILE 08: THỰC NGHIỆM ĐO HIỆU NĂNG (EXPLAIN ANALYZE)
--  Nhóm N10 - Quản lý Đào tạo Đại học
--  Mục tiêu: Phân biệt kết quả đo thực tế (Seq Scan vs Index Scan)
--  với phân tích lý thuyết O(N) vs O(log N)
-- ============================================================
SET search_path = qldt;

-- Cập nhật thống kê bảng cho Query Planner
ANALYZE SinhVien;
ANALYZE LopHocPhan;
ANALYZE DangKy;

-- ============================================================
-- THÍ NGHIỆM 1: Partial Index idx_dangky_diem (WHERE Diem IS NOT NULL)
-- ============================================================
-- 1a. Tắt Index Scan để quan sát Sequential Scan
SET enable_indexscan = off;
SET enable_bitmapscan = off;
EXPLAIN (ANALYZE, BUFFERS)
SELECT MaSV, MaLHP, Diem
FROM DangKy
WHERE Diem >= 8.0;

-- 1b. Bật lại Index Scan để so sánh kế hoạch thực thi
SET enable_indexscan = on;
SET enable_bitmapscan = on;
EXPLAIN (ANALYZE, BUFFERS)
SELECT MaSV, MaLHP, Diem
FROM DangKy
WHERE Diem >= 8.0;

-- ============================================================
-- THÍ NGHIỆM 2: Composite Index idx_lhp_hocky_namhoc(HocKy, NamHoc)
-- ============================================================
EXPLAIN (ANALYZE, BUFFERS)
SELECT MaLHP, MaHP, MaGV, MaPhong, Thu, TietBD
FROM LopHocPhan
WHERE HocKy = 1 AND NamHoc = '2024-2025';

-- ============================================================
-- THÍ NGHIỆM 3: JOIN 3 bảng SinhVien - DangKy - LopHocPhan
-- ============================================================
EXPLAIN (ANALYZE, BUFFERS)
SELECT sv.MaSV, sv.TenSV, lhp.MaLHP, dk.Diem
FROM SinhVien sv
JOIN DangKy dk ON sv.MaSV = dk.MaSV
JOIN LopHocPhan lhp ON dk.MaLHP = lhp.MaLHP
WHERE lhp.HocKy = 1 AND dk.Diem IS NOT NULL;

-- Lưu ý phân tích thực nghiệm:
-- Trên tập dữ liệu nhỏ (15 SV, 37 ĐK), PostgreSQL Planner có thể vẫn chọn
-- Seq Scan dù đã có Index vì chi phí đọc 1 page dữ liệu thấp hơn chi phí
-- đi qua cây B-Tree. Khi dữ liệu lớn (>= hàng nghìn dòng), Index Scan
-- sẽ thể hiện rõ lợi thế O(log N) so với O(N).
SELECT 'Hoàn tất script đo EXPLAIN (ANALYZE, BUFFERS).' AS ket_qua;
