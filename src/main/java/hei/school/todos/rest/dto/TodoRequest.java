package hei.school.todos.rest.dto;

import hei.school.todos.rest.validation.ValidUUID;
import jakarta.validation.constraints.NotBlank;


public class TodoRequest {

    @ValidUUID
    private String id;

    @NotBlank(message = "title est obligatoire")
    private String title;

    private String description;

    private boolean completed;

    public TodoRequest() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
