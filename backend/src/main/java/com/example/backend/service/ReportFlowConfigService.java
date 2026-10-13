package com.example.backend.service;

import com.example.backend.entity.ReportFlowConfig;
import java.util.List;

public interface ReportFlowConfigService {
    List<ReportFlowConfig> findAll();
    ReportFlowConfig findById(Long id);
    ReportFlowConfig create(ReportFlowConfig config);
    ReportFlowConfig update(ReportFlowConfig config);
    void delete(Long id);
}
