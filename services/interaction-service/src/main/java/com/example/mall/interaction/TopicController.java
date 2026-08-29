package com.example.mall.interaction;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
public class TopicController {
    private final TopicService service;

    public TopicController(TopicService service) { this.service = service; }

    @GetMapping public List<TopicService.TopicView> list() { return service.list(); }
    @GetMapping("/{id}") public TopicService.TopicView find(@PathVariable long id) { return service.find(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TopicService.TopicView create(@Valid @RequestBody CreateTopic request) {
        return service.create(request.creatorId(), request.title());
    }

    public record CreateTopic(@Positive long creatorId, @NotBlank @Size(max = 200) String title) {}
}
