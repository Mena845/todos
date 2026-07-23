package hei.school.todos.repository;

import hei.school.todos.config.PostgresContainer;
import hei.school.todos.entity.TodoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TodoRepositoryIT {

    @Autowired
    private TodoRepository repository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PostgresContainer.INSTANCE::getJdbcUrl);
        registry.add("spring.datasource.username", PostgresContainer.INSTANCE::getUsername);
        registry.add("spring.datasource.password", PostgresContainer.INSTANCE::getPassword);
    }

    private TodoEntity createEntity(String id, String title, boolean completed) {
        OffsetDateTime now = OffsetDateTime.now();
        return new TodoEntity(id, title, "description", completed, now, now);
    }

    @Test
    void save_and_findById() {
        TodoEntity entity = createEntity("id-1", "Test Todo", false);
        repository.save(entity);

        Optional<TodoEntity> found = repository.findById("id-1");

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Todo");
        assertThat(found.get().isCompleted()).isFalse();
    }

    @Test
    void findAll_returnsAll() {
        repository.save(createEntity("1", "A", false));
        repository.save(createEntity("2", "B", true));

        List<TodoEntity> all = repository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void findByCompleted_filtersCorrectly() {
        repository.save(createEntity("1", "A", false));
        repository.save(createEntity("2", "B", true));
        repository.save(createEntity("3", "C", true));

        List<TodoEntity> completed = repository.findByCompleted(true);
        List<TodoEntity> notCompleted = repository.findByCompleted(false);

        assertThat(completed).hasSize(2);
        assertThat(notCompleted).hasSize(1);
        assertThat(notCompleted.get(0).getTitle()).isEqualTo("A");
    }

    @Test
    void findById_notFound_returnsEmpty() {
        Optional<TodoEntity> found = repository.findById("nonexistent");

        assertThat(found).isEmpty();
    }

    @Test
    void delete_removesEntity() {
        TodoEntity entity = createEntity("id-1", "To Delete", false);
        repository.save(entity);

        repository.deleteById("id-1");

        assertThat(repository.findById("id-1")).isEmpty();
    }

    @Test
    void save_updatesEntity() {
        TodoEntity entity = createEntity("id-1", "Original", false);
        repository.save(entity);

        entity.setTitle("Updated");
        entity.setCompleted(true);
        repository.save(entity);

        TodoEntity found = repository.findById("id-1").orElseThrow();
        assertThat(found.getTitle()).isEqualTo("Updated");
        assertThat(found.isCompleted()).isTrue();
    }

    @Test
    void count_returnsCorrectCount() {
        repository.save(createEntity("1", "A", false));
        repository.save(createEntity("2", "B", true));

        long count = repository.count();

        assertThat(count).isEqualTo(2);
    }

    @Test
    void save_persistsTimestamps() {
        TodoEntity entity = createEntity("id-1", "Timestamps", false);
        repository.save(entity);

        TodoEntity found = repository.findById("id-1").orElseThrow();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }
}
