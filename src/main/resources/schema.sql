-- ============================================================
-- English Assistant Database Schema (MySQL 8.0)
-- Migrated from SQLite (server/src/database.js)
-- Note: MySQL reserved words (interval, status, level, month,
--       type, year, date) are escaped with backticks.
-- ============================================================

-- 1. Users
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL COMMENT 'bcrypt hash',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Words (personal vocabulary)
CREATE TABLE IF NOT EXISTS words (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    word         VARCHAR(100) NOT NULL,
    phonetic     VARCHAR(255) DEFAULT NULL,
    meaning      TEXT         DEFAULT NULL,
    example      TEXT         DEFAULT NULL,
    `level`      VARCHAR(20)  DEFAULT 'cet6',
    `status`     TINYINT      DEFAULT 0 COMMENT '0=new 1=learning 2=mastered',
    review_count INT          DEFAULT 0,
    next_review  DATE         DEFAULT NULL,
    ease_factor  DOUBLE       DEFAULT 2.5,
    `interval`   INT          DEFAULT 0,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_word (user_id, word),
    INDEX idx_words_user_status (user_id, `status`),
    INDEX idx_words_next_review (user_id, next_review),
    CONSTRAINT fk_words_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Essays (writing corrections)
CREATE TABLE IF NOT EXISTS essays (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    title       VARCHAR(255) DEFAULT NULL,
    content     TEXT         NOT NULL,
    corrected   TEXT         DEFAULT NULL,
    score       DOUBLE       DEFAULT NULL,
    grammar     JSON         DEFAULT NULL,
    vocabulary  JSON         DEFAULT NULL,
    structure   JSON         DEFAULT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_essays_user (user_id),
    CONSTRAINT fk_essays_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Reading logs
CREATE TABLE IF NOT EXISTS reading_logs (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    title           VARCHAR(255) DEFAULT NULL,
    content         TEXT         DEFAULT NULL,
    words_looked_up JSON         DEFAULT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_readlog_user (user_id),
    CONSTRAINT fk_readlog_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Daily stats
CREATE TABLE IF NOT EXISTS daily_stats (
    id              BIGINT  AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT  NOT NULL,
    `date`          DATE    NOT NULL,
    words_learned   INT     DEFAULT 0,
    words_reviewed  INT     DEFAULT 0,
    essays_written  INT     DEFAULT 0,
    minutes_spent   INT     DEFAULT 0,
    UNIQUE KEY uk_user_date (user_id, `date`),
    INDEX idx_dailystats_user (user_id),
    CONSTRAINT fk_dailystats_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Word analysis cache (AI result cache, cold backup for Redis)
CREATE TABLE IF NOT EXISTS word_analysis (
    word       VARCHAR(100) PRIMARY KEY,
    data       JSON         NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Exam papers
CREATE TABLE IF NOT EXISTS exam_papers (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    `year`     INT          NOT NULL,
    `month`    INT          NOT NULL,
    title      VARCHAR(255) NOT NULL,
    `type`     VARCHAR(20)  DEFAULT 'cet6',
    sections   JSON         NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_papers_type (`type`),
    INDEX idx_papers_year_month (`year`, `month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Seed words (public word bank)
CREATE TABLE IF NOT EXISTS seed_words (
    id      BIGINT       AUTO_INCREMENT PRIMARY KEY,
    word    VARCHAR(100) NOT NULL,
    meaning TEXT         DEFAULT NULL,
    `level` VARCHAR(20)  DEFAULT 'cet6',
    UNIQUE KEY uk_seed_word (word),
    INDEX idx_seed_level (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
