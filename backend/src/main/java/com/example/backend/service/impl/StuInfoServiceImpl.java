package com.example.backend.service.impl;

import com.example.backend.entity.StuInfo;
import com.example.backend.entity.SysUser;
import com.example.backend.mapper.StuInfoMapper;
import com.example.backend.mapper.SysUserMapper;
import com.example.backend.service.StuInfoService;
import com.example.backend.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StuInfoServiceImpl implements StuInfoService {

    @Autowired
    private StuInfoMapper stuInfoMapper;
    @Autowired
    private SysUserMapper userMapper;

    @Override
    public List<StuInfo> findAll() {
        return stuInfoMapper.findAll();
    }

    @Override
    public StuInfo findById(Long id) {
        return stuInfoMapper.findById(id);
    }

    @Override
    @Transactional
    public StuInfo create(StuInfo stuInfo) {
        if (stuInfoMapper.countByIdCard(stuInfo.getIdCard()) > 0) {
            throw new RuntimeException("身份证号已存在：" + stuInfo.getIdCard());
        }
        if (stuInfo.getStudentNo() != null && stuInfoMapper.countByStudentNo(stuInfo.getStudentNo()) > 0) {
            throw new RuntimeException("学号已存在：" + stuInfo.getStudentNo());
        }

        SysUser user = new SysUser();
        user.setUsername(stuInfo.getIdCard());
        String rawPassword = stuInfo.getIdCard().substring(Math.max(0, stuInfo.getIdCard().length() - 6));
        user.setPassword(PasswordUtil.encode(rawPassword));
        user.setRealName(stuInfo.getName());
        user.setUserType(1);
        user.setPhone(stuInfo.getPhone());
        user.setEmail(stuInfo.getEmail());
        user.setStatus(1);
        userMapper.insertUser(user);

        stuInfo.setUserId(user.getId());
        stuInfoMapper.insert(stuInfo);
        return stuInfo;
    }

    @Override
    @Transactional
    public StuInfo update(StuInfo stuInfo) {
        stuInfoMapper.update(stuInfo);
        return stuInfo;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        StuInfo stu = stuInfoMapper.findById(id);
        if (stu != null) {
            stuInfoMapper.deleteById(id);
            userMapper.deleteById(stu.getUserId());
        }
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", stuInfoMapper.countAll());
        stats.put("reported", stuInfoMapper.countReported());
        return stats;
    }
}
