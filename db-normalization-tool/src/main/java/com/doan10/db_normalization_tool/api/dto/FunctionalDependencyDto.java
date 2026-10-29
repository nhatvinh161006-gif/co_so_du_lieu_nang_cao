package com.doan10.db_normalization_tool.api.dto;

import java.util.List;

public record FunctionalDependencyDto(List<String> left, List<String> right) {}
