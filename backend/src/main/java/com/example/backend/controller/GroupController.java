package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.entity.GroupConfig;
import com.example.backend.service.GroupConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group")
@CrossOrigin
public class GroupController {

    @Autowired
    private GroupConfigService service;

    @GetMapping("/list")
    public Result<List<GroupConfig>> list() { return Result.success(service.findAll()); }

    @GetMapping("/{id}")
    public Result<GroupConfig> get(@PathVariable Long id) {
        GroupConfig g = service.findById(id);
        return g != null ? Result.success(g) : Result.error(404, "不存在");
    }

    @PostMapping
    public Result<GroupConfig> create(@RequestBody Map<String, Object> body) {
        GroupConfig g = new GroupConfig();
        g.setGroupType(toInt(body.get("groupType")));
        g.setGroupName(str(body.get("groupName")));
        g.setGroupNo(str(body.get("groupNo")));
        g.setGroupQrImage(str(body.get("groupQrImage")));
        g.setTargetId(toLong(body.get("targetId")));
        g.setProvince(str(body.get("province")));
        g.setDescription(str(body.get("description")));
        g.setSortOrder(toInt(body.get("sortOrder")));
        g.setStatus(toInt(body.get("status")));
        return Result.success(service.create(g));
    }

    @PutMapping
    public Result<GroupConfig> update(@RequestBody Map<String, Object> body) {
        GroupConfig g = new GroupConfig();
        g.setId(toLong(body.get("id")));
        g.setGroupType(toInt(body.get("groupType")));
        g.setGroupName(str(body.get("groupName")));
        g.setGroupNo(str(body.get("groupNo")));
        g.setGroupQrImage(str(body.get("groupQrImage")));
        g.setTargetId(toLong(body.get("targetId")));
        g.setProvince(str(body.get("province")));
        g.setDescription(str(body.get("description")));
        g.setSortOrder(toInt(body.get("sortOrder")));
        g.setStatus(toInt(body.get("status")));
        return Result.success(service.update(g));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { service.delete(id); return Result.success(null); }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        return Long.parseLong(val.toString());
    }

    private Integer toInt(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        return Integer.parseInt(val.toString());
    }

    private String str(Object val) {
        return val != null ? val.toString() : null;
    }
}
