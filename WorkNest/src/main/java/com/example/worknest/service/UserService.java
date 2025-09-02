package com.example.worknest.service;

import com.example.worknest.model.User;
import com.example.worknest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return repo.findAll();
    }

    //  Needed for UserTaskController
    public Optional<User> findByUsername(String username) {
        return repo.findByUsername(username);
    }

    /**  Create a user safely (prevents duplicate usernames) */
    public void create(String username, String password, String role) {
        // 🔎 Check if username already exists
        if (repo.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // encode password securely
        user.setRole(role.toUpperCase()); // normalize role (e.g., "ADMIN", "USER")
        repo.save(user);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public List<User> findAllById(Iterable<Long> ids) {
        return repo.findAllById(ids);
    }
}
