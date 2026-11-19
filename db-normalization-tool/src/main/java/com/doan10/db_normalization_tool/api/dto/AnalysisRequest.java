package com.doan10.db_normalization_tool.api.dto;

import java.util.List;

public class AnalysisRequest {
    private List<String> universe;
    private List<FunctionalDependencyDto> dependencies;
    private List<FunctionalDependencyDto> dependencies2;
    private List<List<String>> relations;
    private List<String> x;
    private List<String> y;
    private Boolean atomicValues;
    private Integer limit;
    private String axiomType;
    private List<String> z;
    private List<String> w;

    public String getAxiomType() {
        return axiomType;
    }

    public void setAxiomType(String axiomType) {
        this.axiomType = axiomType;
    }

    public List<String> getZ() {
        return z;
    }

    public void setZ(List<String> z) {
        this.z = z;
    }

    public List<String> getW() {
        return w;
    }

    public void setW(List<String> w) {
        this.w = w;
    }

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

    public List<FunctionalDependencyDto> getDependencies2() {
        return dependencies2;
    }

    public void setDependencies2(List<FunctionalDependencyDto> dependencies2) {
        this.dependencies2 = dependencies2;
    }

    public List<List<String>> getRelations() {
        return relations;
    }

    public void setRelations(List<List<String>> relations) {
        this.relations = relations;
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

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
