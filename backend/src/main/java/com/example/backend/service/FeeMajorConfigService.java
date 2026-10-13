package com.example.backend.service;

import com.example.backend.entity.FeeMajorConfig;
import java.util.List;

public interface FeeMajorConfigService {
    List<FeeMajorConfig> findAll();
    List<FeeMajorConfig> findByMajorId(Long majorId);
    FeeMajorConfig findById(Long id);
    FeeMajorConfig create(FeeMajorConfig config);
    FeeMajorConfig update(FeeMajorConfig config);
    void delete(Long id);
}
