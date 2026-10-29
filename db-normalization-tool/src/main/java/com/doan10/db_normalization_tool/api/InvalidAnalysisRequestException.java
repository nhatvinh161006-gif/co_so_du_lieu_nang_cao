package com.doan10.db_normalization_tool.api;

public class InvalidAnalysisRequestException extends RuntimeException {
    private final String field;

    public InvalidAnalysisRequestException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
