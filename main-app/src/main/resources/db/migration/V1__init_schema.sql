CREATE TABLE users (
    id            CHAR(36)     NOT NULL PRIMARY KEY,
    username      VARCHAR(30)  NOT NULL,
    email         VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(60)  NOT NULL,
    bio           VARCHAR(500),
    role          VARCHAR(20)  NOT NULL,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE job_posts (
    id          CHAR(36)       NOT NULL PRIMARY KEY,
    title       VARCHAR(100)   NOT NULL,
    description VARCHAR(2000)  NOT NULL,
    category    VARCHAR(30)    NOT NULL,
    status      VARCHAR(20)    NOT NULL,
    budget      DECIMAL(10, 2) NOT NULL,
    created_at  DATETIME       NOT NULL,
    client_id   CHAR(36)       NOT NULL,
    CONSTRAINT fk_job_client FOREIGN KEY (client_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE applications (
    id            CHAR(36)      NOT NULL PRIMARY KEY,
    job_post_id   CHAR(36)      NOT NULL,
    freelancer_id CHAR(36)      NOT NULL,
    cover_letter  VARCHAR(1000) NOT NULL,
    proposed_rate DECIMAL(10,2) NOT NULL,
    status        VARCHAR(20)   NOT NULL,
    applied_at    DATETIME      NOT NULL,
    CONSTRAINT fk_app_job        FOREIGN KEY (job_post_id)   REFERENCES job_posts (id),
    CONSTRAINT fk_app_freelancer FOREIGN KEY (freelancer_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE reviews (
    id            CHAR(36)    NOT NULL PRIMARY KEY,
    freelancer_id CHAR(36)    NOT NULL,
    client_id     CHAR(36)    NOT NULL,
    rating        INT         NOT NULL,
    comment       VARCHAR(500),
    created_at    DATETIME    NOT NULL,
    CONSTRAINT fk_review_freelancer FOREIGN KEY (freelancer_id) REFERENCES users (id),
    CONSTRAINT fk_review_client     FOREIGN KEY (client_id)     REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE INDEX idx_job_client   ON job_posts   (client_id);
CREATE INDEX idx_app_job      ON applications (job_post_id);
CREATE INDEX idx_app_free     ON applications (freelancer_id);
CREATE INDEX idx_review_free  ON reviews      (freelancer_id);
