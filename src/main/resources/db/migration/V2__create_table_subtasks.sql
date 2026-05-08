CREATE TABLE subtasks (
                          id BIGSERIAL PRIMARY KEY,
                          title VARCHAR(255) NOT NULL,
                          completed BOOLEAN NOT NULL DEFAULT FALSE,
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          task_id BIGINT NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP,

                          CONSTRAINT fk_subtasks_task_id FOREIGN KEY (task_id) REFERENCES tasks(id)
);

CREATE INDEX idx_subtask_task_id ON subtasks(task_id);