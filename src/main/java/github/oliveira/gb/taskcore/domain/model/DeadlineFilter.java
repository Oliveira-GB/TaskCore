package github.oliveira.gb.taskcore.domain.model;

/**
 * Filtros inteligentes para deadline baseados no tempo UTC do servidor.
 * Permite consultar tarefas por status temporal relativo.
 */
public enum DeadlineFilter {
    /**
     * Tarefas com dueDate anterior ao momento atual.
     * Exclui automaticamente tarefas com status COMPLETED.
     */
    OVERDUE,

    /**
     * Tarefas com dueDate no dia atual (00:00:00 a 23:59:59 UTC).
     */
    TODAY,

    /**
     * Tarefas com dueDate entre hoje e os próximos 7 dias (inclusive).
     */
    THIS_WEEK
}
