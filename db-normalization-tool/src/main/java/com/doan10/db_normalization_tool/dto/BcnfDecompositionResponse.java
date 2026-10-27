package com.doan10.db_normalization_tool.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Response cho API phân rã BCNF
 */
public class BcnfDecompositionResponse {
    private List<Set<String>> subSchemas = new ArrayList<>();   // Các lược đồ con sau phân rã
    private List<PhuThuocHamDTO> violatingFDs = new ArrayList<>(); // Các FD vi phạm BCNF
    private List<StepLog> steps = new ArrayList<>();

    public BcnfDecompositionResponse() {}

    public List<Set<String>> getSubSchemas() { return subSchemas; }
    public void setSubSchemas(List<Set<String>> subSchemas) {
        this.subSchemas = subSchemas != null ? subSchemas : new ArrayList<>();
    }

    public List<PhuThuocHamDTO> getViolatingFDs() { return violatingFDs; }
    public void setViolatingFDs(List<PhuThuocHamDTO> violatingFDs) {
        this.violatingFDs = violatingFDs != null ? violatingFDs : new ArrayList<>();
    }

    public List<StepLog> getSteps() { return steps; }
    public void setSteps(List<StepLog> steps) {
        this.steps = steps != null ? steps : new ArrayList<>();
    }
}
