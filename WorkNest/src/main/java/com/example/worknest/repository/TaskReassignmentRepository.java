package com.example.worknest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.worknest.model.TaskReassignment;

public interface TaskReassignmentRepository extends JpaRepository<TaskReassignment, Long> {
    List<TaskReassignment> findByTask_Id(Long taskId);
}
