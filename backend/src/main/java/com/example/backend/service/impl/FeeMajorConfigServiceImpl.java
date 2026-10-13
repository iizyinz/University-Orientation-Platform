package com.example.backend.service.impl;

import com.example.backend.entity.FeeMajorConfig;
import com.example.backend.mapper.FeeMajorConfigMapper;
import com.example.backend.service.FeeMajorConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeeMajorConfigServiceImpl implements FeeMajorConfigService {

    @Autowired
    private FeeMajorConfigMapper mapper;

    @Override
    public List<FeeMajorConfig> findAll() {
        return mapper.findAll();
    }

    @Override
    public List<FeeMajorConfig> findByMajorId(Long majorId) {
        return mapper.findByMajorId(majorId);
    }

    @Override
    public FeeMajorConfig findById(Long id) {
        return mapper.findById(id);
    }

    @Override
    public FeeMajorConfig create(FeeMajorConfig config) {
        if (mapper.countByMajorAndItem(config.getMajorId(), config.getFeeItemId()) > 0) {
            throw new RuntimeException("该专业已配置此费用项目");
        }
        mapper.insert(config);
        return config;
    }

    @Override
    public FeeMajorConfig update(FeeMajorConfig config) {
        if (mapper.countByMajorAndItemExcludeId(config.getMajorId(), config.getFeeItemId(), config.getId()) > 0) {
            throw new RuntimeException("该专业已配置此费用项目");
        }
        mapper.update(config);
        return config;
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
