package hei.school.todos.repository;

import hei.school.todos.entity.TodoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoRepository extends JpaRepository<TodoEntity, String> {

    List<TodoEntity> findByCompleted(boolean completed);
}