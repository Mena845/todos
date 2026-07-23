package hei.school.todos.service;

import hei.school.todos.entity.TodoEntity;
import hei.school.todos.mapper.TodoMapper;
import hei.school.todos.model.Todo;
import hei.school.todos.repository.TodoRepository;
import hei.school.todos.rest.exception.TodoNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceImplTest {

    @Mock
    private TodoRepository repository;

    @Mock
    private TodoMapper mapper;

    @InjectMocks
    private TodoServiceImpl service;

    private TodoEntity sampleEntity(String id, String title, boolean completed) {
        OffsetDateTime now = OffsetDateTime.now();
        return new TodoEntity(id, title, "desc", completed, now, now);
    }

    private Todo sampleModel(String id, String title, boolean completed) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Todo(id, title, "desc", completed, now, now);
    }

    @Test
    void getAllTodos_noFilter_returnsAll() {
        TodoEntity e1 = sampleEntity("1", "A", false);
        TodoEntity e2 = sampleEntity("2", "B", true);
        Todo m1 = sampleModel("1", "A", false);
        Todo m2 = sampleModel("2", "B", true);

        when(repository.findAll()).thenReturn(List.of(e1, e2));
        when(mapper.toModel(e1)).thenReturn(m1);
        when(mapper.toModel(e2)).thenReturn(m2);

        List<Todo> result = service.getAllTodos(Optional.empty());

        assertThat(result).hasSize(2);
        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getAllTodos_completedTrue_filtersByCompleted() {
        TodoEntity e1 = sampleEntity("1", "A", true);
        Todo m1 = sampleModel("1", "A", true);

        when(repository.findByCompleted(true)).thenReturn(List.of(e1));
        when(mapper.toModel(e1)).thenReturn(m1);

        List<Todo> result = service.getAllTodos(Optional.of(true));

        assertThat(result).hasSize(1);
        verify(repository).findByCompleted(true);
    }

    @Test
    void getAllTodos_completedFalse_filtersByNotCompleted() {
        when(repository.findByCompleted(false)).thenReturn(List.of());

        List<Todo> result = service.getAllTodos(Optional.of(false));

        assertThat(result).isEmpty();
        verify(repository).findByCompleted(false);
    }

    @Test
    void getTodoById_exists_returnsTodo() {
        String id = "test-id";
        TodoEntity entity = sampleEntity(id, "A", false);
        Todo model = sampleModel(id, "A", false);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toModel(entity)).thenReturn(model);

        Todo result = service.getTodoById(id);

        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void getTodoById_notExists_throwsNotFound() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTodoById("missing"))
                .isInstanceOf(TodoNotFoundException.class);
    }

    @Test
    void upsertTodo_noId_generatesIdAndSaves() {
        Todo input = sampleModel(null, "New", false);
        TodoEntity savedEntity = sampleEntity("generated-uuid", "New", false);
        Todo savedModel = sampleModel("generated-uuid", "New", false);

        when(repository.save(any(TodoEntity.class))).thenReturn(savedEntity);
        when(mapper.toEntity(any(Todo.class))).thenReturn(savedEntity);
        when(mapper.toModel(savedEntity)).thenReturn(savedModel);

        Todo result = service.upsertTodo(input);

        assertThat(result.getId()).isEqualTo("generated-uuid");
        verify(repository).save(any(TodoEntity.class));
    }

    @Test
    void upsertTodo_withExistingId_updatesFields() {
        String id = "existing-id";
        Todo input = sampleModel(id, "Updated", true);
        TodoEntity existing = sampleEntity(id, "Old", false);
        TodoEntity updatedEntity = sampleEntity(id, "Updated", true);
        Todo updatedModel = sampleModel(id, "Updated", true);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(TodoEntity.class))).thenReturn(updatedEntity);
        when(mapper.toModel(updatedEntity)).thenReturn(updatedModel);

        Todo result = service.upsertTodo(input);

        assertThat(result.getTitle()).isEqualTo("Updated");
        assertThat(result.isCompleted()).isTrue();
        verify(repository).save(argThat(e ->
                e.getTitle().equals("Updated") && e.isCompleted()));
    }
}
