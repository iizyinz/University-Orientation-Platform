package com.example.backend.service.impl;

import com.example.backend.entity.ReportFlowConfig;
import com.example.backend.mapper.ReportFlowConfigMapper;
import com.example.backend.service.ReportFlowConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportFlowConfigServiceImpl implements ReportFlowConfigService {

    @Autowired
    private ReportFlowConfigMapper mapper;

    @Override
    public List<ReportFlowConfig> findAll() {
        return mapper.findAll();
    }

    @Override
    public ReportFlowConfig findById(Long id) {
        return mapper.findById(id);
    }

    @Override
    public ReportFlowConfig create(ReportFlowConfig config) {
        if (config.getFlowStep() == null) {
            config.setFlowStep(mapper.nextStep());
        }
        mapper.insert(config);
        return config;
    }

    @Override
    public ReportFlowConfig update(ReportFlowConfig config) {
        mapper.update(config);
        return config;
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
