package com.example.backend.service;

import com.example.backend.entity.FaqCategory;
import java.util.List;

public interface FaqCategoryService {
    List<FaqCategory> findAll();
    FaqCategory findById(Long id);
    FaqCategory create(FaqCategory c);
    FaqCategory update(FaqCategory c);
    void delete(Long id);
}
