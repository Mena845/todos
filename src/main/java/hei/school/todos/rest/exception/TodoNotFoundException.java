package hei.school.todos.rest.exception;

public class TodoNotFoundException extends RuntimeException {

    public TodoNotFoundException(String id) {
        super("Todo introuvable pour id=" + id);
    }
}