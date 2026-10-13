package com.example.backend.service;

import com.example.backend.entity.CmsHandbook;
import java.util.List;

public interface CmsHandbookService {
    List<CmsHandbook> findAll();
    CmsHandbook findById(Long id);
    CmsHandbook create(CmsHandbook h);
    CmsHandbook update(CmsHandbook h);
    void delete(Long id);
}
