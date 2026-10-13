package com.example.backend.service.impl;

import com.example.backend.entity.SysClass;
import com.example.backend.mapper.SysClassMapper;
import com.example.backend.service.SysClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysClassServiceImpl implements SysClassService {

    @Autowired
    private SysClassMapper sysClassMapper;

    @Override
    public List<SysClass> findAll() {
        return sysClassMapper.findAll();
    }

    @Override
    public SysClass findById(Long id) {
        return sysClassMapper.findById(id);
    }

    @Override
    public List<SysClass> findByMajorId(Long majorId) {
        return sysClassMapper.findByMajorId(majorId);
    }

    @Override
    public SysClass create(SysClass sysClass) {
        if (sysClassMapper.countByCode(sysClass.getClassCode()) > 0) {
            throw new RuntimeException("班级编码已存在");
        }
        sysClassMapper.insert(sysClass);
        return sysClass;
    }

    @Override
    public SysClass update(SysClass sysClass) {
        if (sysClassMapper.countByCodeExcludeId(sysClass.getClassCode(), sysClass.getId()) > 0) {
            throw new RuntimeException("班级编码已存在");
        }
        sysClassMapper.update(sysClass);
        return sysClass;
    }

    @Override
    public void delete(Long id) {
        sysClassMapper.deleteById(id);
    }
}
