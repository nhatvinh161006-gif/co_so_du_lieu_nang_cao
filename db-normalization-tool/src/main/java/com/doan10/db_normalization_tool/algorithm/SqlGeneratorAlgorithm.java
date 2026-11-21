package com.doan10.db_normalization_tool.algorithm;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SqlGeneratorAlgorithm {

    public String taoCauLenhSqlDdl(List<Set<String>> cacQuanHe, List<List<Set<String>>> khoaUngVienTheoQuanHe) {
        StringBuilder sql = new StringBuilder();
        sql.append("-- ==========================================================\n");
        sql.append("-- DDL GENERATED FROM NORMALIZATION DECOMPOSITION\n");
        sql.append("-- Sinh tự động từ công cụ chuẩn hóa CSDL (Đồ án 10)\n");
        sql.append("-- Hỗ trợ PostgreSQL / Oracle / SQL Server / MySQL\n");
        sql.append("-- ==========================================================\n\n");

        for (int i = 0; i < cacQuanHe.size(); i++) {
            Set<String> attributes = cacQuanHe.get(i);
            String tableName = "Bang_R" + (i + 1);

            List<Set<String>> keys = (khoaUngVienTheoQuanHe != null && i < khoaUngVienTheoQuanHe.size())
                    ? khoaUngVienTheoQuanHe.get(i)
                    : List.of();

            Set<String> primaryKey = !keys.isEmpty() ? keys.get(0) : Set.of();

            sql.append("CREATE TABLE ").append(tableName).append(" (\n");

            List<String> colDefs = new ArrayList<>();
            for (String attr : attributes) {
                String dataType = doanKieuDuLieu(attr);
                boolean isPkCol = primaryKey.contains(attr);
                colDefs.add("    " + String.format("%-20s", attr) + " " + dataType + (isPkCol ? " NOT NULL" : ""));
            }

            if (!primaryKey.isEmpty()) {
                colDefs.add("    CONSTRAINT PK_" + tableName + " PRIMARY KEY (" + String.join(", ", primaryKey) + ")");
            }

            // Các khóa ứng viên còn lại thêm UNIQUE constraint
            for (int k = 1; k < keys.size(); k++) {
                Set<String> altKey = keys.get(k);
                colDefs.add("    CONSTRAINT UQ_" + tableName + "_" + k + " UNIQUE (" + String.join(", ", altKey) + ")");
            }

            sql.append(String.join(",\n", colDefs));
            sql.append("\n);\n\n");
        }

        return sql.toString();
    }

    private String doanKieuDuLieu(String attribute) {
        String lower = attribute.toLowerCase();
        if (lower.startsWith("ma") || lower.endsWith("id") || lower.startsWith("id")) {
            return "VARCHAR(20)";
        } else if (lower.startsWith("ten") || lower.contains("name")) {
            return "VARCHAR(100)";
        } else if (lower.contains("ngay") || lower.contains("date") || lower.contains("time")) {
            return "DATE";
        } else if (lower.contains("diem") || lower.contains("score") || lower.contains("gpa")) {
            return "NUMERIC(4, 2)";
        } else if (lower.startsWith("so") || lower.contains("tiet") || lower.contains("succhua") || lower.contains("count")) {
            return "INT";
        } else if (lower.contains("gioitinh") || lower.contains("gender")) {
            return "VARCHAR(10)";
        }
        return "VARCHAR(50)";
    }
}
