package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.entity.*;
import com.example.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cms")
@CrossOrigin
public class CmsController {

    @Autowired private CmsHandbookService handbookService;
    @Autowired private CmsCampusMapService mapService;
    @Autowired private CmsNoticeService noticeService;

    // ==================== Handbook ====================
    @GetMapping("/handbook/list")
    public Result<List<CmsHandbook>> handbookList() { return Result.success(handbookService.findAll()); }
    @GetMapping("/handbook/{id}")
    public Result<CmsHandbook> handbookGet(@PathVariable Long id) {
        CmsHandbook h = handbookService.findById(id); return h != null ? Result.success(h) : Result.error(404, "不存在");
    }
    @PostMapping("/handbook")
    public Result<CmsHandbook> handbookCreate(@RequestBody Map<String, Object> body) {
        CmsHandbook h = new CmsHandbook();
        h.setTitle(str(body.get("title")));
        h.setContent(str(body.get("content")));
        h.setCoverImage(str(body.get("coverImage")));
        h.setSortOrder(toInt(body.get("sortOrder")));
        h.setPublishStatus(toInt(body.get("publishStatus")));
        return Result.success(handbookService.create(h));
    }
    @PutMapping("/handbook")
    public Result<CmsHandbook> handbookUpdate(@RequestBody Map<String, Object> body) {
        CmsHandbook h = new CmsHandbook();
        h.setId(toLong(body.get("id")));
        h.setTitle(str(body.get("title")));
        h.setContent(str(body.get("content")));
        h.setCoverImage(str(body.get("coverImage")));
        h.setSortOrder(toInt(body.get("sortOrder")));
        h.setPublishStatus(toInt(body.get("publishStatus")));
        return Result.success(handbookService.update(h));
    }
    @DeleteMapping("/handbook/{id}")
    public Result<Void> handbookDelete(@PathVariable Long id) { handbookService.delete(id); return Result.success(null); }

    // ==================== Campus Map ====================
    @GetMapping("/map/list")
    public Result<List<CmsCampusMap>> mapList() { return Result.success(mapService.findAll()); }
    @GetMapping("/map/{id}")
    public Result<CmsCampusMap> mapGet(@PathVariable Long id) {
        CmsCampusMap m = mapService.findById(id); return m != null ? Result.success(m) : Result.error(404, "不存在");
    }
    @PostMapping("/map")
    public Result<CmsCampusMap> mapCreate(@RequestBody Map<String, Object> body) {
        CmsCampusMap m = new CmsCampusMap();
        m.setMapName(str(body.get("mapName")));
        m.setMapImage(str(body.get("mapImage")));
        m.setDescription(str(body.get("description")));
        m.setSortOrder(toInt(body.get("sortOrder")));
        m.setStatus(toInt(body.get("status")));
        return Result.success(mapService.create(m));
    }
    @PutMapping("/map")
    public Result<CmsCampusMap> mapUpdate(@RequestBody Map<String, Object> body) {
        CmsCampusMap m = new CmsCampusMap();
        m.setId(toLong(body.get("id")));
        m.setMapName(str(body.get("mapName")));
        m.setMapImage(str(body.get("mapImage")));
        m.setDescription(str(body.get("description")));
        m.setSortOrder(toInt(body.get("sortOrder")));
        m.setStatus(toInt(body.get("status")));
        return Result.success(mapService.update(m));
    }
    @DeleteMapping("/map/{id}")
    public Result<Void> mapDelete(@PathVariable Long id) { mapService.delete(id); return Result.success(null); }

    // ==================== Notice ====================
    @GetMapping("/notice/list")
    public Result<List<CmsNotice>> noticeList() { return Result.success(noticeService.findAll()); }
    @GetMapping("/notice/{id}")
    public Result<CmsNotice> noticeGet(@PathVariable Long id) {
        CmsNotice n = noticeService.findById(id); return n != null ? Result.success(n) : Result.error(404, "不存在");
    }
    @PostMapping("/notice")
    public Result<CmsNotice> noticeCreate(@RequestBody Map<String, Object> body) {
        CmsNotice n = new CmsNotice();
        n.setTitle(str(body.get("title")));
        n.setContent(str(body.get("content")));
        n.setNoticeType(toInt(body.get("noticeType")));
        n.setPublishStatus(toInt(body.get("publishStatus")));
        n.setPublishTime(parseDateTime(str(body.get("publishTime"))));
        return Result.success(noticeService.create(n));
    }
    @PutMapping("/notice")
    public Result<CmsNotice> noticeUpdate(@RequestBody Map<String, Object> body) {
        CmsNotice n = new CmsNotice();
        n.setId(toLong(body.get("id")));
        n.setTitle(str(body.get("title")));
        n.setContent(str(body.get("content")));
        n.setNoticeType(toInt(body.get("noticeType")));
        n.setPublishStatus(toInt(body.get("publishStatus")));
        n.setPublishTime(parseDateTime(str(body.get("publishTime"))));
        return Result.success(noticeService.update(n));
    }
    @DeleteMapping("/notice/{id}")
    public Result<Void> noticeDelete(@PathVariable Long id) { noticeService.delete(id); return Result.success(null); }

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

    private LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDateTime.parse(s.replace(' ', 'T'));
        } catch (Exception e) {
            return null;
        }
    }
}
