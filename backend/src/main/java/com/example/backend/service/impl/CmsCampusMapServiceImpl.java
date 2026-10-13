package com.example.backend.service.impl;

import com.example.backend.entity.CmsCampusMap;
import com.example.backend.mapper.CmsCampusMapMapper;
import com.example.backend.service.CmsCampusMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CmsCampusMapServiceImpl implements CmsCampusMapService {
    @Autowired private CmsCampusMapMapper mapper;
    public List<CmsCampusMap> findAll() { return mapper.findAll(); }
    public CmsCampusMap findById(Long id) { return mapper.findById(id); }
    public CmsCampusMap create(CmsCampusMap m) { mapper.insert(m); return m; }
    public CmsCampusMap update(CmsCampusMap m) { mapper.update(m); return m; }
    public void delete(Long id) { mapper.deleteById(id); }
}
