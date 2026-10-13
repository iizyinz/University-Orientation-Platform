package com.example.backend.service;

import com.example.backend.entity.FaqKnowledge;
import java.util.List;

public interface FaqKnowledgeService {
    List<FaqKnowledge> findAll();
    List<FaqKnowledge> findByCategoryId(Long categoryId);
    FaqKnowledge findById(Long id);
    FaqKnowledge create(FaqKnowledge k);
    FaqKnowledge update(FaqKnowledge k);
    void delete(Long id);
}
