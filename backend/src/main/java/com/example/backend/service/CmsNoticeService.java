package com.example.backend.service;

import com.example.backend.entity.CmsNotice;
import java.util.List;

public interface CmsNoticeService {
    List<CmsNotice> findAll();
    CmsNotice findById(Long id);
    CmsNotice create(CmsNotice n);
    CmsNotice update(CmsNotice n);
    void delete(Long id);
}
