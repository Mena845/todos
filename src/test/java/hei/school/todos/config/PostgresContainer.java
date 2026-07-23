package hei.school.todos.config;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresContainer {

    public static final PostgreSQLContainer<?> INSTANCE =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("todos_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        INSTANCE.start();
    }
}
