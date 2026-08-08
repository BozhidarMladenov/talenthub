package com.softuni.talenthub.service;

import com.softuni.talenthub.exception.ResourceNotFoundException;
import com.softuni.talenthub.model.entity.Permission;
import com.softuni.talenthub.model.entity.User;
import com.softuni.talenthub.repository.PermissionRepository;
import com.softuni.talenthub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    @Transactional
    public void grantPermission(UUID userId, UUID permissionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionId));
        user.getPermissions().add(permission);
        userRepository.save(user);
        log.info("Granted permission '{}' to user '{}'", permission.getName(), user.getUsername());
    }

    @Transactional
    public void revokePermission(UUID userId, UUID permissionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionId));
        user.getPermissions().remove(permission);
        userRepository.save(user);
        log.info("Revoked permission '{}' from user '{}'", permission.getName(), user.getUsername());
    }
}
