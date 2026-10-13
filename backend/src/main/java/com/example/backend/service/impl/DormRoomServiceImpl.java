package com.example.backend.service.impl;

import com.example.backend.entity.DormRoom;
import com.example.backend.mapper.DormRoomMapper;
import com.example.backend.service.DormRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DormRoomServiceImpl implements DormRoomService {
    @Autowired private DormRoomMapper mapper;

    public List<DormRoom> findAll() { return mapper.findAll(); }
    public List<DormRoom> findByBuildingId(Long buildingId) { return mapper.findByBuildingId(buildingId); }
    public DormRoom findById(Long id) { return mapper.findById(id); }
    public DormRoom create(DormRoom r) {
        if (mapper.countByUnique(r.getBuildingId(), r.getFloorNo(), r.getRoomNo()) > 0)
            throw new RuntimeException("该楼栋已存在相同楼层房间号");
        mapper.insert(r); return r;
    }
    public DormRoom update(DormRoom r) {
        if (mapper.countByUniqueExcludeId(r.getBuildingId(), r.getFloorNo(), r.getRoomNo(), r.getId()) > 0)
            throw new RuntimeException("该楼栋已存在相同楼层房间号");
        mapper.update(r); return r;
    }
    public void delete(Long id) { mapper.deleteById(id); }
}
