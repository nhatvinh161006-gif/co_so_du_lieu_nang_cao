package com.doan10.db_normalization_tool.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DecompositionResponse {
    private List<Set<String>> subSchemas = new ArrayList<>();
    private List<PhuThuocHamDTO> minimalCover = new ArrayList<>();
    private List<StepLog> steps = new ArrayList<>();

    public DecompositionResponse() {}

    public List<Set<String>> getSubSchemas() {
        return subSchemas;
    }

    public void setSubSchemas(List<Set<String>> subSchemas) {
        this.subSchemas = subSchemas != null ? subSchemas : new ArrayList<>();
    }

    public List<PhuThuocHamDTO> getMinimalCover() {
        return minimalCover;
    }

    public void setMinimalCover(List<PhuThuocHamDTO> minimalCover) {
        this.minimalCover = minimalCover != null ? minimalCover : new ArrayList<>();
    }

    public List<StepLog> getSteps() {
        return steps;
    }

    public void setSteps(List<StepLog> steps) {
        this.steps = steps != null ? steps : new ArrayList<>();
    }
}
