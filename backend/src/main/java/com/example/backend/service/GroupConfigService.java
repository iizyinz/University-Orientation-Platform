package com.example.backend.service;

import com.example.backend.entity.GroupConfig;
import java.util.List;

public interface GroupConfigService {
    List<GroupConfig> findAll();
    GroupConfig findById(Long id);
    List<GroupConfig> findByCounselorId(Long counselorId);
    GroupConfig create(GroupConfig g);
    GroupConfig update(GroupConfig g);
    void delete(Long id);
}
