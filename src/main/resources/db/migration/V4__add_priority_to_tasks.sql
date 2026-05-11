ALTER TABLE tasks
ADD COLUMN priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';

COMMENT ON COLUMN tasks.priority IS 'Priority level of the task: LOW, MEDIUM, HIGH, CRITICAL';

CREATE INDEX idx_tasks_priority ON tasks(priority);
