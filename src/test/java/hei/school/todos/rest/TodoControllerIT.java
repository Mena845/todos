package hei.school.todos.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import hei.school.todos.config.PostgresContainer;
import hei.school.todos.entity.TodoEntity;
import hei.school.todos.repository.TodoRepository;
import hei.school.todos.rest.dto.TodoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TodoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PostgresContainer.INSTANCE::getJdbcUrl);
        registry.add("spring.datasource.username", PostgresContainer.INSTANCE::getUsername);
        registry.add("spring.datasource.password", PostgresContainer.INSTANCE::getPassword);
    }

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    private TodoEntity createAndSave(String id, String title, boolean completed) {
        OffsetDateTime now = OffsetDateTime.now();
        TodoEntity entity = new TodoEntity(id, title, "description", completed, now, now);
        return repository.save(entity);
    }

    @Test
    void getAllTodos_empty_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAllTodos_withData_returnsAll() throws Exception {
        createAndSave("id-1", "Todo A", false);
        createAndSave("id-2", "Todo B", true);

        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("id-1")))
                .andExpect(jsonPath("$[1].id", is("id-2")));
    }

    @Test
    void getAllTodos_completedTrue_filtersCorrectly() throws Exception {
        createAndSave("1", "Active", false);
        createAndSave("2", "Done", true);
        createAndSave("3", "Also Done", true);

        mockMvc.perform(get("/todos").param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getAllTodos_completedFalse_filtersCorrectly() throws Exception {
        createAndSave("1", "Active", false);
        createAndSave("2", "Done", true);

        mockMvc.perform(get("/todos").param("completed", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Active")));
    }

    @Test
    void getTodoById_exists_returnsTodo() throws Exception {
        String uuid = "550e8400-e29b-41d4-a716-446655440001";
        createAndSave(uuid, "My Todo", false);

        mockMvc.perform(get("/todos/" + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(uuid)))
                .andExpect(jsonPath("$.title", is("My Todo")))
                .andExpect(jsonPath("$.completed", is(false)));
    }

    @Test
    void getTodoById_notExists_returns404() throws Exception {
        String uuid = "550e8400-e29b-41d4-a716-446655440099";
        mockMvc.perform(get("/todos/" + uuid))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTodoById_invalidUuid_returns400() throws Exception {
        mockMvc.perform(get("/todos/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upsertTodo_createNew_returns200() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setTitle("New Todo");
        request.setDescription("A brand new todo");
        request.setCompleted(false);

        mockMvc.perform(put("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title", is("New Todo")))
                .andExpect(jsonPath("$.description", is("A brand new todo")))
                .andExpect(jsonPath("$.completed", is(false)))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());

        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void upsertTodo_withExistingId_updatesTodo() throws Exception {
        String uuid = "550e8400-e29b-41d4-a716-446655440000";
        createAndSave(uuid, "Old Title", false);

        TodoRequest request = new TodoRequest();
        request.setId(uuid);
        request.setTitle("New Title");
        request.setDescription("Updated description");
        request.setCompleted(true);

        mockMvc.perform(put("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(uuid)))
                .andExpect(jsonPath("$.title", is("New Title")))
                .andExpect(jsonPath("$.description", is("Updated description")))
                .andExpect(jsonPath("$.completed", is(true)));

        assertThat(repository.count()).isEqualTo(1);
        TodoEntity saved = repository.findById(uuid).orElseThrow();
        assertThat(saved.getTitle()).isEqualTo("New Title");
        assertThat(saved.isCompleted()).isTrue();
    }

    @Test
    void upsertTodo_missingTitle_returns400() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setDescription("No title provided");

        mockMvc.perform(put("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upsertTodo_invalidId_returns400() throws Exception {
        TodoRequest request = new TodoRequest();
        request.setId("not-a-uuid");
        request.setTitle("Title");

        mockMvc.perform(put("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fullCycle_createGetUpdate() throws Exception {
        TodoRequest createRequest = new TodoRequest();
        createRequest.setTitle("Cycle Todo");
        createRequest.setDescription("Test full cycle");
        createRequest.setCompleted(false);

        String response = mockMvc.perform(put("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String createdId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/todos/" + createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Cycle Todo")));

        assertThat(repository.findById(createdId)).isPresent();
    }
}
