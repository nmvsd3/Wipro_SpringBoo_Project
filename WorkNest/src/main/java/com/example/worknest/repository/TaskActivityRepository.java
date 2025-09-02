// src/main/java/com/example/worknest/repository/TaskActivityRepository.java
package com.example.worknest.repository;

import com.example.worknest.model.TaskActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskActivityRepository extends JpaRepository<TaskActivity, Long> {
    List<TaskActivity> findByTask_IdOrderByCreatedAtDesc(Long taskId);
}
