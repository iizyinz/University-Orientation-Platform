package com.example.backend.service;

import com.example.backend.entity.SysVolunteer;
import java.util.List;

public interface SysVolunteerService {
    List<SysVolunteer> findAll();
    SysVolunteer findById(Long id);
    List<SysVolunteer> findByType(Integer type);
    SysVolunteer create(SysVolunteer volunteer);
    SysVolunteer update(SysVolunteer volunteer);
    void delete(Long id);
}
