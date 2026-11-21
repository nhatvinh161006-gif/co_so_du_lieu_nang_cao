package com.doan10.db_normalization_tool.dto;

import java.util.ArrayList;
import java.util.List;

public class MinimalCoverResponse {
    private List<PhuThuocHamDTO> originalDependencies = new ArrayList<>();
    private List<PhuThuocHamDTO> minimalCover = new ArrayList<>();
    private List<StepLog> steps = new ArrayList<>();

    public MinimalCoverResponse() {}

    public List<PhuThuocHamDTO> getOriginalDependencies() {
        return originalDependencies;
    }

    public void setOriginalDependencies(List<PhuThuocHamDTO> originalDependencies) {
        this.originalDependencies = originalDependencies != null ? originalDependencies : new ArrayList<>();
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
