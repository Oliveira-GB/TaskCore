-- Adiciona coluna archived para feature de arquivamento
-- Regra: tasks arquivadas permanecem active=true (não é soft delete)
ALTER TABLE tasks
    ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN tasks.archived IS 'Indica se a tarefa está arquivada. Tasks arquivadas permanecem active=true';
