package com.example.worknest.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_comments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskComment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=2000)
    private String content;

    @Column(nullable=false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(); // ✅ keep default when using builder

    @ManyToOne(optional=false)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(optional=false)
    @JoinColumn(name = "author_id")
    private User author;

    // (Optional extra safety)
    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
