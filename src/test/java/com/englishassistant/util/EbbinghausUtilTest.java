package com.englishassistant.util;

import com.englishassistant.entity.Word;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EbbinghausUtilTest {

    @Test
    void testQualityMastered() {
        Word word = createWord();
        EbbinghausUtil.recalculate(word, 4);

        assertEquals(2, word.getStatus());           // mastered
        assertEquals(999, word.getInterval());
        assertEquals(LocalDate.of(2099, 12, 31), word.getNextReview());
    }

    @Test
    void testQualityForgot() {
        Word word = createWord();
        word.setInterval(10);
        word.setEaseFactor(2.5);

        EbbinghausUtil.recalculate(word, 0);

        assertEquals(0, word.getInterval());         // reset
        assertEquals(1, word.getStatus());           // learning
    }

    @Test
    void testQualityNewWord() {
        Word word = createWord();  // interval=0

        EbbinghausUtil.recalculate(word, 2);

        assertEquals(1, word.getInterval());         // 0→1
        assertNotNull(word.getNextReview());
        assertEquals(1, word.getStatus());
    }

    @Test
    void testIntervalProgression() {
        Word word = createWord();
        word.setInterval(1);

        EbbinghausUtil.recalculate(word, 3);

        assertEquals(3, word.getInterval());         // 1→3
        assertTrue(word.getEaseFactor() >= 1.3);
    }

    @Test
    void testEaseFactorMinimum() {
        Word word = createWord();
        word.setInterval(3);
        word.setEaseFactor(1.3);

        EbbinghausUtil.recalculate(word, 1);  // low quality

        // Ease factor shouldn't drop below 1.3
        assertEquals(1.3, word.getEaseFactor(), 0.001);
    }

    @Test
    void testIntervalMultipliedByEaseFactor() {
        Word word = createWord();
        word.setInterval(10);
        word.setEaseFactor(2.5);

        EbbinghausUtil.recalculate(word, 3);

        assertEquals(25, word.getInterval());        // 10 * 2.5 = 25 → mastered
        assertEquals(2, word.getStatus());
    }

    @Test
    void testReviewCountIncrements() {
        Word word = createWord();

        EbbinghausUtil.recalculate(word, 3);

        assertEquals(1, word.getReviewCount().intValue());
    }

    private Word createWord() {
        Word word = new Word();
        word.setId(1L);
        word.setUserId(1L);
        word.setWord("abandon");
        word.setInterval(0);
        word.setEaseFactor(2.5);
        word.setReviewCount(0);
        word.setStatus(0);
        word.setNextReview(LocalDate.now());
        return word;
    }
}
