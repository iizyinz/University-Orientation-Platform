package com.example.backend.service.impl;

import com.example.backend.entity.SysUser;
import com.example.backend.mapper.SysUserMapper;
import com.example.backend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public SysUser login(String username, String password) {
        SysUser user = sysUserMapper.findByUsername(username);
        if (user == null) return null;
        if (user.getStatus() != 1) return null;
        if (!passwordEncoder.matches(password, user.getPassword())) return null;
        return user;
    }

    @Override
    public SysUser getUserById(Long id) {
        return sysUserMapper.findById(id);
    }

    @Override
    public SysUser updateProfile(SysUser user) {
        sysUserMapper.updateProfile(user);
        return sysUserMapper.findById(user.getId());
    }

    @Override
    public void updatePassword(Long id, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.findById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) throw new RuntimeException("原密码错误");
        sysUserMapper.updatePassword(passwordEncoder.encode(newPassword), id);
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
