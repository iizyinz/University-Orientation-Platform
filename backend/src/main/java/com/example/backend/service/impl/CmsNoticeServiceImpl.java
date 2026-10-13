package com.example.backend.service.impl;

import com.example.backend.entity.CmsNotice;
import com.example.backend.mapper.CmsNoticeMapper;
import com.example.backend.service.CmsNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CmsNoticeServiceImpl implements CmsNoticeService {
    @Autowired private CmsNoticeMapper mapper;
    public List<CmsNotice> findAll() { return mapper.findAll(); }
    public CmsNotice findById(Long id) { return mapper.findById(id); }
    public CmsNotice create(CmsNotice n) { mapper.insert(n); return n; }
    public CmsNotice update(CmsNotice n) { mapper.update(n); return n; }
    public void delete(Long id) { mapper.deleteById(id); }
}
