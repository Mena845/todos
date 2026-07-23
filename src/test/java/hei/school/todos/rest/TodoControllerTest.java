package hei.school.todos.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import hei.school.todos.mapper.TodoMapper;
import hei.school.todos.model.Todo;
import hei.school.todos.rest.dto.TodoRequest;
import hei.school.todos.rest.dto.TodoResponse;
import hei.school.todos.rest.exception.GlobalExceptionHandler;
import hei.school.todos.rest.exception.TodoNotFoundException;
import hei.school.todos.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TodoControllerTest {

    @Mock
    private TodoService service;

    @Mock
    private TodoMapper mapper;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        TodoController controller = new TodoController(service, mapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Todo sampleModel(String id, String title, boolean completed) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Todo(id, title, "desc", completed, now, now);
    }

    private TodoResponse sampleResponse(String id, String title, boolean completed) {
        OffsetDateTime now = OffsetDateTime.now();
        return new TodoResponse(id, title, "desc", completed, now, now);
    }

    @Test
    void getAllTodos_returnsList() throws Exception {
        Todo m1 = sampleModel("1", "A", false);
        Todo m2 = sampleModel("2", "B", true);
        TodoResponse r1 = sampleResponse("1", "A", false);
        TodoResponse r2 = sampleResponse("2", "B", true);

        when(service.getAllTodos(Optional.empty())).thenReturn(List.of(m1, m2));
        when(mapper.toResponse(m1)).thenReturn(r1);
        when(mapper.toResponse(m2)).thenReturn(r2);

        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("1")))
                .andExpect(jsonPath("$[1].id", is("2")));
    }

    @Test
    void getAllTodos_withCompletedFilter_delegatesToService() throws Exception {
        when(service.getAllTodos(Optional.of(true))).thenReturn(List.of());

        mockMvc.perform(get("/todos").param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(service).getAllTodos(Optional.of(true));
    }

    @Test
    void getTodoById_exists_returnsTodo() throws Exception {
        Todo model = sampleModel("test-id", "Title", false);
        TodoResponse response = sampleResponse("test-id", "Title", false);

        when(service.getTodoById("test-id")).thenReturn(model);
        when(mapper.toResponse(model)).thenReturn(response);

        mockMvc.perform(get("/todos/test-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("test-id")))
                .andExpect(jsonPath("$.title", is("Title")));
    }

    @Test
    void getTodoById_notExists_returns404() throws Exception {
        when(service.getTodoById("missing")).thenThrow(new TodoNotFoundException("missing"));

        mockMvc.perform(get("/todos/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void upsertTodo_create_returns200() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setTitle("New Todo");
        request.setDescription("Desc");
        request.setCompleted(false);

        Todo model = sampleModel(null, "New Todo", false);
        Todo savedModel = sampleModel("generated-id", "New Todo", false);
        TodoResponse response = sampleResponse("generated-id", "New Todo", false);

        when(mapper.toModel(any(TodoRequest.class))).thenReturn(model);
        when(service.upsertTodo(model)).thenReturn(savedModel);
        when(mapper.toResponse(savedModel)).thenReturn(response);

        mockMvc.perform(put("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("generated-id")))
                .andExpect(jsonPath("$.title", is("New Todo")));
    }

    @Test
    void upsertTodo_withId_updatesExisting() throws Exception {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        TodoRequest request = new TodoRequest();
        request.setId(uuid);
        request.setTitle("Updated");
        request.setCompleted(true);

        Todo model = sampleModel(uuid, "Updated", true);
        Todo savedModel = sampleModel(uuid, "Updated", true);
        TodoResponse response = sampleResponse(uuid, "Updated", true);

        when(mapper.toModel(any(TodoRequest.class))).thenReturn(model);
        when(service.upsertTodo(model)).thenReturn(savedModel);
        when(mapper.toResponse(savedModel)).thenReturn(response);

        mockMvc.perform(put("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(uuid)))
                .andExpect(jsonPath("$.title", is("Updated")));
    }

    @Test
    void upsertTodo_missingTitle_returns400() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setDescription("No title");

        mockMvc.perform(put("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
