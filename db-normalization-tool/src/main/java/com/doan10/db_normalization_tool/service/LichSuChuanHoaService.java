package com.doan10.db_normalization_tool.service;

import com.doan10.db_normalization_tool.dto.LichSuRequest;
import com.doan10.db_normalization_tool.entity.LichSuChuanHoa;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service xử lý lưu trữ và truy vấn Lịch sử Chuẩn hóa trong ObjectDB (Task 4)
 * Cung cấp:
 * 1. Hàm Java trực tiếp cho Người 2 (Backend thuật toán) gọi sau khi chạy xong thuật toán
 * 2. Tầng nghiệp vụ xử lý cho REST Controller của Người 3 (Frontend Web)
 */
@Service
public class LichSuChuanHoaService {

    public static final String DB_PATH = "db/daotao.odb";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private EntityManagerFactory emf;

    public LichSuChuanHoaService() {
        // Hỗ trợ khởi tạo ngoài Spring container (CLI / Test)
    }

    public LichSuChuanHoaService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @PostConstruct
    public void init() {
        if (this.emf == null || !this.emf.isOpen()) {
            this.emf = Persistence.createEntityManagerFactory(DB_PATH);
        }
    }

    @PreDestroy
    public void destroy() {
        if (this.emf != null && this.emf.isOpen()) {
            this.emf.close();
        }
    }

    public synchronized EntityManagerFactory getEntityManagerFactory() {
        if (this.emf == null || !this.emf.isOpen()) {
            this.emf = Persistence.createEntityManagerFactory(DB_PATH);
        }
        return this.emf;
    }

    /**
     * Lưu lịch sử mới từ DTO Request
     */
    public LichSuChuanHoa luuLichSu(LichSuRequest req) {
        String thoiGian = LocalDateTime.now().format(FORMATTER);
        return luuLichSu(
                req.getTapThuocTinhU(),
                req.getTapPhuThuocHamF(),
                req.getLoaiThuatToan(),
                req.getDauVaoBoSung(),
                req.getKetQuaChiTiet(),
                req.getThoiGianThucThiMs(),
                req.isThanhCong()
        );
    }

    /**
     * Lưu lịch sử mới với đầy đủ tham số (Người 2 có thể gọi hàm này trực tiếp)
     */
    public LichSuChuanHoa luuLichSu(String tapThuocTinhU, String tapPhuThuocHamF,
                                   String loaiThuatToan, String dauVaoBoSung,
                                   String ketQuaChiTiet, long thoiGianThucThiMs,
                                   boolean thanhCong) {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            String thoiGian = LocalDateTime.now().format(FORMATTER);
            LichSuChuanHoa item = new LichSuChuanHoa(
                    thoiGian,
                    tapThuocTinhU,
                    tapPhuThuocHamF,
                    loaiThuatToan,
                    dauVaoBoSung,
                    ketQuaChiTiet,
                    thoiGianThucThiMs,
                    thanhCong
            );

            em.persist(item);
            em.getTransaction().commit();
            return item;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Lỗi lưu lịch sử vào ObjectDB: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Lấy toàn bộ danh sách lịch sử chuẩn hóa (mới nhất lên đầu)
     */
    public List<LichSuChuanHoa> layTatCaLichSu() {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<LichSuChuanHoa> query = em.createQuery(
                    "SELECT h FROM LichSuChuanHoa h ORDER BY h.id DESC", LichSuChuanHoa.class);
            return new ArrayList<>(query.getResultList());
        } finally {
            em.close();
        }
    }

    /**
     * Lấy chi tiết một lượt chạy lịch sử theo OID
     */
    public LichSuChuanHoa layChiTietLichSu(Long id) {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        try {
            return em.find(LichSuChuanHoa.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Xóa 1 bản ghi lịch sử theo OID
     */
    public boolean xoaLichSu(Long id) {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            LichSuChuanHoa item = em.find(LichSuChuanHoa.class, id);
            if (item != null) {
                em.remove(item);
                em.getTransaction().commit();
                return true;
            }
            em.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Lỗi xóa lịch sử OID " + id + ": " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    /**
     * Xóa toàn bộ lịch sử (tiện lợi khi reset dữ liệu thử nghiệm)
     */
    public int xoaTatCaLichSu() {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            int count = em.createQuery("DELETE FROM LichSuChuanHoa").executeUpdate();
            em.getTransaction().commit();
            return count;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Lỗi xóa tất cả lịch sử: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
