package com.example.backend.service.impl;

import com.example.backend.entity.SysCollege;
import com.example.backend.mapper.SysCollegeMapper;
import com.example.backend.service.SysCollegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysCollegeServiceImpl implements SysCollegeService {

    @Autowired
    private SysCollegeMapper sysCollegeMapper;

    @Override
    public List<SysCollege> findAll() {
        return sysCollegeMapper.findAll();
    }

    @Override
    public SysCollege findById(Long id) {
        return sysCollegeMapper.findById(id);
    }

    @Override
    public SysCollege create(SysCollege college) {
        if (sysCollegeMapper.countByCode(college.getCollegeCode()) > 0) {
            throw new RuntimeException("学院编码已存在");
        }
        sysCollegeMapper.insert(college);
        return college;
    }

    @Override
    public SysCollege update(SysCollege college) {
        if (sysCollegeMapper.countByCodeExcludeId(college.getCollegeCode(), college.getId()) > 0) {
            throw new RuntimeException("学院编码已存在");
        }
        sysCollegeMapper.update(college);
        return college;
    }

    @Override
    public void delete(Long id) {
        sysCollegeMapper.deleteById(id);
    }
}
