package com.example.worknest.controller;

import com.example.worknest.model.Task;
import com.example.worknest.model.TaskStatus;
import com.example.worknest.model.User;
import com.example.worknest.service.TaskCommentService;
import com.example.worknest.service.TaskService;
import com.example.worknest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final TaskService taskService;
    private final TaskCommentService commentService;

    /** Dashboard (only stats) */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("countPending", taskService.countByStatus(TaskStatus.PENDING));
        model.addAttribute("countInProgress", taskService.countByStatus(TaskStatus.IN_PROGRESS));
        model.addAttribute("countCompleted", taskService.countByStatus(TaskStatus.COMPLETED));
        model.addAttribute("countDelayed", taskService.countDelayed(LocalDate.now()));
        return "admin-dashboard"; // templates/admin-dashboard.html
    }

    /** Manage Task (form only) */
    @GetMapping("/manage-task")
    public String manageTask(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin-manage-task"; // templates/admin-manage-task.html
    }

    /** Create Task action */
    @PostMapping("/tasks")
    public String createTask(@RequestParam String title,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) List<Long> assigneeIds,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        taskService.create(title, description, assigneeIds, startDate, dueDate);
        return "redirect:/admin/tasks";
    }

    /** List all tasks */
    @GetMapping("/tasks")
    public String tasks(Model model) {
        List<Task> tasks = taskService.findAll();
        List<AdminTaskRow> rows = new ArrayList<>();
        for (Task t : tasks) {
            if (t.getAssignees().isEmpty()) {
                rows.add(new AdminTaskRow(t, null));
            } else {
                t.getAssignees().forEach(u -> rows.add(new AdminTaskRow(t, u)));
            }
        }
        model.addAttribute("rows", rows);
        model.addAttribute("today", LocalDate.now());
        return "admin-tasks"; // templates/admin-tasks.html
    }

    /** Task Details (view + comments) */
    @GetMapping("/tasks/{id}")
    public String taskDetails(@PathVariable Long id, Model model) {
        Task task = taskService.getById(id);
        model.addAttribute("task", task);
        model.addAttribute("comments", commentService.listByTask(id));
        return "admin-task-details"; // templates/admin-task-details.html
    }

    /** Update task status */
    @PostMapping("/tasks/{id}/status")
    public String updateTaskStatus(@PathVariable Long id,
                                   @RequestParam TaskStatus status) {
        taskService.updateStatus(id, status);
        return "redirect:/admin/tasks/" + id;
    }

    /** Delete Task */
    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id) {
        taskService.delete(id);
        return "redirect:/admin/tasks";
    }

    /** Manage User (form only) */
    @GetMapping("/manage-user")
    public String manageUser() {
        return "admin-manage-user"; // templates/admin-manage-user.html
    }

    /** Add User action */
    @PostMapping("/users")
    public String addUser(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String role) {
        userService.create(username, password, role);
        return "redirect:/admin/users";
    }

    /** List all users */
    @GetMapping("/users")
    public String users(Model model) {
        List<User> all = userService.findAll();
        long adminCount = all.stream().filter(u -> "ADMIN".equalsIgnoreCase(u.getRole())).count();
        long userCount  = all.stream().filter(u -> "USER".equalsIgnoreCase(u.getRole())).count();

        model.addAttribute("users", all);
        model.addAttribute("totalUsers", all.size());
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("userCount", userCount);

        return "admin-users"; // templates/admin-users.html
    }

    /** Delete User */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return "redirect:/admin/users";
    }

    // --- Helper for task rows ---
    public static class AdminTaskRow {
        private final Task task;
        private final User assignee;
        public AdminTaskRow(Task task, User assignee) {
            this.task = task;
            this.assignee = assignee;
        }
        public Task getTask() { return task; }
        public User getAssignee() { return assignee; }
    }
}
