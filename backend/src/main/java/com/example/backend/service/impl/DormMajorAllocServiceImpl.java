package com.example.backend.service.impl;

import com.example.backend.entity.DormMajorAlloc;
import com.example.backend.mapper.DormMajorAllocMapper;
import com.example.backend.service.DormMajorAllocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DormMajorAllocServiceImpl implements DormMajorAllocService {
    @Autowired private DormMajorAllocMapper mapper;

    public List<DormMajorAlloc> findAll() { return mapper.findAll(); }
    public DormMajorAlloc findById(Long id) { return mapper.findById(id); }
    public DormMajorAlloc create(DormMajorAlloc a) {
        if (mapper.countByMajorRoomGender(a.getMajorId(), a.getRoomId(), a.getGender()) > 0)
            throw new RuntimeException("该专业此性别已分配此房间");
        mapper.insert(a); return a;
    }
    public DormMajorAlloc update(DormMajorAlloc a) {
        if (mapper.countByMajorRoomGenderExcludeId(a.getMajorId(), a.getRoomId(), a.getGender(), a.getId()) > 0)
            throw new RuntimeException("该专业此性别已分配此房间");
        mapper.update(a); return a;
    }
    public void delete(Long id) { mapper.deleteById(id); }
}
