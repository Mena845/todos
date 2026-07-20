package hei.school.todos.service;


import hei.school.todos.entity.TodoEntity;
import hei.school.todos.mapper.TodoMapper;
import hei.school.todos.model.Todo;
import hei.school.todos.repository.TodoRepository;
import hei.school.todos.rest.exception.TodoNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TodoServiceImpl implements TodoService {

    private final TodoRepository repository;
    private final TodoMapper mapper;

    public TodoServiceImpl(TodoRepository repository, TodoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Todo> getAllTodos(Optional<Boolean> completed) {
        List<TodoEntity> entities = completed
                .map(repository::findByCompleted)
                .orElseGet(repository::findAll);

        return entities.stream().map(mapper::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Todo getTodoById(String id) {
        TodoEntity entity = repository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
        return mapper.toModel(entity);
    }

    @Override
    @Transactional
    public Todo upsertTodo(Todo todo) {
        Optional<TodoEntity> existing = todo.getId() != null
                ? repository.findById(todo.getId())
                : Optional.empty();

        TodoEntity entity = existing
                .map(e -> updateFields(e, todo))
                .orElseGet(() -> mapper.toEntity(withGeneratedId(todo)));

        return mapper.toModel(repository.save(entity));
    }

    private Todo withGeneratedId(Todo todo) {
        todo.setId(UUID.randomUUID().toString());
        return todo;
    }

    private TodoEntity updateFields(TodoEntity entity, Todo todo) {
        entity.setTitle(todo.getTitle());
        entity.setDescription(todo.getDescription());
        entity.setCompleted(todo.isCompleted());
        return entity;
    }
}