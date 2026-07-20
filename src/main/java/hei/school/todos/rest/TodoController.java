package hei.school.todos.rest;

import hei.school.todos.mapper.TodoMapper;
import hei.school.todos.model.Todo;
import hei.school.todos.service.TodoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/todos")
@Validated
public class TodoController {

    private final TodoService service;
    private final TodoMapper mapper;

    public TodoController(TodoService service, TodoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }


    @GetMapping
    public ResponseEntity<List<TodoResponse>> getAllTodos(
            @RequestParam(name = "completed", required = false) Boolean completed) {

        List<Todo> todos = service.getAllTodos(Optional.ofNullable(completed));
        List<TodoResponse> response = todos.stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> getTodoById(
            @PathVariable @NotBlank @ValidUUID String id) {

        Todo todo = service.getTodoById(id);
        return ResponseEntity.ok(mapper.toResponse(todo));
    }


    @PutMapping
    public ResponseEntity<TodoResponse> upsertTodo(@Valid @RequestBody TodoRequest request) {
        Todo todo = mapper.toModel(request);
        Todo saved = service.upsertTodo(todo);
        return ResponseEntity.status(HttpStatus.OK).body(mapper.toResponse(saved));
    }
}