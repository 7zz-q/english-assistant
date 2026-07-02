package com.englishassistant.service.impl;

import com.englishassistant.common.BusinessException;
import com.englishassistant.config.AiProperties;
import com.englishassistant.service.AiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * AI service — calls DashScope (通义千问) via OpenAI-compatible API.
 * All prompts are exact ports from server/src/services/ai.js.
 * Uses @ConfigurationProperties for type-safe config injection.
 */
@Service
public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AiProperties props;

    public AiServiceImpl(ObjectMapper objectMapper, AiProperties props) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.props = props;
    }

    // ──── Common: call DashScope API ────

    private Map<String, Object> callDashScope(String userMessage, String systemMessage,
                                               String modelName, double temperature,
                                               Integer maxTokens) {
        String url = props.getBaseUrl() + "/chat/completions";

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", modelName);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemMessage));
        messages.add(Map.of("role", "user", "content", userMessage));

        requestBody.put("messages", messages);
        requestBody.put("temperature", temperature);
        requestBody.put("response_format", Map.of("type", "json_object"));
        if (maxTokens != null) {
            requestBody.put("max_tokens", maxTokens);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + props.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            Map<String, Object> respBody = response.getBody();
            List<Map<String, Object>> choices = (List<Map<String, Object>>) respBody.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");

            content = content.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            return objectMapper.readValue(content, Map.class);
        } catch (Exception e) {
            log.error("DashScope API call failed: {}", e.getMessage());
            throw new BusinessException(500, "AI 服务暂不可用，请稍后重试");
        }
    }

    // ──── 1. correctEssay ────

    @Override
    public Map<String, Object> correctEssay(String content, String title) {
        String prompt = "你是一位英语写作老师，请批改以下英语作文。严格返回 JSON 格式，不要多余文字。\n\n";
        if (title != null && !title.isEmpty()) {
            prompt += "作文题目：" + title + "\n";
        }
        prompt += "学生作文：\n\"\"\"\n" + content + "\n\"\"\"\n\n";
        prompt += "返回 JSON（字段说明）：\n";
        prompt += "- score: 总分 0-100，综合语法、词汇、结构、内容打分\n";
        prompt += "- grammar: 数组，每项 {error: \"原文错误\", fix: \"正确写法\", explain: \"用中文解释原因\", severity: \"high|medium|low\"}\n";
        prompt += "- vocabulary: 数组，每项 {original: \"原文用词\", suggestion: \"更好的词\", reason: \"中文解释为什么更好\"}\n";
        prompt += "- structure: 数组，每项 {type: \"开头|衔接|结尾|逻辑\", comment: \"中文建议\", improved: \"改进后写法（英文）\"}\n";
        prompt += "- corrected: 润色后的完整作文（英文）\n\n";
        prompt += "要求：至少找出 3 个语法错误，至少 3 个词汇升级建议。如果原文很短，也如实标记。";

        return callDashScope(prompt,
            "You are a professional English writing tutor. Always output valid JSON only.",
            props.getModel(), 0.3, null);
    }

    // ──── 2. generateExample ────

    @Override
    public Map<String, Object> generateExample(String word) {
        String prompt = "请为英语单词\"" + word + "\"生成学习内容，返回 JSON：\n" +
            "{\n" +
            "  \"phonetic\": \"音标\",\n" +
            "  \"meaning\": \"中文释义（主要是CET-6常考义项）\",\n" +
            "  \"example\": \"包含该单词的自然英语例句\",\n" +
            "  \"example_cn\": \"例句中文翻译\",\n" +
            "  \"collocations\": [\"常用搭配1\", \"常用搭配2\"]\n" +
            "}";

        try {
            return callDashScope(prompt,
                "You are an English vocabulary teacher. Output JSON only.",
                props.getModel(), 0.5, null);
        } catch (Exception e) {
            log.warn("例句生成失败: {}", e.getMessage());
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("phonetic", "");
            fallback.put("meaning", "生成失败");
            fallback.put("example", "");
            fallback.put("example_cn", "");
            fallback.put("collocations", Collections.emptyList());
            return fallback;
        }
    }

    // ──── 3. analyzeWord ────

    @Override
    public Map<String, Object> analyzeWord(String word) {
        String prompt = "请为英语单词\"" + word + "\"做完整学习分析，严格返回 JSON：\n\n" +
            "{\n" +
            "  \"phonetic\": \"音标（如 /əbændən/）\",\n" +
            "  \"meanings\": [\n" +
            "    { \"text\": \"中文释义\", \"isKey\": true/false, \"type\": \"动词/名词/形容词等\" }\n" +
            "  ],\n" +
            "  \"example\": \"自然英语例句\",\n" +
            "  \"example_cn\": \"例句中文翻译\",\n" +
            "  \"similar_words\": [\n" +
            "    { \"word\": \"形近词\", \"meaning\": \"中文意思\", \"difference\": \"与" + word + "的区别用中文一句话说明\" }\n" +
            "  ],\n" +
            "  \"derived_words\": [\n" +
            "    { \"word\": \"派生词\", \"type\": \"名词/形容词/副词等\", \"meaning\": \"中文意思\" }\n" +
            "  ],\n" +
            "  \"options\": [\n" +
            "    { \"text\": \"正确中文释义A\", \"correct\": true },\n" +
            "    { \"text\": \"相近但错误B\", \"correct\": false },\n" +
            "    { \"text\": \"相近但错误C\", \"correct\": false },\n" +
            "    { \"text\": \"相近但错误D\", \"correct\": false }\n" +
            "  ]\n" +
            "}\n\n" +
            "要求：\n" +
            "- meanings 至少 3 条，覆盖常用义项，isKey=true 标记常考义项\n" +
            "- similar_words 至少 3 个形近词（拼写相似的单词）\n" +
            "- derived_words 至少 2 个派生词（该单词的不同词性变形，如名词→形容词→动词→副词）\n" +
            "- options 中 4 个选项必须看起来都合理（干扰项要真实可信）\n" +
            "- 所有释义用中文";

        return callDashScope(prompt,
            "You are a professional English vocabulary teacher. Output valid JSON only.",
            props.getFastModel(), 0.4, 1200);
    }

    // ──── 4. generateTopic ────

    @Override
    public Map<String, Object> generateTopic(String level) {
        String levelName = switch (level) {
            case "cet4" -> "四级";
            case "cet6" -> "六级";
            default -> "考研";
        };

        String prompt = "请为" + levelName + "英语考试作文部分生成一个题目。\n\n" +
            "返回 JSON：\n" +
            "{\n" +
            "  \"title\": \"作文题目（英文）\",\n" +
            "  \"title_cn\": \"题目中文翻译\",\n" +
            "  \"requirement\": \"写作要求（英文，如 write at least 150 words...）\",\n" +
            "  \"requirement_cn\": \"写作要求中文\",\n" +
            "  \"keywords\": [\"关键词1\", \"关键词2\", \"关键词3\"],\n" +
            "  \"outline\": [\"可用论点1（英文）\", \"可用论点2（英文）\"],\n" +
            "  \"outline_cn\": [\"可用论点1中文\", \"可用论点2中文\"]\n" +
            "}\n\n" +
            "题目要求：贴近真题风格，有思辨性，适合写150-200词短文。";

        return callDashScope(prompt,
            "You are an English exam question setter. Output JSON only.",
            props.getFastModel(), 0.8, 800);
    }

    // ──── 5. generateEssay ────

    @Override
    public Map<String, Object> generateEssay(String topic, String level) {
        String levelName = "cet4".equals(level) ? "四级" : "六级";

        String prompt = "请根据以下作文题目，写一篇" + levelName + "水平的英语范文。\n\n" +
            "题目：" + topic + "\n\n" +
            "返回 JSON：\n" +
            "{\n" +
            "  \"title\": \"题目\",\n" +
            "  \"essay\": \"范文全文（150-200词，结构完整，用词地道但不刻意复杂）\",\n" +
            "  \"essay_cn\": \"范文中文翻译\",\n" +
            "  \"highlights\": [\n" +
            "    { \"phrase\": \"范文中的好词组\", \"explain\": \"中文解释为什么好\" }\n" +
            "  ],\n" +
            "  \"vocabulary\": [\n" +
            "    { \"word\": \"高级词汇\", \"meaning\": \"中文意思\", \"sentence\": \"原文中的句子\" }\n" +
            "  ],\n" +
            "  \"structure\": \"文章结构分析（中文，简要说明开头-正文-结尾的逻辑）\"\n" +
            "}";

        return callDashScope(prompt,
            "You are an excellent English essay writer. Output JSON only.",
            props.getModel(), 0.6, 2000);
    }

    // ──── 6. translateText ────

    @Override
    public Map<String, Object> translateText(String text, String direction) {
        String dirDesc = switch (direction) {
            case "en2zh" -> "从英文翻译为中文";
            case "zh2en" -> "从中文翻译为英文";
            default -> "自动检测语言并翻译（英→中 或 中→英）";
        };

        String prompt = "请翻译以下文字。" + dirDesc + "。\n\n" +
            "原文：\n\"\"\"\n" + text + "\n\"\"\"\n\n" +
            "返回 JSON：\n" +
            "{\n" +
            "  \"source_lang\": \"英语/中文\",\n" +
            "  \"target_lang\": \"英语/中文\",\n" +
            "  \"translation\": \"完整译文\",\n" +
            "  \"notes\": [\n" +
            "    { \"original\": \"原文中关键短语\", \"translation\": \"翻译\", \"explain\": \"翻译要点说明（中文，一句话）\" }\n" +
            "  ],\n" +
            "  \"difficult_words\": [\n" +
            "    { \"word\": \"难词\", \"meaning\": \"中文解释\" }\n" +
            "  ]\n" +
            "}";

        return callDashScope(prompt,
            "You are a professional translator between English and Chinese. Output JSON only.",
            props.getFastModel(), 0.2, 1500);
    }
}
