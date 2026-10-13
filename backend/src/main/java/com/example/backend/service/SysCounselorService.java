package com.example.backend.service;

import com.example.backend.entity.SysCounselor;
import java.util.List;

public interface SysCounselorService {
    List<SysCounselor> findAll();
    SysCounselor findById(Long id);
    List<SysCounselor> findByCollegeId(Long collegeId);
    SysCounselor create(SysCounselor counselor, List<Long> classIds);
    SysCounselor update(SysCounselor counselor, List<Long> classIds);
    void delete(Long id);
}
