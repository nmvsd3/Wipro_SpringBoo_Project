package com.example.worknest.controller;

import com.example.worknest.model.Task;
import com.example.worknest.model.TaskStatus;
import com.example.worknest.model.User;
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
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
@PreAuthorize("hasRole('USER')") // 👈 restrict all endpoints in this controller to USER
public class UserTaskController {

    private final TaskService taskService;
    private final TaskCommentService commentService;
    private final UserService userService;

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
        boolean isAssignee = task.getAssignees().stream().anyMatch(u -> u.getId().equals(me.getId()));
        if (!isAssignee) return "redirect:/user/dashboard";

        model.addAttribute("task", task);
        model.addAttribute("comments", commentService.listByTask(id));
        model.addAttribute("today", LocalDate.now());
        return "user-task-details"; // must exist in templates
    }

    /** Update my task status */
    @PostMapping("/tasks/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam TaskStatus status,
                               @AuthenticationPrincipal UserDetails userDetails) {
        User me = userService.findByUsername(userDetails.getUsername()).orElseThrow();

        Task task = taskService.getById(id);
        boolean isAssignee = task.getAssignees().stream().anyMatch(u -> u.getId().equals(me.getId()));
        if (!isAssignee) return "redirect:/user/dashboard";

        if (status == TaskStatus.IN_PROGRESS || status == TaskStatus.COMPLETED || status == TaskStatus.PENDING) {
            taskService.updateStatus(id, status);
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
        return "redirect:/user/tasks/" + id;
    }
}
