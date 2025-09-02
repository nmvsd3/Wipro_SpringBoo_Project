package com.example.worknest.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.worknest.model.Task;
import com.example.worknest.model.TaskReassignment;
import com.example.worknest.model.User;
import com.example.worknest.repository.TaskReassignmentRepository;
import com.example.worknest.repository.TaskRepository;
import com.example.worknest.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskReassignmentService {

    private final TaskReassignmentRepository repo;
    private final TaskRepository taskRepo;
    private final UserRepository userRepo;

    public TaskReassignment reassignTask(Long taskId, Long fromUserId, Long toUserId, String comment) {
        Task task = taskRepo.findById(taskId).orElseThrow();
        User fromUser = userRepo.findById(fromUserId).orElseThrow();
        User toUser = userRepo.findById(toUserId).orElseThrow();

        TaskReassignment reassignment = TaskReassignment.builder()
                .task(task)
                .fromUser(fromUser)
                .toUser(toUser)
                .comment(comment)
                .reassignedAt(LocalDateTime.now())
                .build();

        return repo.save(reassignment);
    }

    public List<TaskReassignment> getByTask(Long taskId) {
        return repo.findByTask_Id(taskId);
    }
}
