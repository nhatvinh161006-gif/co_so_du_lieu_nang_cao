package com.doan10.db_normalization_tool.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Request body cho API tính bao đóng /api/algorithms/closure
 *
 * Ví dụ JSON:
 * {
 *   "universe": ["A", "B", "C", "D", "E"],
 *   "inputSet": ["A", "C"],
 *   "dependencies": [
 *     {"left": ["A"], "right": ["B"]},
 *     {"left": ["C", "D"], "right": ["E"]}
 *   ]
 * }
 */
public class ClosureRequest {

    @JsonProperty("universe")
    @JsonAlias({"u", "U", "attributes"})
    private Set<String> universe = new HashSet<>();

    @JsonProperty("inputSet")
    @JsonAlias({"x", "X", "input", "tapX"})
    private Set<String> inputSet = new HashSet<>();

    @JsonProperty("dependencies")
    @JsonAlias({"f", "F", "fds"})
    private List<PhuThuocHamDTO> dependencies = new ArrayList<>();

    public ClosureRequest() {}

    public Set<String> getUniverse() { return universe; }
    public void setUniverse(Set<String> universe) {
        this.universe = universe != null ? universe : new HashSet<>();
    }

    public Set<String> getInputSet() { return inputSet; }
    public void setInputSet(Set<String> inputSet) {
        this.inputSet = inputSet != null ? inputSet : new HashSet<>();
    }

    public List<PhuThuocHamDTO> getDependencies() { return dependencies; }
    public void setDependencies(List<PhuThuocHamDTO> dependencies) {
        this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
    }
}
