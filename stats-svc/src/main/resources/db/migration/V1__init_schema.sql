CREATE TABLE job_stats (
    id                 CHAR(36)   NOT NULL PRIMARY KEY,
    category           VARCHAR(30) NOT NULL,
    total_job_posts    INT         NOT NULL DEFAULT 0,
    total_applications INT         NOT NULL DEFAULT 0,
    last_updated       DATETIME    NOT NULL,
    CONSTRAINT uq_stat_category UNIQUE (category)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
