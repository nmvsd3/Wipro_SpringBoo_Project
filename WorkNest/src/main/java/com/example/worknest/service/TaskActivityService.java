
package com.example.worknest.service;

import com.example.worknest.model.Task;
import com.example.worknest.model.TaskActivity;
import com.example.worknest.model.User;
import com.example.worknest.repository.TaskActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskActivityService {

    private final TaskActivityRepository repo;

    /**
     * Log an activity for a task
     * @param task The task (must be a managed entity)
     * @param performedBy The user who performed the action (can be null = system)
     * @param action Short label like "Status Update", "Comment", "Reassign"
     * @param details Additional description/details
     */
    public void log(Task task, User performedBy, String action, String details) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null for activity logging");
        }

        TaskActivity activity = TaskActivity.builder()
                .task(task)
                .performedBy(performedBy) // can be null (system action)
                .action(action != null ? action : "Unknown Action")
                .details(details != null ? details : "")
                .createdAt(LocalDateTime.now())
                .build();

        repo.save(activity);
    }

    /**
     * Log an action performed by the system (no user)
     */
    public void logSystem(Task task, String action, String details) {
        log(task, null, action, details);
    }

    /**
     * Fetch activity log for a task (newest first)
     */
    public List<TaskActivity> getForTask(Long taskId) {
        return repo.findByTask_IdOrderByCreatedAtDesc(taskId);
    }
}
