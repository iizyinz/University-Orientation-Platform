package com.example.backend.service.impl;

import com.example.backend.entity.FaqKnowledge;
import com.example.backend.mapper.FaqKnowledgeMapper;
import com.example.backend.service.FaqKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FaqKnowledgeServiceImpl implements FaqKnowledgeService {
    @Autowired private FaqKnowledgeMapper mapper;
    public List<FaqKnowledge> findAll() { return mapper.findAll(); }
    public List<FaqKnowledge> findByCategoryId(Long categoryId) { return mapper.findByCategoryId(categoryId); }
    public FaqKnowledge findById(Long id) { return mapper.findById(id); }
    public FaqKnowledge create(FaqKnowledge k) { mapper.insert(k); return k; }
    public FaqKnowledge update(FaqKnowledge k) { mapper.update(k); return k; }
    public void delete(Long id) { mapper.deleteById(id); }
}
