package com.example.backend.service;

import com.example.backend.entity.FeeItem;
import java.util.List;

public interface FeeItemService {
    List<FeeItem> findAll();
    FeeItem findById(Long id);
    FeeItem create(FeeItem item);
    FeeItem update(FeeItem item);
    void delete(Long id);
}
