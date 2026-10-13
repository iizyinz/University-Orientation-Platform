package com.example.backend.service;

import com.example.backend.entity.SysCollege;
import java.util.List;

public interface SysCollegeService {
    List<SysCollege> findAll();
    SysCollege findById(Long id);
    SysCollege create(SysCollege college);
    SysCollege update(SysCollege college);
    void delete(Long id);
}
