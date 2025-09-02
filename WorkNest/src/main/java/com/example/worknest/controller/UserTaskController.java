package com.example.worknest.controller;

import com.example.worknest.model.Task;
import com.example.worknest.model.TaskStatus;
import com.example.worknest.model.User;
import com.example.worknest.service.TaskActivityService;
import com.example.worknest.service.TaskCommentService;
import com.example.worknest.service.TaskService;
import com.example.worknest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
@PreAuthorize("hasRole('USER')")
public class UserTaskController {

    private final TaskService taskService;
    private final TaskCommentService commentService;
    private final UserService userService;
    private final TaskActivityService activityService;

    /** User dashboard: list my tasks */
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User me = userService.findByUsername(userDetails.getUsername()).orElseThrow();

        List<Task> myTasks = taskService.findByAssignee(me.getId());

        long countPending    = myTasks.stream().filter(t -> t.getStatus() == TaskStatus.PENDING).count();
        long countInProgress = myTasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long countCompleted  = myTasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long countDelayed    = myTasks.stream().filter(t ->
                t.getStatus() != TaskStatus.COMPLETED &&
                        t.getDueDate() != null &&
                        t.getDueDate().isBefore(LocalDate.now())
        ).count();

        model.addAttribute("me", me);
        model.addAttribute("tasks", myTasks);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("countPending", countPending);
        model.addAttribute("countInProgress", countInProgress);
        model.addAttribute("countCompleted", countCompleted);
        model.addAttribute("countDelayed", countDelayed);
        return "user-dashboard";
    }

    /** View a task I am assigned to */
    @GetMapping("/tasks/{id}")
    public String viewTask(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User me = userService.findByUsername(userDetails.getUsername()).orElseThrow();

        Task task = taskService.getById(id);
        boolean isAssignee = task.getAssignees().stream()
                .anyMatch(u -> u.getId().equals(me.getId()));
        if (!isAssignee) return "redirect:/user/dashboard";

        model.addAttribute("task", task);
        model.addAttribute("comments", commentService.listByTask(id));
        model.addAttribute("today", LocalDate.now());

        // Filter users → exclude admins and myself
        List<User> otherUsers = userService.findAll().stream()
                .filter(u -> !"ADMIN".equalsIgnoreCase(u.getRole())) // remove admins
                .filter(u -> !u.getId().equals(me.getId()))           // remove current user
                .toList();

        model.addAttribute("users", otherUsers);

        return "user-task-details";
    }

    /** Update my task status */
 // updateStatus method
    @PostMapping("/tasks/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam TaskStatus status,
                               @AuthenticationPrincipal UserDetails userDetails) {
        User me = userService.findByUsername(userDetails.getUsername()).orElseThrow();

        Task task = taskService.getById(id);
        boolean isAssignee = task.getAssignees().stream().anyMatch(u -> u.getId().equals(me.getId()));
        if (!isAssignee) return "redirect:/user/dashboard";

        //  Block if frozen
        if (task.isFrozen()) {
            return "redirect:/user/tasks/" + id + "?error=frozen";
        }

        if (status == TaskStatus.IN_PROGRESS || status == TaskStatus.COMPLETED || status == TaskStatus.PENDING) {
            taskService.updateStatus(id, status);
            activityService.log(task, me, "Status Update", "Marked as " + status.name());
        }
        return "redirect:/user/tasks/" + id;
    }


    /** Add a comment on my task */
    @PostMapping("/tasks/{id}/comments")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             @AuthenticationPrincipal UserDetails userDetails) {
        User me = userService.findByUsername(userDetails.getUsername()).orElseThrow();

        Task task = taskService.getById(id);
        boolean isAssignee = task.getAssignees().stream().anyMatch(u -> u.getId().equals(me.getId()));
        if (!isAssignee) return "redirect:/user/dashboard";

        commentService.add(id, me.getId(), content);
        activityService.log(task, me, "Comment", content);
        return "redirect:/user/tasks/" + id;
    }

    /** Re-assign task to other users (with a note) */
    @PostMapping("/tasks/{id}/assign")
    public String reassignTask(@PathVariable Long id,
                               @RequestParam List<Long> assigneeIds,
                               @RequestParam String note,
                               @AuthenticationPrincipal UserDetails userDetails) {
        User me = userService.findByUsername(userDetails.getUsername()).orElseThrow();

        Task task = taskService.getById(id);
        boolean isAssignee = task.getAssignees().stream()
                .anyMatch(u -> u.getId().equals(me.getId()));
        if (!isAssignee) return "redirect:/user/dashboard";

        // Ensure current user stays assigned
        Set<Long> finalAssignees = new HashSet<>(assigneeIds);
        finalAssignees.add(me.getId());

        taskService.assignUsers(id, List.copyOf(finalAssignees));

        // Resolve usernames instead of raw IDs
        List<User> assignedUsers = userService.findAllById(finalAssignees);
        String usernames = assignedUsers.stream()
                .map(User::getUsername)
                .collect(Collectors.joining(", "));

        // Log activity
        activityService.log(task, me,
                "Task Reassigned",
                me.getUsername() + " reassigned task to " + usernames +
                        (note != null && !note.isBlank() ? " with note: " + note : "")
        );

        // Save reassign note as a comment
        if (note != null && !note.isBlank()) {
            commentService.add(id, me.getId(), "[Reassign Note] " + note);
        }

        return "redirect:/user/tasks/" + id;
    }
}
