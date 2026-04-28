package com.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.exception.BusinessException;
import com.blog.dto.tag.TagRequest;
import com.blog.dto.tag.TagVO;
import com.blog.entity.ArticleTag;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.entity.Tag;
import com.blog.service.TagService;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 标签服务实现
 *
 * @author blog
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final ArticleTagMapper articleTagMapper;
    private final Slugify slugify = Slugify.builder().build();

    @Override
    public List<TagVO> listAll() {
        return this.baseMapper.selectWithArticleCount();
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
        long count = articleTagMapper.selectCount(
                Wrappers.<ArticleTag>lambdaQuery().eq(ArticleTag::getTagId, id));
        if (count > 0) {
            throw new BusinessException(400, "该标签下还有 " + count + " 篇文章，请先移除文章后再删除");
        }
        this.removeById(id);
    }
}
