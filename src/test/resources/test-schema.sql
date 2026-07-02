CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS words (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    word VARCHAR(100) NOT NULL,
    phonetic VARCHAR(255),
    meaning TEXT,
    example TEXT,
    level VARCHAR(20) DEFAULT 'cet6',
    status TINYINT DEFAULT 0,
    review_count INT DEFAULT 0,
    next_review DATE,
    ease_factor DOUBLE DEFAULT 2.5,
    interval INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS daily_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,
    words_learned INT DEFAULT 0,
    words_reviewed INT DEFAULT 0,
    essays_written INT DEFAULT 0,
    minutes_spent INT DEFAULT 0
);
