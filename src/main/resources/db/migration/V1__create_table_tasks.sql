CREATE TABLE tasks (
    -- Identificador único com autoincremento (64 bits)
                       id BIGSERIAL PRIMARY KEY,

    -- Título da tarefa (Regra US01: 3 a 100 caracteres)
                       title VARCHAR(100) NOT NULL,

    -- Descrição opcional (Regra US01: Limite 500 caracteres)
                       description VARCHAR(500),

    -- Status da tarefa (PENDING, IN_PROGRESS, COMPLETED)
                       status VARCHAR(20) NOT NULL,

    -- Prazo para conclusão
                       due_date TIMESTAMP,

    -- Campos de Auditoria (Regra EN03)
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Controle de Exclusão Lógica (Regra US05)
                       active BOOLEAN NOT NULL DEFAULT TRUE
);

COMMENT ON COLUMN tasks.status IS 'Armazena o estado atual da tarefa: PENDING, IN_PROGRESS ou COMPLETED';
COMMENT ON COLUMN tasks.active IS 'Indica se o registro está ativo. Se FALSE, o registro foi excluído logicamente';