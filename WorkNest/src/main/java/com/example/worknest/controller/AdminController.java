package com.example.worknest.controller;

import com.example.worknest.model.Task;
import com.example.worknest.model.TaskStatus;
import com.example.worknest.model.User;
import com.example.worknest.service.TaskActivityService;
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
    private final TaskActivityService activityService;

    /** Dashboard (stats only) */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("countPending", taskService.countByStatus(TaskStatus.PENDING));
        model.addAttribute("countInProgress", taskService.countByStatus(TaskStatus.IN_PROGRESS));
        model.addAttribute("countCompleted", taskService.countByStatus(TaskStatus.COMPLETED));
        model.addAttribute("countDelayed", taskService.countDelayed(LocalDate.now()));
        return "admin-dashboard";
    }

    /** Manage Task (form only) */
    @GetMapping("/manage-task")
    public String manageTask(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin-manage-task";
    }

    /** Create Task */
    @PostMapping("/tasks")
    public String createTask(@RequestParam String title,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) List<Long> assigneeIds,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        Task task = taskService.create(title, description, assigneeIds, startDate, dueDate);

        // log creation
        activityService.log(task, null, "Task Created",
                "Task created and assigned to " + (assigneeIds != null ? assigneeIds.size() : 0) + " users");

        return "redirect:/admin/tasks";
    }

    /** List all tasks (active only, not deleted) */
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
        return "admin-tasks";
    }

    /** Task Details (view + comments + activities) */
    @GetMapping("/tasks/{id}")
    public String taskDetails(@PathVariable Long id, Model model) {
        Task task = taskService.getById(id);

        model.addAttribute("task", task);
        model.addAttribute("comments", commentService.listByTask(id));
        model.addAttribute("activities", activityService.getForTask(id));

        return "admin-task-details";
    }

    /** Update Task Status */
    @PostMapping("/tasks/{id}/status")
    public String updateTaskStatus(@PathVariable Long id,
                                   @RequestParam TaskStatus status) {
        Task task = taskService.updateStatus(id, status);

        activityService.log(task, null, "Status Updated", "Changed to " + status.name());

        return "redirect:/admin/tasks/" + id;
    }

    /** Freeze/Unfreeze Task */
    @PostMapping("/tasks/{id}/freeze")
    public String toggleFreeze(@PathVariable Long id) {
        Task task = taskService.getById(id);
        task.setFrozen(!task.isFrozen()); // toggle state
        taskService.save(task);

        // log freeze/unfreeze
        String action = task.isFrozen() ? "Task Frozen" : "Task Unfrozen";
        String details = task.isFrozen() ? "Admin has frozen this task" : "Admin has unfrozen this task";
        activityService.log(task, null, action, details);

        return "redirect:/admin/tasks/" + id;
    }

    /** Soft Delete Task */
    @PostMapping("/tasks/{id}/delete")
    public String softDeleteTask(@PathVariable Long id) {
        Task task = taskService.getById(id);

        taskService.delete(id); // 👉 this now marks it as deleted (soft delete)
        activityService.log(task, null, "Task Soft Deleted", "Task marked as deleted but kept in DB");

        return "redirect:/admin/tasks";
    }

    /** Restore Task (optional recycle bin feature) */
    @PostMapping("/tasks/{id}/restore")
    public String restoreTask(@PathVariable Long id) {
        taskService.restore(id);
        Task task = taskService.getById(id);

        activityService.log(task, null, "Task Restored", "Task restored from soft delete");

        return "redirect:/admin/tasks";
    }

    /** Manage User (form only) */
    @GetMapping("/manage-user")
    public String manageUser() {
        return "admin-manage-user";
    }

    /** Add User */
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

        return "admin-users";
    }

    /** Delete User */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return "redirect:/admin/users";
    }

    //  Helper for task rows 
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
