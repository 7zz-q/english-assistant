package com.englishassistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.englishassistant.common.BusinessException;
import com.englishassistant.entity.ExamPaper;
import com.englishassistant.mapper.ExamPaperMapper;
import com.englishassistant.service.PaperService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PaperServiceImpl implements PaperService {

    private static final Logger log = LoggerFactory.getLogger(PaperServiceImpl.class);

    private final ExamPaperMapper examPaperMapper;
    private final ObjectMapper objectMapper;

    public PaperServiceImpl(ExamPaperMapper examPaperMapper, ObjectMapper objectMapper) {
        this.examPaperMapper = examPaperMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> listPapers(String type, int page, int limit) {
        LambdaQueryWrapper<ExamPaper> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(ExamPaper::getType, type);
        }
        wrapper.orderByDesc(ExamPaper::getYear).orderByDesc(ExamPaper::getMonth);

        Page<ExamPaper> result = examPaperMapper.selectPage(new Page<>(page, limit), wrapper);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("papers", result.getRecords());
        resp.put("total", result.getTotal());
        resp.put("page", page);
        return resp;
    }

    @Override
    public Map<String, Object> getStats() {
        List<Map<String, Object>> rows = examPaperMapper.selectMaps(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ExamPaper>()
                .select("type, COUNT(*) as count")
                .groupBy("type"));

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("stats", rows);
        return resp;
    }

    @Override
    public Map<String, Object> getDetail(Long id) {
        ExamPaper paper = examPaperMapper.selectById(id);
        if (paper == null) {
            throw new BusinessException(404, "试卷不存在");
        }

        Map<String, Object> paperMap = new LinkedHashMap<>();
        paperMap.put("id", paper.getId());
        paperMap.put("year", paper.getYear());
        paperMap.put("month", paper.getMonth());
        paperMap.put("title", paper.getTitle());
        paperMap.put("type", paper.getType());

        try {
            List<Object> sections = objectMapper.readValue(paper.getSections(), new TypeReference<List<Object>>() {});
            paperMap.put("sections", sections);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse paper sections: {}", e.getMessage());
            paperMap.put("sections", Collections.emptyList());
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("paper", paperMap);
        return resp;
    }
}
