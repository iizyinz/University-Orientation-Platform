package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.entity.SysUser;
import com.example.backend.mapper.SysUserMapper;
import com.example.backend.service.SysUserService;
import com.example.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SysUserMapper userMapper;

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> loginForm, HttpServletRequest request) {
        String username = loginForm.get("username");
        String password = loginForm.get("password");

        if (username == null || username.isEmpty()) return Result.error(400, "用户名不能为空");
        if (password == null || password.isEmpty()) return Result.error(400, "密码不能为空");

        SysUser user = sysUserService.login(username, password);
        if (user == null) return Result.error(401, "用户名或密码错误");

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());

        String ip = getClientIp(request);
        userMapper.updateLoginInfo(user.getId(), ip);

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("userType", user.getUserType());
        data.put("avatar", user.getAvatar());
        data.put("token", token);

        return Result.success("登录成功", data);
    }

    @GetMapping("/info")
    public Result<?> getUserInfo(@RequestParam Long id) {
        SysUser user = sysUserService.getUserById(id);
        if (user == null) return Result.error(404, "用户不存在");
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("userType", user.getUserType());
        data.put("phone", user.getPhone());
        data.put("email", user.getEmail());
        data.put("avatar", user.getAvatar());
        data.put("createTime", user.getCreateTime());
        return Result.success(data);
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("id"));
        if (id == null) return Result.error(400, "缺少用户ID");

        SysUser user = new SysUser();
        user.setId(id);
        user.setRealName((String) body.get("realName"));
        user.setPhone((String) body.get("phone"));
        user.setEmail((String) body.get("email"));
        user.setAvatar((String) body.get("avatar"));

        try {
            SysUser updated = sysUserService.updateProfile(user);
            Map<String, Object> data = new HashMap<>();
            data.put("id", updated.getId());
            data.put("username", updated.getUsername());
            data.put("realName", updated.getRealName());
            data.put("userType", updated.getUserType());
            data.put("phone", updated.getPhone());
            data.put("email", updated.getEmail());
            data.put("avatar", updated.getAvatar());
            return Result.success("更新成功", data);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map<String, String> body) {
        String idStr = body.get("id");
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (idStr == null || oldPassword == null || newPassword == null) return Result.error(400, "参数不完整");
        if (newPassword.length() < 6) return Result.error("新密码至少6位");

        try {
            sysUserService.updatePassword(Long.parseLong(idStr), oldPassword, newPassword);
            return Result.success("密码修改成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
