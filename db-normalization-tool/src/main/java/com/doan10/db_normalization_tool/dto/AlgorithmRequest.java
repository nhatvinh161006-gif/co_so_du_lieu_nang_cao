package com.doan10.db_normalization_tool.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlgorithmRequest {
    @JsonProperty("universe")
    @JsonAlias({"u", "U", "attributes"})
    private Set<String> universe = new HashSet<>();

    @JsonProperty("dependencies")
    @JsonAlias({"f", "F", "fds"})
    private List<PhuThuocHamDTO> dependencies = new ArrayList<>();

    public AlgorithmRequest() {}

    public AlgorithmRequest(Set<String> universe, List<PhuThuocHamDTO> dependencies) {
        this.universe = universe != null ? universe : new HashSet<>();
        this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
    }

    public Set<String> getUniverse() {
        return universe;
    }

    public void setUniverse(Set<String> universe) {
        this.universe = universe != null ? universe : new HashSet<>();
    }

    public List<PhuThuocHamDTO> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<PhuThuocHamDTO> dependencies) {
        this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
    }
}
