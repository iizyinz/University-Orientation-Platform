package com.example.backend.service;

import com.example.backend.entity.SysMajor;
import java.util.List;

public interface SysMajorService {
    List<SysMajor> findAll();
    SysMajor findById(Long id);
    List<SysMajor> findByCollegeId(Long collegeId);
    SysMajor create(SysMajor major);
    SysMajor update(SysMajor major);
    void delete(Long id);
}
