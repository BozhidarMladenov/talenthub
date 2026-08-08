package com.softuni.statssvc.controller;

import com.softuni.statssvc.model.dto.StatRecordRequest;
import com.softuni.statssvc.model.dto.StatResponse;
import com.softuni.statssvc.service.JobStatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final JobStatService jobStatService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<StatResponse>>> getAll() {
        List<EntityModel<StatResponse>> stats = jobStatService.findAll().stream()
                .map(this::toModel)
                .toList();
        Link selfLink = linkTo(methodOn(StatsController.class).getAll()).withSelfRel();
        return ResponseEntity.ok(CollectionModel.of(stats, selfLink));
    }

    @GetMapping("/{category}")
    public ResponseEntity<EntityModel<StatResponse>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(toModel(jobStatService.findByCategory(category)));
    }

    @PostMapping
    public ResponseEntity<EntityModel<StatResponse>> record(@Valid @RequestBody StatRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(jobStatService.record(request)));
    }

    @PutMapping("/{category}")
    public ResponseEntity<EntityModel<StatResponse>> update(@PathVariable String category,
                                                            @Valid @RequestBody StatRecordRequest request) {
        return ResponseEntity.ok(toModel(jobStatService.update(category, request)));
    }

    @DeleteMapping("/{category}")
    public ResponseEntity<Void> delete(@PathVariable String category) {
        jobStatService.delete(category);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<StatResponse> toModel(StatResponse stat) {
        return EntityModel.of(stat,
                linkTo(methodOn(StatsController.class).getByCategory(stat.getCategory())).withSelfRel(),
                linkTo(methodOn(StatsController.class).getAll()).withRel("all-stats"));
    }
}
