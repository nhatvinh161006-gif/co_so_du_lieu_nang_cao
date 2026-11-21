package com.doan10.db_normalization_tool.dto;

import java.util.ArrayList;
import java.util.List;

public class NormalFormResponse {
    private String highestNormalForm;
    private boolean is2NF;
    private boolean is3NF;
    private boolean isBCNF;
    private List<String> violations2NF = new ArrayList<>();
    private List<String> violations3NF = new ArrayList<>();
    private List<String> violationsBCNF = new ArrayList<>();
    private List<StepLog> steps = new ArrayList<>();

    public NormalFormResponse() {}

    public String getHighestNormalForm() {
        return highestNormalForm;
    }

    public void setHighestNormalForm(String highestNormalForm) {
        this.highestNormalForm = highestNormalForm;
    }

    public boolean isIs2NF() {
        return is2NF;
    }

    public void setIs2NF(boolean is2NF) {
        this.is2NF = is2NF;
    }

    public boolean isIs3NF() {
        return is3NF;
    }

    public void setIs3NF(boolean is3NF) {
        this.is3NF = is3NF;
    }

    public boolean isIsBCNF() {
        return isBCNF;
    }

    public void setIsBCNF(boolean isBCNF) {
        this.isBCNF = isBCNF;
    }

    public List<String> getViolations2NF() {
        return violations2NF;
    }

    public void setViolations2NF(List<String> violations2NF) {
        this.violations2NF = violations2NF != null ? violations2NF : new ArrayList<>();
    }

    public List<String> getViolations3NF() {
        return violations3NF;
    }

    public void setViolations3NF(List<String> violations3NF) {
        this.violations3NF = violations3NF != null ? violations3NF : new ArrayList<>();
    }

    public List<String> getViolationsBCNF() {
        return violationsBCNF;
    }

    public void setViolationsBCNF(List<String> violationsBCNF) {
        this.violationsBCNF = violationsBCNF != null ? violationsBCNF : new ArrayList<>();
    }

    public List<StepLog> getSteps() {
        return steps;
    }

    public void setSteps(List<StepLog> steps) {
        this.steps = steps != null ? steps : new ArrayList<>();
    }
}
