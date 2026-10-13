package com.example.backend.service;

import com.example.backend.entity.DormRoom;
import java.util.List;

public interface DormRoomService {
    List<DormRoom> findAll();
    List<DormRoom> findByBuildingId(Long buildingId);
    DormRoom findById(Long id);
    DormRoom create(DormRoom room);
    DormRoom update(DormRoom room);
    void delete(Long id);
}
