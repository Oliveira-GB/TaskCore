CREATE TABLE task_notes (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_task_notes_task_id FOREIGN KEY (task_id) REFERENCES tasks(id)
);

CREATE INDEX idx_task_notes_task_id ON task_notes(task_id);

COMMENT ON TABLE task_notes IS 'Notas associadas às tarefas (sistema de soft delete)';
COMMENT ON COLUMN task_notes.content IS 'Conteúdo textual da nota (máximo 1000 caracteres)';
COMMENT ON COLUMN task_notes.active IS 'Indica se a nota está ativa (false = soft deleted)';
