package hei.school.todos.rest.exception;

import java.time.OffsetDateTime;
import java.util.List;

public class ApiError {

    private OffsetDateTime timestamp = OffsetDateTime.now();
    private int status;
    private String message;
    private List<String> errors;

    public ApiError(int status, String message, List<String> errors) {
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    public OffsetDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public List<String> getErrors() { return errors; }
}