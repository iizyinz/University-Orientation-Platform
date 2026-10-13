package com.example.backend.service.impl;

import com.example.backend.entity.DormBuilding;
import com.example.backend.mapper.DormBuildingMapper;
import com.example.backend.service.DormBuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DormBuildingServiceImpl implements DormBuildingService {
    @Autowired private DormBuildingMapper mapper;

    public List<DormBuilding> findAll() { return mapper.findAll(); }
    public DormBuilding findById(Long id) { return mapper.findById(id); }
    public DormBuilding create(DormBuilding b) {
        if (mapper.countByCode(b.getBuildingCode()) > 0) throw new RuntimeException("楼栋编码已存在");
        mapper.insert(b); return b;
    }
    public DormBuilding update(DormBuilding b) {
        if (mapper.countByCodeExcludeId(b.getBuildingCode(), b.getId()) > 0) throw new RuntimeException("楼栋编码已存在");
        mapper.update(b); return b;
    }
    public void delete(Long id) { mapper.deleteById(id); }
}
