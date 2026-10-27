package com.doan10.db_normalization_tool.dto;

import com.doan10.db_normalization_tool.model.PhuThuocHam;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class PhuThuocHamDTO {
    @JsonProperty("left")
    @JsonAlias({"vt", "vT", "VT"})
    private Set<String> left = new HashSet<>();

    @JsonProperty("right")
    @JsonAlias({"vp", "vP", "VP"})
    private Set<String> right = new HashSet<>();

    public PhuThuocHamDTO() {}

    public PhuThuocHamDTO(Set<String> left, Set<String> right) {
        this.left = left != null ? left : new HashSet<>();
        this.right = right != null ? right : new HashSet<>();
    }

    public PhuThuocHamDTO(PhuThuocHam fd) {
        if (fd != null) {
            this.left = fd.getVT() != null ? new HashSet<>(fd.getVT()) : new HashSet<>();
            this.right = fd.getVP() != null ? new HashSet<>(fd.getVP()) : new HashSet<>();
        }
    }

    public PhuThuocHam toModel() {
        return new PhuThuocHam(new HashSet<>(this.left), new HashSet<>(this.right));
    }

    public Set<String> getLeft() {
        return left;
    }

    public void setLeft(Set<String> left) {
        this.left = left != null ? left : new HashSet<>();
    }

    public Set<String> getRight() {
        return right;
    }

    public void setRight(Set<String> right) {
        this.right = right != null ? right : new HashSet<>();
    }

    @Override
    public String toString() {
        return "{" + String.join(", ", new TreeSet<>(left)) + "} -> {" + String.join(", ", new TreeSet<>(right)) + "}";
    }
}
