package com.doan10.db_normalization_tool.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Response cho API tính bao đóng của tập thuộc tính X+
 */
public class ClosureResponse {
    private Set<String> inputSet;           // Tập X đầu vào
    private Set<String> closure;            // Bao đóng X+
    private boolean isSuperKey;             // X+ == U?
    private List<StepLog> steps = new ArrayList<>();

    public ClosureResponse() {}

    public Set<String> getInputSet() { return inputSet; }
    public void setInputSet(Set<String> inputSet) {
        this.inputSet = inputSet != null ? new TreeSet<>(inputSet) : new TreeSet<>();
    }

    public Set<String> getClosure() { return closure; }
    public void setClosure(Set<String> closure) {
        this.closure = closure != null ? new TreeSet<>(closure) : new TreeSet<>();
    }

    public boolean isIsSuperKey() { return isSuperKey; }
    public void setIsSuperKey(boolean isSuperKey) { this.isSuperKey = isSuperKey; }

    public List<StepLog> getSteps() { return steps; }
    public void setSteps(List<StepLog> steps) {
        this.steps = steps != null ? steps : new ArrayList<>();
    }
}
