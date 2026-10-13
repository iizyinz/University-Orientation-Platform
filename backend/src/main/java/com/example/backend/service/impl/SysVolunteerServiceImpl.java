package com.example.backend.service.impl;

import com.example.backend.entity.SysVolunteer;
import com.example.backend.entity.SysUser;
import com.example.backend.mapper.SysUserMapper;
import com.example.backend.mapper.SysVolunteerMapper;
import com.example.backend.service.SysVolunteerService;
import com.example.backend.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysVolunteerServiceImpl implements SysVolunteerService {

    @Autowired
    private SysVolunteerMapper volunteerMapper;
    @Autowired
    private SysUserMapper userMapper;

    @Override
    public List<SysVolunteer> findAll() {
        return volunteerMapper.findAll();
    }

    @Override
    public SysVolunteer findById(Long id) {
        return volunteerMapper.findById(id);
    }

    @Override
    public List<SysVolunteer> findByType(Integer type) {
        return volunteerMapper.findByType(type);
    }

    @Override
    @Transactional
    public SysVolunteer create(SysVolunteer volunteer) {
        SysUser user = new SysUser();
        user.setUsername(volunteer.getPhone());
        user.setPassword(PasswordUtil.encode("123456"));
        user.setRealName(volunteer.getRealName());
        user.setUserType(volunteer.getVolunteerType() == 1 ? 4 : 5);
        user.setPhone(volunteer.getPhone());
        user.setStatus(1);
        userMapper.insertUser(user);

        volunteer.setUserId(user.getId());
        volunteerMapper.insert(volunteer);
        return volunteer;
    }

    @Override
    @Transactional
    public SysVolunteer update(SysVolunteer volunteer) {
        volunteerMapper.update(volunteer);
        return volunteer;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysVolunteer volunteer = volunteerMapper.findById(id);
        if (volunteer != null) {
            volunteerMapper.deleteById(id);
            userMapper.deleteById(volunteer.getUserId());
        }
    }
}