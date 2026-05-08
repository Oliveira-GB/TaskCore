CREATE TABLE tags (
                      id BIGSERIAL PRIMARY KEY,
                      name VARCHAR(20) NOT NULL UNIQUE,
                      active BOOLEAN NOT NULL DEFAULT TRUE,
                      created_at TIMESTAMP NOT NULL,
                      updated_at TIMESTAMP
);

CREATE TABLE tasks_tags (
                            task_id BIGINT NOT NULL,
                            tag_id BIGINT NOT NULL,

                            PRIMARY KEY (task_id, tag_id),

                            CONSTRAINT fk_tasks_tags_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
                            CONSTRAINT fk_tasks_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE INDEX idx_tasks_tags_tag_id ON tasks_tags(tag_id);