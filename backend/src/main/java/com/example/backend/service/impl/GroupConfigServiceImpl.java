package com.example.backend.service.impl;

import com.example.backend.entity.GroupConfig;
import com.example.backend.mapper.GroupConfigMapper;
import com.example.backend.service.GroupConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GroupConfigServiceImpl implements GroupConfigService {
    @Autowired private GroupConfigMapper mapper;
    public List<GroupConfig> findAll() { return mapper.findAll(); }
    public GroupConfig findById(Long id) { return mapper.findById(id); }
    public List<GroupConfig> findByCounselorId(Long counselorId) { return mapper.findByCounselorId(counselorId); }
    public GroupConfig create(GroupConfig g) { mapper.insert(g); return g; }
    public GroupConfig update(GroupConfig g) { mapper.update(g); return g; }
    public void delete(Long id) { mapper.deleteById(id); }
}
