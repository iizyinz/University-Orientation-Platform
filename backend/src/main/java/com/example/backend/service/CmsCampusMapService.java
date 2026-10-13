package com.example.backend.service;

import com.example.backend.entity.CmsCampusMap;
import java.util.List;

public interface CmsCampusMapService {
    List<CmsCampusMap> findAll();
    CmsCampusMap findById(Long id);
    CmsCampusMap create(CmsCampusMap m);
    CmsCampusMap update(CmsCampusMap m);
    void delete(Long id);
}
