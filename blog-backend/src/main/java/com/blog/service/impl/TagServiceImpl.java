package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.dto.tag.TagRequest;
import com.blog.entity.Tag;
import com.blog.mapper.TagMapper;
import com.blog.service.TagService;
import com.github.slugify.Slugify;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 标签服务实现
 *
 * @author blog
 * @since 1.0.0
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final Slugify slugify = Slugify.builder().build();

    @Override
    public List<Tag> listAll() {
        return this.list();
    }

    @Override
    public Tag create(TagRequest req) {
        String slug = (req.getSlug() != null && !req.getSlug().isBlank())
                ? req.getSlug() : slugify.slugify(req.getName());
        Tag t = new Tag();
        t.setName(req.getName());
        t.setSlug(slug);
        this.save(t);
        return t;
    }

    @Override
    public void delete(Long id) {
        this.removeById(id);
    }
}
