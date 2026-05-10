package github.oliveira.gb.taskcore.integration.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DbUtils {

    private final JdbcTemplate jdbcTemplate;

    public DbUtils(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void cleanDatabase() {
        jdbcTemplate.execute("SET session_replication_role = 'replica'");

        jdbcTemplate.execute("TRUNCATE TABLE tasks_tags RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE subtasks RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE tasks RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE tags RESTART IDENTITY CASCADE");

        jdbcTemplate.execute("SET session_replication_role = 'origin'");
    }
}