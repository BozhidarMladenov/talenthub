package com.softuni.talenthub.controller;

import com.softuni.talenthub.config.AppUserPrincipal;
import com.softuni.talenthub.config.CurrentUserProvider;
import com.softuni.talenthub.model.dto.ProfileUpdateRequest;
import com.softuni.talenthub.model.entity.User;
import com.softuni.talenthub.service.ReviewService;
import com.softuni.talenthub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final ReviewService reviewService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public String viewOwnProfile(Model model) {
        User user = currentUserProvider.getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("reviews", reviewService.findAllForFreelancer(user));
        return "auth/profile";
    }

    @GetMapping("/{id}")
    public String viewProfile(@PathVariable UUID id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("reviews", reviewService.findAllForFreelancer(user));
        return "auth/profile";
    }

    @GetMapping("/edit")
    public String editForm(Model model) {
        User user = currentUserProvider.getCurrentUser();
        ProfileUpdateRequest form = new ProfileUpdateRequest();
        form.setFullName(user.getFullName());
        form.setBio(user.getBio());
        model.addAttribute("profileUpdateRequest", form);
        return "auth/edit-profile";
    }

    @PostMapping("/edit")
    public String edit(@Valid @ModelAttribute("profileUpdateRequest") ProfileUpdateRequest request,
                       BindingResult bindingResult,
                       @AuthenticationPrincipal AppUserPrincipal principal) {
        if (bindingResult.hasErrors()) {
            return "auth/edit-profile";
        }
        userService.updateProfile(principal.getId(), request);
        return "redirect:/profile";
    }
}
