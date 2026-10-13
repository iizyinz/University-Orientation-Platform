package com.example.backend.service;

import com.example.backend.entity.StuInfo;
import java.util.List;
import java.util.Map;

public interface StuInfoService {
    List<StuInfo> findAll();
    StuInfo findById(Long id);
    StuInfo create(StuInfo stuInfo);
    StuInfo update(StuInfo stuInfo);
    void delete(Long id);
    Map<String, Object> getStats();
}
