package com.softuni.talenthub.controller;

import com.softuni.talenthub.model.enums.UserRole;
import com.softuni.talenthub.service.PermissionService;
import com.softuni.talenthub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final PermissionService permissionService;

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllNonAdmins());
        model.addAttribute("roles", new UserRole[]{UserRole.FREELANCER, UserRole.CLIENT});
        model.addAttribute("allPermissions", permissionService.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String changeRole(@PathVariable UUID id, @RequestParam UserRole role) {
        userService.changeRole(id, role);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/permissions/grant")
    public String grantPermission(@PathVariable UUID userId, @RequestParam UUID permissionId) {
        permissionService.grantPermission(userId, permissionId);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/permissions/revoke")
    public String revokePermission(@PathVariable UUID userId, @RequestParam UUID permissionId) {
        permissionService.revokePermission(userId, permissionId);
        return "redirect:/admin/users";
    }
}
