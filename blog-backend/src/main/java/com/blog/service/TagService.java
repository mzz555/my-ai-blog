package com.blog.service;

import com.blog.dto.tag.TagRequest;
import com.blog.entity.Tag;
import java.util.List;

public interface TagService {
    List<Tag> listAll();
    Tag create(TagRequest request);
    void delete(Long id);
}
