// src/main/java/com/example/worknest/repository/TaskRepository.java
package com.example.worknest.repository;

import com.example.worknest.model.Task;
import com.example.worknest.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAssignees_Id(Long userId);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByDueDateBeforeAndStatusNot(LocalDate refDate, TaskStatus status);

    long countByStatus(TaskStatus status);

    long countByDueDateBeforeAndStatusNot(LocalDate refDate, TaskStatus status);

    // 👇 Custom query to exclude deleted tasks
    List<Task> findByDeletedFalse();
}
