CREATE TABLE permissions (
    id          CHAR(36)     NOT NULL PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(200),
    CONSTRAINT uq_permission_name UNIQUE (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE user_permissions (
    user_id       CHAR(36) NOT NULL,
    permission_id CHAR(36) NOT NULL,
    PRIMARY KEY (user_id, permission_id),
    CONSTRAINT fk_up_user       FOREIGN KEY (user_id)       REFERENCES users (id),
    CONSTRAINT fk_up_permission FOREIGN KEY (permission_id) REFERENCES permissions (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO permissions (id, name, description) VALUES
    (UUID(), 'MANAGE_JOBS',    'Can create, edit, and delete job postings'),
    (UUID(), 'EXPORT_DATA',    'Can export job and application data to PDF or Excel'),
    (UUID(), 'VIEW_STATS',     'Can view job category statistics'),
    (UUID(), 'MANAGE_REVIEWS', 'Can delete any review as a moderator');
