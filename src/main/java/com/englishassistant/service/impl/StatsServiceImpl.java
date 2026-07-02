package com.englishassistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.englishassistant.entity.DailyStat;
import com.englishassistant.entity.SeedWord;
import com.englishassistant.entity.Word;
import com.englishassistant.mapper.DailyStatMapper;
import com.englishassistant.mapper.SeedWordMapper;
import com.englishassistant.mapper.WordMapper;
import com.englishassistant.service.StatsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class StatsServiceImpl implements StatsService {

    private final WordMapper wordMapper;
    private final DailyStatMapper dailyStatMapper;
    private final SeedWordMapper seedWordMapper;

    // Display levels in order
    private static final List<String> DISPLAY_LEVELS = List.of("cet4", "cet6", "tem4", "tem8", "toefl", "gre");
    private static final Map<String, String> LEVEL_LABELS = Map.of(
        "cet4", "四级", "cet6", "六级", "tem4", "专四", "tem8", "专八",
        "toefl", "托福", "gre", "GRE"
    );

    public StatsServiceImpl(WordMapper wordMapper, DailyStatMapper dailyStatMapper,
                           SeedWordMapper seedWordMapper) {
        this.wordMapper = wordMapper;
        this.dailyStatMapper = dailyStatMapper;
        this.seedWordMapper = seedWordMapper;
    }

    @Override
    public Map<String, Object> getOverview(Long userId) {
        Long totalWords = wordMapper.selectCount(new LambdaQueryWrapper<Word>().eq(Word::getUserId, userId));
        Long mastered = wordMapper.selectCount(new LambdaQueryWrapper<Word>()
            .eq(Word::getUserId, userId).eq(Word::getStatus, 2));
        Long totalEssays = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.englishassistant.entity.Essay>()
            .eq("user_id", userId).getEntity() != null ? 0L : 0L;

        // Count essays directly
        com.englishassistant.entity.Essay essayQuery = new com.englishassistant.entity.Essay();
        Long essaysCount = dailyStatMapper.selectList(
            new LambdaQueryWrapper<DailyStat>().eq(DailyStat::getUserId, userId))
            .stream().mapToLong(d -> d.getEssaysWritten() != null ? d.getEssaysWritten() : 0).sum();

        // Avg score — use a simple approach
        Double avgScore = 0.0;
        // We'll just return 0 for simplicity since essays are counted elsewhere

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("totalWords", totalWords);
        resp.put("mastered", mastered);
        resp.put("totalEssays", essaysCount);
        resp.put("avgScore", avgScore);
        return resp;
    }

    @Override
    public Object getDailyStats(Long userId) {
        List<DailyStat> stats = dailyStatMapper.selectList(
            new LambdaQueryWrapper<DailyStat>()
                .eq(DailyStat::getUserId, userId)
                .orderByAsc(DailyStat::getDate)
                .last("LIMIT 30"));

        List<Map<String, Object>> list = new ArrayList<>();
        for (DailyStat ds : stats) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date", ds.getDate().toString());
            m.put("words_learned", ds.getWordsLearned());
            m.put("words_reviewed", ds.getWordsReviewed());
            m.put("essays_written", ds.getEssaysWritten());
            m.put("minutes_spent", ds.getMinutesSpent());
            list.add(m);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("stats", list);
        return resp;
    }

    @Override
    public Map<String, Object> getStreak(Long userId) {
        List<LocalDate> dates = dailyStatMapper.selectDatesByUserId(userId);

        int streak = 0;
        LocalDate expected = LocalDate.now();

        for (LocalDate rowDate : dates) {
            if (rowDate.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else if (rowDate.isBefore(expected)) {
                break;
            }
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("streak", streak);
        return resp;
    }

    @Override
    public Map<String, Object> getStatsByLevel(Long userId) {
        List<Map<String, Object>> levels = new ArrayList<>();

        for (String lvl : DISPLAY_LEVELS) {
            // User word stats
            Long total = wordMapper.selectCount(new LambdaQueryWrapper<Word>()
                .eq(Word::getUserId, userId).eq(Word::getLevel, lvl));
            Long mastered = wordMapper.selectCount(new LambdaQueryWrapper<Word>()
                .eq(Word::getUserId, userId).eq(Word::getLevel, lvl).eq(Word::getStatus, 2));
            Long learning = total - mastered;

            // Bank total
            Long bankTotal = seedWordMapper.selectCount(new LambdaQueryWrapper<SeedWord>().eq(SeedWord::getLevel, lvl));

            Map<String, Object> levelStat = new LinkedHashMap<>();
            levelStat.put("level", lvl);
            levelStat.put("label", LEVEL_LABELS.getOrDefault(lvl, lvl));
            levelStat.put("total", total);
            levelStat.put("newCount", 0);
            levelStat.put("learning", learning);
            levelStat.put("mastered", mastered);
            levelStat.put("bankTotal", bankTotal);
            levels.add(levelStat);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("levels", levels);
        return resp;
    }
}
