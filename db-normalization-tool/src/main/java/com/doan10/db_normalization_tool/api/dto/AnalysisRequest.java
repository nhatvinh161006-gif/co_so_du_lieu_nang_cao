package com.doan10.db_normalization_tool.api.dto;

import java.util.List;

public class AnalysisRequest {
    private List<String> universe;
    private List<FunctionalDependencyDto> dependencies;
    private List<String> x;
    private List<String> y;
    private Boolean atomicValues;

    public List<String> getUniverse() {
        return universe;
    }

    public void setUniverse(List<String> universe) {
        this.universe = universe;
    }

    public List<FunctionalDependencyDto> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<FunctionalDependencyDto> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getX() {
        return x;
    }

    public void setX(List<String> x) {
        this.x = x;
    }

    public List<String> getY() {
        return y;
    }

    public void setY(List<String> y) {
        this.y = y;
    }

    public Boolean getAtomicValues() {
        return atomicValues;
    }

    public void setAtomicValues(Boolean atomicValues) {
        this.atomicValues = atomicValues;
    }
}
