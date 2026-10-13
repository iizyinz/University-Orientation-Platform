package com.example.backend.service.impl;

import com.example.backend.entity.FaqCategory;
import com.example.backend.mapper.FaqCategoryMapper;
import com.example.backend.service.FaqCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FaqCategoryServiceImpl implements FaqCategoryService {
    @Autowired private FaqCategoryMapper mapper;
    public List<FaqCategory> findAll() { return mapper.findAll(); }
    public FaqCategory findById(Long id) { return mapper.findById(id); }
    public FaqCategory create(FaqCategory c) { mapper.insert(c); return c; }
    public FaqCategory update(FaqCategory c) { mapper.update(c); return c; }
    public void delete(Long id) { mapper.deleteById(id); }
}
