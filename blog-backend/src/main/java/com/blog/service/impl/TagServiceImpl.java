package com.blog.service.impl;

import com.blog.dto.tag.TagRequest;
import com.blog.entity.Tag;
import com.blog.repository.TagRepository;
import com.blog.service.TagService;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final Slugify slugify = Slugify.builder().build();

    @Override
    public List<Tag> listAll() {
        return tagRepository.findAll();
    }

    @Override
    public Tag create(TagRequest req) {
        String slug = (req.getSlug() != null && !req.getSlug().isBlank())
                ? req.getSlug() : slugify.slugify(req.getName());
        Tag t = new Tag();
        t.setName(req.getName());
        t.setSlug(slug);
        return tagRepository.save(t);
    }

    @Override
    public void delete(Long id) {
        tagRepository.deleteById(id);
    }
}
