package com.example.backend.service;

import com.example.backend.entity.DormMajorAlloc;
import java.util.List;

public interface DormMajorAllocService {
    List<DormMajorAlloc> findAll();
    DormMajorAlloc findById(Long id);
    DormMajorAlloc create(DormMajorAlloc alloc);
    DormMajorAlloc update(DormMajorAlloc alloc);
    void delete(Long id);
}
