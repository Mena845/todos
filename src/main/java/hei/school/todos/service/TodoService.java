package hei.school.todos.service;

import hei.school.todos.model.Todo;

import java.util.List;
import java.util.Optional;

public interface TodoService {

    List<Todo> getAllTodos(Optional<Boolean> completed);

    Todo getTodoById(String id);

    Todo upsertTodo(Todo todo);
}