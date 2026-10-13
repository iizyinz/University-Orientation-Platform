package com.example.backend.service;

import com.example.backend.entity.DormBuilding;
import java.util.List;

public interface DormBuildingService {
    List<DormBuilding> findAll();
    DormBuilding findById(Long id);
    DormBuilding create(DormBuilding building);
    DormBuilding update(DormBuilding building);
    void delete(Long id);
}
