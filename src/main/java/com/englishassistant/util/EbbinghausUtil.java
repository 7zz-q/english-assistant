package com.englishassistant.util;

import com.englishassistant.entity.Word;

import java.time.LocalDate;

/**
 * Ebbinghaus spaced repetition algorithm (SM-2 variant).
 * Direct port from Node.js vocabulary.js PATCH /:id/review handler.
 */
public class EbbinghausUtil {

    /**
     * Recalculate spaced repetition parameters.
     *
     * @param word    the current word entity (modified in place)
     * @param quality 0=forgot 1=vague 2=remembered 3=familiar 4=mastered
     */
    public static void recalculate(Word word, int quality) {
        double easeFactor = word.getEaseFactor() != null ? word.getEaseFactor() : 2.5;
        int interval = word.getInterval() != null ? word.getInterval() : 0;
        int reviewCount = word.getReviewCount() != null ? word.getReviewCount() : 0;

        // quality >= 4: user manually marks as mastered
        if (quality >= 4) {
            word.setStatus(2);
            word.setInterval(999);
            word.setReviewCount(reviewCount + 1);
            word.setEaseFactor(easeFactor);
            word.setNextReview(LocalDate.of(2099, 12, 31));
            return;
        }

        // quality < 2: forgot → reset interval
        if (quality < 2) {
            interval = 0;
        } else {
            // quality 2-3: advance via Ebbinghaus curve
            if (interval == 0) {
                interval = 1;
            } else if (interval == 1) {
                interval = 3;
            } else {
                interval = (int) Math.round(interval * easeFactor);
            }

            // SM-2 ease factor formula
            easeFactor = Math.max(1.3,
                easeFactor + (0.1 - (3 - quality) * (0.08 + (3 - quality) * 0.02)));
        }

        reviewCount++;
        // interval >= 21 days → mastered
        int newStatus = interval >= 21 ? 2 : 1;

        // next review = today + interval + 1
        LocalDate nextReview = LocalDate.now().plusDays(interval + 1);

        word.setEaseFactor(easeFactor);
        word.setInterval(interval);
        word.setReviewCount(reviewCount);
        word.setStatus(newStatus);
        word.setNextReview(nextReview);
    }
}
