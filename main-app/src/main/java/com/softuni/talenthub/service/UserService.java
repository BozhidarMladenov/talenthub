package com.softuni.talenthub.service;

import com.softuni.talenthub.exception.InvalidOperationException;
import com.softuni.talenthub.exception.ResourceNotFoundException;
import com.softuni.talenthub.model.dto.ProfileUpdateRequest;
import com.softuni.talenthub.model.dto.RegisterRequest;
import com.softuni.talenthub.model.entity.User;
import com.softuni.talenthub.model.enums.UserRole;
import com.softuni.talenthub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest request) {
        log.info("Registering new user with username: {}", request.getUsername());
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(UUID userId, ProfileUpdateRequest request) {
        log.info("Updating profile for user id: {}", userId);
        User user = findById(userId);
        user.setFullName(request.getFullName());
        user.setBio(request.getBio());
        return userRepository.save(user);
    }

    @Transactional
    public User changeRole(UUID userId, UserRole newRole) {
        log.info("Admin changing role of user {} to {}", userId, newRole);
        User user = findById(userId);
        if (user.getRole() == UserRole.ADMIN) {
            throw new InvalidOperationException("Cannot change role of another admin.");
        }
        user.setRole(newRole);
        return userRepository.save(user);
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public List<User> findAllNonAdmins() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != UserRole.ADMIN)
                .toList();
    }

    public boolean usernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailTaken(String email) {
        return userRepository.existsByEmail(email);
    }
}
