package com.example.backend.service.impl;

import com.example.backend.entity.CmsHandbook;
import com.example.backend.mapper.CmsHandbookMapper;
import com.example.backend.service.CmsHandbookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CmsHandbookServiceImpl implements CmsHandbookService {
    @Autowired private CmsHandbookMapper mapper;
    public List<CmsHandbook> findAll() { return mapper.findAll(); }
    public CmsHandbook findById(Long id) { return mapper.findById(id); }
    public CmsHandbook create(CmsHandbook h) { mapper.insert(h); return h; }
    public CmsHandbook update(CmsHandbook h) { mapper.update(h); return h; }
    public void delete(Long id) { mapper.deleteById(id); }
}
