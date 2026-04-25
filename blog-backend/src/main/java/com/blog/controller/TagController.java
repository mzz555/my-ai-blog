package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.tag.TagRequest;
import com.blog.entity.Tag;
import com.blog.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public Result<List<Tag>> list() {
        return Result.success(tagService.listAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('tag:manage')")
    public Result<Tag> create(@Valid @RequestBody TagRequest req) {
        return Result.success(tagService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('tag:manage')")
    public Result<Tag> update(@PathVariable Long id, @Valid @RequestBody TagRequest req) {
        Tag tag = tagService.getById(id);
        if (tag == null) return Result.error(404, "标签不存在");
        tag.setName(req.getName());
        if (req.getSlug() != null) tag.setSlug(req.getSlug());
        tagService.updateById(tag);
        return Result.success(tag);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('tag:manage')")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.success();
    }
}
