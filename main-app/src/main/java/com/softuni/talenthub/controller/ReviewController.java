package com.softuni.talenthub.controller;

import com.softuni.talenthub.config.CurrentUserProvider;
import com.softuni.talenthub.model.dto.ReviewRequest;
import com.softuni.talenthub.model.entity.User;
import com.softuni.talenthub.service.ReviewService;
import com.softuni.talenthub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/create/{freelancerId}")
    public String createForm(@PathVariable UUID freelancerId, Model model) {
        model.addAttribute("reviewRequest", new ReviewRequest());
        model.addAttribute("freelancer", userService.findById(freelancerId));
        return "review/create";
    }

    @PostMapping("/create/{freelancerId}")
    public String create(@PathVariable UUID freelancerId,
                         @Valid @ModelAttribute("reviewRequest") ReviewRequest request,
                         BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("freelancer", userService.findById(freelancerId));
            return "review/create";
        }
        User currentUser = currentUserProvider.getCurrentUser();
        reviewService.create(freelancerId, request, currentUser);
        return "redirect:/profile/" + freelancerId;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        UUID freelancerId = reviewService.findById(id).getFreelancer().getId();
        User currentUser = currentUserProvider.getCurrentUser();
        reviewService.delete(id, currentUser);
        return "redirect:/profile/" + freelancerId;
    }
}
