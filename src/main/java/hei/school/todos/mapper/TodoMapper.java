package hei.school.todos.mapper;

import hei.school.todos.rest.dto.TodoResponse;
import hei.school.todos.entity.TodoEntity;
import hei.school.todos.model.Todo;
import hei.school.todos.rest.dto.TodoRequest;
import org.springframework.stereotype.Component;

@Component
public class TodoMapper {

    public Todo toModel(TodoEntity entity) {
        if (entity == null) return null;
        return new Todo(
                entity.getId(), entity.getTitle(), entity.getDescription(),
                entity.isCompleted(), entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }

    public TodoEntity toEntity(Todo model) {
        if (model == null) return null;
        return new TodoEntity(
                model.getId(), model.getTitle(), model.getDescription(),
                model.isCompleted(), model.getCreatedAt(), model.getUpdatedAt()
        );
    }

    public Todo toModel(TodoRequest request) {
        if (request == null) return null;
        Todo todo = new Todo();
        todo.setId(request.getId());
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setCompleted(request.isCompleted());
        return todo;
    }

    public TodoResponse toResponse(Todo model) {
        if (model == null) return null;
        return new TodoResponse(
                model.getId(), model.getTitle(), model.getDescription(),
                model.isCompleted(), model.getCreatedAt(), model.getUpdatedAt()
        );
    }
}