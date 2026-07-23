package hei.school.todos.mapper;

import hei.school.todos.entity.TodoEntity;
import hei.school.todos.model.Todo;
import hei.school.todos.rest.dto.TodoRequest;
import hei.school.todos.rest.dto.TodoResponse;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TodoMapperTest {

    private final TodoMapper mapper = new TodoMapper();

    @Test
    void toModel_fromEntity_mapsAllFields() {
        OffsetDateTime now = OffsetDateTime.now();
        TodoEntity entity = new TodoEntity("id-1", "Title", "Desc", true, now, now);

        Todo model = mapper.toModel(entity);

        assertThat(model.getId()).isEqualTo("id-1");
        assertThat(model.getTitle()).isEqualTo("Title");
        assertThat(model.getDescription()).isEqualTo("Desc");
        assertThat(model.isCompleted()).isTrue();
        assertThat(model.getCreatedAt()).isEqualTo(now);
        assertThat(model.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toModel_nullEntity_returnsNull() {
        assertThat(mapper.toModel((TodoEntity) null)).isNull();
    }

    @Test
    void toEntity_fromModel_mapsAllFields() {
        OffsetDateTime now = OffsetDateTime.now();
        Todo model = new Todo("id-1", "Title", "Desc", true, now, now);

        TodoEntity entity = mapper.toEntity(model);

        assertThat(entity.getId()).isEqualTo("id-1");
        assertThat(entity.getTitle()).isEqualTo("Title");
        assertThat(entity.getDescription()).isEqualTo("Desc");
        assertThat(entity.isCompleted()).isTrue();
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toEntity_nullModel_returnsNull() {
        assertThat(mapper.toEntity((Todo) null)).isNull();
    }

    @Test
    void toModel_fromRequest_mapsFields() {
        TodoRequest request = new TodoRequest();
        request.setId("id-1");
        request.setTitle("Title");
        request.setDescription("Desc");
        request.setCompleted(true);

        Todo model = mapper.toModel(request);

        assertThat(model.getId()).isEqualTo("id-1");
        assertThat(model.getTitle()).isEqualTo("Title");
        assertThat(model.getDescription()).isEqualTo("Desc");
        assertThat(model.isCompleted()).isTrue();
        assertThat(model.getCreatedAt()).isNull();
        assertThat(model.getUpdatedAt()).isNull();
    }

    @Test
    void toModel_nullRequest_returnsNull() {
        assertThat(mapper.toModel((TodoRequest) null)).isNull();
    }

    @Test
    void toResponse_fromModel_mapsAllFields() {
        OffsetDateTime now = OffsetDateTime.now();
        Todo model = new Todo("id-1", "Title", "Desc", true, now, now);

        TodoResponse response = mapper.toResponse(model);

        assertThat(response.getId()).isEqualTo("id-1");
        assertThat(response.getTitle()).isEqualTo("Title");
        assertThat(response.getDescription()).isEqualTo("Desc");
        assertThat(response.isCompleted()).isTrue();
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void toResponse_nullModel_returnsNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }
}
