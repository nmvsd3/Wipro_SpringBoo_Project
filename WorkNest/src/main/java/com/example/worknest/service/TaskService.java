// src/main/java/com/example/worknest/service/TaskService.java
package com.example.worknest.service;

import com.example.worknest.model.Task;
import com.example.worknest.model.TaskStatus;
import com.example.worknest.model.User;
import com.example.worknest.repository.TaskRepository;
import com.example.worknest.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepo;
    private final UserRepository userRepo;

    // 👇 changed: multiple assignees
    public Task create(String title, String description, List<Long> assigneeIds,
                       LocalDate startDate, LocalDate dueDate) {

        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title is required");
        if (startDate == null || dueDate == null) throw new IllegalArgumentException("Dates are required");
        if (dueDate.isBefore(startDate)) throw new IllegalArgumentException("Due date cannot be before start date");
        if (assigneeIds == null || assigneeIds.isEmpty()) throw new IllegalArgumentException("At least one assignee is required");

        Set<User> assignees = new HashSet<>(userRepo.findAllById(assigneeIds));
        if (assignees.isEmpty()) throw new IllegalArgumentException("No valid assignees found");

        Task task = Task.builder()
                .title(title)
                .description(description)
                .startDate(startDate)
                .dueDate(dueDate)
                .assignees(assignees)
                .build();

        return taskRepo.save(task);
    }

    public Task updateStatus(Long taskId, TaskStatus status) {
        Task t = taskRepo.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found"));
        if (status != null) {
            t.setStatus(status);
            taskRepo.save(t);
        }
        return t;
    }

    public void delete(Long taskId) { taskRepo.deleteById(taskId); }

    public Task getById(Long taskId) {
        return taskRepo.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found"));
    }

    // 👇 changed: membership-based query
    public List<Task> findByAssignee(Long userId) { return taskRepo.findByAssignees_Id(userId); }

    public List<Task> findByStatus(TaskStatus status) { return taskRepo.findByStatus(status); }

    public List<Task> findDelayed(LocalDate refDate) {
        return taskRepo.findByDueDateBeforeAndStatusNot(refDate, TaskStatus.COMPLETED);
    }
    public List<Task> findAll() { 
        return taskRepo.findAll(); 
    }
    public long countByStatus(TaskStatus status) { return taskRepo.countByStatus(status); }
 // TaskService.java
   


    public long countDelayed(LocalDate refDate) {
        return taskRepo.countByDueDateBeforeAndStatusNot(refDate, TaskStatus.COMPLETED);
    }
}
