package com.example.backend.service;

import com.example.backend.entity.DormChooseConfig;
import java.util.List;

public interface DormChooseConfigService {
    List<DormChooseConfig> findAll();
    DormChooseConfig findById(Long id);
    DormChooseConfig create(DormChooseConfig config);
    DormChooseConfig update(DormChooseConfig config);
    void delete(Long id);
}
