-- Índice para otimizar filtros temporais por data de vencimento
-- Usado nas consultas OVERDUE, TODAY e THIS_WEEK
CREATE INDEX idx_tasks_due_date ON tasks(due_date);

COMMENT ON INDEX idx_tasks_due_date IS 'Otimiza queries de filtragem por deadline (OVERDUE, TODAY, THIS_WEEK)';
