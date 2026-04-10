package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.PageResult;
import com.blog.common.exception.NotFoundException;
import com.blog.dto.article.*;
import com.blog.entity.*;
import com.blog.entity.Article.ArticleStatus;
import com.blog.mapper.*;
import com.blog.service.ArticleService;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文章服务实现
 *
 * @author blog
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final Slugify slugify = Slugify.builder().build();

    @Override
    @Transactional
    public Article create(ArticleCreateRequest req, String authorUsername) {
        User author = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, authorUsername));
        if (author == null) throw new NotFoundException("用户不存在");

        Article article = new Article();
        article.setTitle(req.getTitle());
        article.setSlug(resolveSlug(req.getSlug(), req.getTitle()));
        article.setContent(req.getContent());
        article.setSummary(req.getSummary());
        article.setCoverImage(req.getCoverImage());
        article.setAuthorId(author.getId());
        article.setIsTop(req.getIsTop() != null ? req.getIsTop() : false);
        article.setAllowComment(req.getAllowComment() != null ? req.getAllowComment() : true);

        ArticleStatus status = "PUBLISHED".equals(req.getStatus())
                ? ArticleStatus.PUBLISHED : ArticleStatus.DRAFT;
        article.setStatus(status);
        if (status == ArticleStatus.PUBLISHED) {
            article.setPublishedAt(LocalDateTime.now());
        }
        if (req.getCategoryId() != null) {
            article.setCategoryId(req.getCategoryId());
        }

        this.save(article);

        if (req.getTagNames() != null && !req.getTagNames().isEmpty()) {
            saveArticleTags(article.getId(), req.getTagNames());
        }
        return article;
    }

    @Override
    @Transactional
    public Article update(Long id, ArticleUpdateRequest req) {
        Article article = this.getById(id);
        if (article == null) throw new NotFoundException("文章不存在");

        if (req.getTitle() != null) article.setTitle(req.getTitle());
        if (req.getSummary() != null) article.setSummary(req.getSummary());
        if (req.getContent() != null) article.setContent(req.getContent());
        if (req.getCoverImage() != null) article.setCoverImage(req.getCoverImage());
        if (req.getIsTop() != null) article.setIsTop(req.getIsTop());
        if (req.getAllowComment() != null) article.setAllowComment(req.getAllowComment());
        if (req.getCategoryId() != null) article.setCategoryId(req.getCategoryId());

        this.updateById(article);

        if (req.getTagNames() != null) {
            articleTagMapper.deleteByArticleId(id);
            saveArticleTags(id, req.getTagNames());
        }
        return article;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        articleTagMapper.deleteByArticleId(id);
        this.removeById(id);
    }

    @Override
    @Transactional
    public void togglePublish(Long id) {
        Article article = this.getById(id);
        if (article == null) throw new NotFoundException("文章不存在");
        if (article.getStatus() == ArticleStatus.DRAFT) {
            article.setStatus(ArticleStatus.PUBLISHED);
            if (article.getPublishedAt() == null) {
                article.setPublishedAt(LocalDateTime.now());
            }
        } else {
            article.setStatus(ArticleStatus.DRAFT);
        }
        this.updateById(article);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDetailResponse getBySlug(String slug) {
        Article article = this.baseMapper.selectBySlugAndStatus(slug, ArticleStatus.PUBLISHED.name());
        if (article == null) throw new NotFoundException("文章不存在");
        fillAssociations(List.of(article));
        return toDetailResponse(article);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ArticleListResponse> listPublished(int page, int size, Long categoryId, String tagSlug) {
        LambdaQueryWrapper<Article> wrapper = Wrappers.<Article>lambdaQuery()
                .eq(Article::getStatus, ArticleStatus.PUBLISHED)
                .eq(categoryId != null, Article::getCategoryId, categoryId)
                .orderByDesc(Article::getIsTop)
                .orderByDesc(Article::getPublishedAt);

        if (tagSlug != null) {
            List<Long> ids = this.baseMapper.selectIdsByTagSlug(tagSlug);
            if (ids.isEmpty()) return PageResult.of(List.of(), 0L, page, size);
            wrapper.in(Article::getId, ids);
        }

        Page<Article> pageData = this.page(new Page<>(page, size), wrapper);
        fillAssociations(pageData.getRecords());
        List<ArticleListResponse> list = pageData.getRecords().stream()
                .map(this::toListResponse)
                .toList();
        return PageResult.of(list, pageData.getTotal(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ArticleListResponse> listAll(int page, int size) {
        Page<Article> pageData = this.page(
                new Page<>(page, size),
                Wrappers.<Article>lambdaQuery().orderByDesc(Article::getCreatedAt)
        );
        fillAssociations(pageData.getRecords());
        List<ArticleListResponse> list = pageData.getRecords().stream()
                .map(this::toListResponse)
                .toList();
        return PageResult.of(list, pageData.getTotal(), page, size);
    }

    /**
     * 批量填充文章关联数据（作者、分类、标签），避免 N+1 查询
     *
     * @param articles 需要填充关联的文章列表
     */
    private void fillAssociations(List<Article> articles) {
        if (articles.isEmpty()) return;

        // 批量获取作者信息
        List<Long> authorIds = articles.stream()
                .map(Article::getAuthorId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, User> authorMap = authorIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(authorIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        // 批量获取分类信息
        List<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, Category> categoryMap = categoryIds.isEmpty() ? Map.of() :
                categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, c -> c));

        // 逐篇文章填充关联对象和标签
        articles.forEach(a -> {
            a.setAuthor(authorMap.get(a.getAuthorId()));
            a.setCategory(categoryMap.get(a.getCategoryId()));
            a.setTags(tagMapper.selectByArticleId(a.getId()));
        });
    }

    /**
     * 将文章实体转换为列表响应 DTO
     */
    private ArticleListResponse toListResponse(Article a) {
        ArticleListResponse r = new ArticleListResponse();
        r.setId(a.getId());
        r.setTitle(a.getTitle());
        r.setSlug(a.getSlug());
        r.setSummary(a.getSummary());
        r.setCoverImage(a.getCoverImage());
        r.setViewCount(a.getViewCount());
        r.setIsTop(a.getIsTop());
        r.setPublishedAt(a.getPublishedAt());
        if (a.getAuthor() != null) {
            r.setAuthorName(a.getAuthor().getUsername());
        }
        if (a.getCategory() != null) {
            r.setCategoryName(a.getCategory().getName());
        }
        if (a.getTags() != null) {
            r.setTagNames(a.getTags().stream().map(Tag::getName).toList());
        }
        return r;
    }

    /**
     * 将文章实体转换为详情响应 DTO
     */
    private ArticleDetailResponse toDetailResponse(Article a) {
        ArticleDetailResponse r = new ArticleDetailResponse();
        r.setId(a.getId());
        r.setTitle(a.getTitle());
        r.setSlug(a.getSlug());
        r.setSummary(a.getSummary());
        r.setContent(a.getContent());
        r.setCoverImage(a.getCoverImage());
        r.setViewCount(a.getViewCount());
        r.setIsTop(a.getIsTop());
        r.setAllowComment(a.getAllowComment());
        r.setPublishedAt(a.getPublishedAt());
        r.setCreatedAt(a.getCreatedAt());
        if (a.getAuthor() != null) {
            r.setAuthorName(a.getAuthor().getUsername());
            r.setAuthorAvatar(a.getAuthor().getAvatar());
        }
        if (a.getCategory() != null) {
            r.setCategoryName(a.getCategory().getName());
            r.setCategoryId(a.getCategory().getId());
        }
        if (a.getTags() != null) {
            r.setTagNames(a.getTags().stream().map(Tag::getName).toList());
        }
        return r;
    }

    /**
     * 查找或创建标签，并建立与文章的关联关系
     *
     * @param articleId 文章 ID
     * @param tagNames  标签名称列表
     */
    private void saveArticleTags(Long articleId, List<String> tagNames) {
        List<Tag> existing = tagMapper.selectByNames(tagNames);
        Set<String> existingNames = existing.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        // 创建不存在的标签
        List<Tag> newTags = tagNames.stream()
                .filter(n -> !existingNames.contains(n))
                .map(n -> {
                    Tag t = new Tag();
                    t.setName(n);
                    t.setSlug(slugify.slugify(n));
                    tagMapper.insert(t);
                    return t;
                })
                .toList();

        List<Tag> allTags = new ArrayList<>(existing);
        allTags.addAll(newTags);

        // 插入 article_tags 关联记录
        allTags.forEach(t -> {
            ArticleTag at = new ArticleTag(articleId, t.getId());
            articleTagMapper.insert(at);
        });
    }

    /**
     * 根据提供的 slug 或文章标题生成 URL slug
     */
    private String resolveSlug(String slug, String title) {
        return (slug != null && !slug.isBlank()) ? slug : slugify.slugify(title);
    }
}
