package com.example.backend.service.impl;

import com.example.backend.entity.SysMajor;
import com.example.backend.mapper.SysMajorMapper;
import com.example.backend.service.SysMajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysMajorServiceImpl implements SysMajorService {

    @Autowired
    private SysMajorMapper sysMajorMapper;

    @Override
    public List<SysMajor> findAll() {
        return sysMajorMapper.findAll();
    }

    @Override
    public SysMajor findById(Long id) {
        return sysMajorMapper.findById(id);
    }

    @Override
    public List<SysMajor> findByCollegeId(Long collegeId) {
        return sysMajorMapper.findByCollegeId(collegeId);
    }

    @Override
    public SysMajor create(SysMajor major) {
        if (sysMajorMapper.countByCode(major.getMajorCode()) > 0) {
            throw new RuntimeException("专业编码已存在");
        }
        sysMajorMapper.insert(major);
        return major;
    }

    @Override
    public SysMajor update(SysMajor major) {
        if (sysMajorMapper.countByCodeExcludeId(major.getMajorCode(), major.getId()) > 0) {
            throw new RuntimeException("专业编码已存在");
        }
        sysMajorMapper.update(major);
        return major;
    }

    @Override
    public void delete(Long id) {
        sysMajorMapper.deleteById(id);
    }
}
