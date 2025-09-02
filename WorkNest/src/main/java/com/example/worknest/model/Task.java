// src/main/java/com/example/worknest/model/Task.java
package com.example.worknest.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//src/main/java/com/example/worknest/model/Task.java
@Entity
@Table(name = "tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Task {

 @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Column(nullable=false)
 private String title;

 @Column(length=2000)
 private String description;

 @Enumerated(EnumType.STRING)
 @Column(nullable=false)
 @Builder.Default
 private TaskStatus status = TaskStatus.PENDING;

 @Column(nullable=false)
 private LocalDate startDate;

 @Column(nullable=false)
 private LocalDate dueDate;

 @ManyToMany
 @JoinTable(
     name = "task_assignees",
     joinColumns = @JoinColumn(name = "task_id"),
     inverseJoinColumns = @JoinColumn(name = "user_id")
 )
 @Builder.Default
 private Set<User> assignees = new HashSet<>();

 @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
 @Builder.Default
 private List<TaskComment> comments = new ArrayList<>();

 //  NEW field for Freeze
 @Builder.Default
 private boolean frozen = false;

 //  NEW field for Soft Delete
 @Builder.Default
 private boolean deleted = false;
}
