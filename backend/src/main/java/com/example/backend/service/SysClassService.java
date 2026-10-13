package com.example.backend.service;

import com.example.backend.entity.SysClass;
import java.util.List;

public interface SysClassService {
    List<SysClass> findAll();
    SysClass findById(Long id);
    List<SysClass> findByMajorId(Long majorId);
    SysClass create(SysClass sysClass);
    SysClass update(SysClass sysClass);
    void delete(Long id);
}
