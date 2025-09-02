package com.example.worknest.service;

import com.example.worknest.model.Task;
import com.example.worknest.model.TaskComment;
import com.example.worknest.model.User;
import com.example.worknest.repository.TaskCommentRepository;
import com.example.worknest.repository.TaskRepository;
import com.example.worknest.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskCommentService {

    private final TaskCommentRepository commentRepo;
    private final TaskRepository taskRepo;
    private final UserRepository userRepo;

    public TaskComment add(Long taskId, Long authorUserId, String content) {
        if (content == null || content.isBlank()) throw new IllegalArgumentException("Comment cannot be empty");
        Task task = taskRepo.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found"));
        User author = userRepo.findById(authorUserId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        TaskComment c = TaskComment.builder()
                .task(task)
                .author(author)
                .content(content)
                .build();

        return commentRepo.save(c);
    }

    public List<TaskComment> listByTask(Long taskId) {
        return commentRepo.findByTaskIdOrderByCreatedAtAsc(taskId);
    }
}
