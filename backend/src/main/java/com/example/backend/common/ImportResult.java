package com.example.backend.common;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResult {
    private int totalRows;
    private int successCount;
    private int failCount;
    private List<String> errors = new ArrayList<>();

    public void addError(String error) {
        errors.add(error);
        failCount++;
    }
}
