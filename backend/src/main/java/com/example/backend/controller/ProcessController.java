package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.entity.ReportFlowConfig;
import com.example.backend.service.ReportFlowConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/process")
@CrossOrigin
public class ProcessController {

    @Autowired
    private ReportFlowConfigService flowConfigService;

    @GetMapping("/list")
    public Result<List<ReportFlowConfig>> list() {
        return Result.success(flowConfigService.findAll());
    }

    @GetMapping("/{id}")
    public Result<ReportFlowConfig> get(@PathVariable Long id) {
        ReportFlowConfig config = flowConfigService.findById(id);
        if (config == null) return Result.error(404, "流程配置不存在");
        return Result.success(config);
    }

    @PostMapping
    public Result<ReportFlowConfig> create(@RequestBody ReportFlowConfig config) {
        try {
            return Result.success(flowConfigService.create(config));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    public Result<ReportFlowConfig> update(@RequestBody ReportFlowConfig config) {
        try {
            return Result.success(flowConfigService.update(config));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        flowConfigService.delete(id);
        return Result.success(null);
    }
}
