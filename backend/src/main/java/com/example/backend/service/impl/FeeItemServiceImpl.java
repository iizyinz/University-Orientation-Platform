package com.example.backend.service.impl;

import com.example.backend.entity.FeeItem;
import com.example.backend.mapper.FeeItemMapper;
import com.example.backend.service.FeeItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeeItemServiceImpl implements FeeItemService {

    @Autowired
    private FeeItemMapper mapper;

    @Override
    public List<FeeItem> findAll() {
        return mapper.findAll();
    }

    @Override
    public FeeItem findById(Long id) {
        return mapper.findById(id);
    }

    @Override
    public FeeItem create(FeeItem item) {
        if (mapper.countByCode(item.getItemCode()) > 0) {
            throw new RuntimeException("费用编码已存在");
        }
        mapper.insert(item);
        return item;
    }

    @Override
    public FeeItem update(FeeItem item) {
        if (mapper.countByCodeExcludeId(item.getItemCode(), item.getId()) > 0) {
            throw new RuntimeException("费用编码已存在");
        }
        mapper.update(item);
        return item;
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
