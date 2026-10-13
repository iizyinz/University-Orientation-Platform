package com.example.backend.service.impl;

import com.example.backend.entity.SysCounselor;
import com.example.backend.entity.SysUser;
import com.example.backend.mapper.SysCounselorMapper;
import com.example.backend.mapper.SysUserMapper;
import com.example.backend.service.SysCounselorService;
import com.example.backend.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysCounselorServiceImpl implements SysCounselorService {

    @Autowired
    private SysCounselorMapper counselorMapper;
    @Autowired
    private SysUserMapper userMapper;

    @Override
    public List<SysCounselor> findAll() {
        return counselorMapper.findAll();
    }

    @Override
    public SysCounselor findById(Long id) {
        return counselorMapper.findById(id);
    }

    @Override
    public List<SysCounselor> findByCollegeId(Long collegeId) {
        return counselorMapper.findByCollegeId(collegeId);
    }

    @Override
    @Transactional
    public SysCounselor create(SysCounselor counselor, List<Long> classIds) {
        SysUser user = new SysUser();
        user.setUsername(counselor.getWorkNo());
        user.setPassword(PasswordUtil.encode("123456"));
        user.setRealName(counselor.getRealName());
        user.setUserType(2);
        user.setPhone(counselor.getPhone());
        user.setEmail(counselor.getEmail());
        user.setStatus(1);
        userMapper.insertUser(user);

        counselor.setUserId(user.getId());
        counselorMapper.insert(counselor);

        if (classIds != null && !classIds.isEmpty()) {
            counselorMapper.insertClasses(counselor.getId(), classIds);
        }
        return counselor;
    }

    @Override
    @Transactional
    public SysCounselor update(SysCounselor counselor, List<Long> classIds) {
        counselorMapper.update(counselor);

        if (classIds != null) {
            counselorMapper.deleteClassesByCounselorId(counselor.getId());
            if (!classIds.isEmpty()) {
                counselorMapper.insertClasses(counselor.getId(), classIds);
            }
        }
        return counselor;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysCounselor counselor = counselorMapper.findById(id);
        if (counselor != null) {
            counselorMapper.deleteClassesByCounselorId(id);
            counselorMapper.deleteById(id);
            userMapper.deleteById(counselor.getUserId());
        }
    }
}
