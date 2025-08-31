package com.example.worknest.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_assignments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"task_id","assignee_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskAssignment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(optional = false) @JoinColumn(name = "assignee_id")
    private User assignee;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    // Optional: per-assignee dates (or inherit from task if you prefer)
    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate dueDate;
}
