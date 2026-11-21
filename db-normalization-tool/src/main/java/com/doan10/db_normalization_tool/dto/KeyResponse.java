package com.doan10.db_normalization_tool.dto;

import java.util.*;

public class KeyResponse {
    private Set<String> universe = new TreeSet<>();
    private List<Set<String>> keys = new ArrayList<>();
    private Set<String> primeAttributes = new TreeSet<>();
    private Set<String> nonPrimeAttributes = new TreeSet<>();
    private Map<String, Set<String>> attributeGroups = new LinkedHashMap<>();
    private List<StepLog> steps = new ArrayList<>();

    public KeyResponse() {}

    public Set<String> getUniverse() {
        return universe;
    }

    public void setUniverse(Set<String> universe) {
        this.universe = universe != null ? new TreeSet<>(universe) : new TreeSet<>();
    }

    public List<Set<String>> getKeys() {
        return keys;
    }

    public void setKeys(List<Set<String>> keys) {
        this.keys = keys != null ? keys : new ArrayList<>();
    }

    public Set<String> getPrimeAttributes() {
        return primeAttributes;
    }

    public void setPrimeAttributes(Set<String> primeAttributes) {
        this.primeAttributes = primeAttributes != null ? new TreeSet<>(primeAttributes) : new TreeSet<>();
    }

    public Set<String> getNonPrimeAttributes() {
        return nonPrimeAttributes;
    }

    public void setNonPrimeAttributes(Set<String> nonPrimeAttributes) {
        this.nonPrimeAttributes = nonPrimeAttributes != null ? new TreeSet<>(nonPrimeAttributes) : new TreeSet<>();
    }

    public Map<String, Set<String>> getAttributeGroups() {
        return attributeGroups;
    }

    public void setAttributeGroups(Map<String, Set<String>> attributeGroups) {
        this.attributeGroups = attributeGroups != null ? attributeGroups : new LinkedHashMap<>();
    }

    public List<StepLog> getSteps() {
        return steps;
    }

    public void setSteps(List<StepLog> steps) {
        this.steps = steps != null ? steps : new ArrayList<>();
    }
}
