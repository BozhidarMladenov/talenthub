package com.softuni.talenthub.controller;

import com.softuni.talenthub.config.CurrentUserProvider;
import com.softuni.talenthub.model.dto.JobPostRequest;
import com.softuni.talenthub.model.entity.JobPost;
import com.softuni.talenthub.model.entity.User;
import com.softuni.talenthub.model.enums.JobCategory;
import com.softuni.talenthub.model.enums.JobStatus;
import com.softuni.talenthub.service.ApplicationService;
import com.softuni.talenthub.service.JobPostService;
import com.softuni.talenthub.service.StatsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.softuni.talenthub.service.CurrencyService;

import java.util.UUID;

@Controller
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobPostService jobPostService;
    private final ApplicationService applicationService;
    private final StatsService statsService;
    private final CurrencyService currencyService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("jobs", jobPostService.findAllOpen());
        model.addAttribute("stats", statsService.getAllStats());
        return "job/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable UUID id, Model model) {
        JobPost job = jobPostService.findById(id);
        model.addAttribute("job", job);
        model.addAttribute("applications", applicationService.findAllByJobPost(job));
        model.addAttribute("budgetInCurrencies", currencyService.convertBudget(job.getBudget()));
        return "job/details";
    }

    @GetMapping("/mine")
    public String myJobs(Model model) {
        User currentUser = currentUserProvider.getCurrentUser();
        model.addAttribute("jobs", jobPostService.findAllByClient(currentUser));
        return "job/mine";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("jobPostRequest", new JobPostRequest());
        model.addAttribute("categories", JobCategory.values());
        return "job/create";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("jobPostRequest") JobPostRequest request,
                         BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", JobCategory.values());
            return "job/create";
        }
        User currentUser = currentUserProvider.getCurrentUser();
        JobPost created = jobPostService.create(request, currentUser);
        return "redirect:/jobs/" + created.getId();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        JobPost job = jobPostService.findById(id);
        JobPostRequest form = new JobPostRequest();
        form.setTitle(job.getTitle());
        form.setDescription(job.getDescription());
        form.setCategory(job.getCategory());
        form.setBudget(job.getBudget());
        model.addAttribute("jobPostRequest", form);
        model.addAttribute("categories", JobCategory.values());
        model.addAttribute("jobId", id);
        return "job/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable UUID id,
                       @Valid @ModelAttribute("jobPostRequest") JobPostRequest request,
                       BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", JobCategory.values());
            model.addAttribute("jobId", id);
            return "job/edit";
        }
        User currentUser = currentUserProvider.getCurrentUser();
        jobPostService.update(id, request, currentUser);
        return "redirect:/jobs/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        User currentUser = currentUserProvider.getCurrentUser();
        jobPostService.delete(id, currentUser);
        return "redirect:/jobs/mine";
    }

    @PostMapping("/{id}/close")
    public String close(@PathVariable UUID id) {
        User currentUser = currentUserProvider.getCurrentUser();
        jobPostService.changeStatus(id, JobStatus.CLOSED, currentUser);
        return "redirect:/jobs/" + id;
    }

    @PostMapping("/{id}/fill")
    public String fill(@PathVariable UUID id) {
        User currentUser = currentUserProvider.getCurrentUser();
        jobPostService.changeStatus(id, JobStatus.FILLED, currentUser);
        return "redirect:/jobs/" + id;
    }
}
