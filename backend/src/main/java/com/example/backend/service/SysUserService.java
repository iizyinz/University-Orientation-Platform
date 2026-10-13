package com.example.backend.service;

import com.example.backend.entity.SysUser;

public interface SysUserService {
    SysUser login(String username, String password);
    SysUser getUserById(Long id);
    SysUser updateProfile(SysUser user);
    void updatePassword(Long id, String oldPassword, String newPassword);
}
