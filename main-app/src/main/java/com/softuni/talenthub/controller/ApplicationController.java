package com.softuni.talenthub.controller;

import com.softuni.talenthub.config.CurrentUserProvider;
import com.softuni.talenthub.model.dto.ApplicationRequest;
import com.softuni.talenthub.model.entity.User;
import com.softuni.talenthub.model.enums.ApplicationStatus;
import com.softuni.talenthub.service.ApplicationService;
import com.softuni.talenthub.service.JobPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final JobPostService jobPostService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/mine")
    public String myApplications(Model model) {
        User currentUser = currentUserProvider.getCurrentUser();
        model.addAttribute("applications", applicationService.findAllByFreelancer(currentUser));
        return "application/mine";
    }

    @GetMapping("/apply/{jobId}")
    public String applyForm(@PathVariable UUID jobId, Model model) {
        model.addAttribute("applicationRequest", new ApplicationRequest());
        model.addAttribute("job", jobPostService.findById(jobId));
        return "application/apply";
    }

    @PostMapping("/apply/{jobId}")
    public String apply(@PathVariable UUID jobId,
                        @Valid @ModelAttribute("applicationRequest") ApplicationRequest request,
                        BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("job", jobPostService.findById(jobId));
            return "application/apply";
        }
        User currentUser = currentUserProvider.getCurrentUser();
        applicationService.apply(jobId, request, currentUser);
        return "redirect:/applications/mine";
    }

    @PostMapping("/{id}/withdraw")
    public String withdraw(@PathVariable UUID id) {
        User currentUser = currentUserProvider.getCurrentUser();
        applicationService.withdraw(id, currentUser);
        return "redirect:/applications/mine";
    }

    @PostMapping("/{id}/accept")
    public String accept(@PathVariable UUID id) {
        User currentUser = currentUserProvider.getCurrentUser();
        applicationService.decide(id, ApplicationStatus.ACCEPTED, currentUser);
        return "redirect:/jobs/mine";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable UUID id) {
        User currentUser = currentUserProvider.getCurrentUser();
        applicationService.decide(id, ApplicationStatus.REJECTED, currentUser);
        return "redirect:/jobs/mine";
    }
}
