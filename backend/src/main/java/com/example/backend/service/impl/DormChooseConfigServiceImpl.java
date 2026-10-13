package com.example.backend.service.impl;

import com.example.backend.entity.DormChooseConfig;
import com.example.backend.mapper.DormChooseConfigMapper;
import com.example.backend.service.DormChooseConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DormChooseConfigServiceImpl implements DormChooseConfigService {
    @Autowired private DormChooseConfigMapper mapper;

    public List<DormChooseConfig> findAll() { return mapper.findAll(); }
    public DormChooseConfig findById(Long id) { return mapper.findById(id); }
    public DormChooseConfig create(DormChooseConfig c) { mapper.insert(c); return c; }
    public DormChooseConfig update(DormChooseConfig c) { mapper.update(c); return c; }
    public void delete(Long id) { mapper.deleteById(id); }
}
