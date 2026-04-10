package com.blog.service.impl;

import com.blog.common.PageResult;
import com.blog.dto.article.*;
import com.blog.entity.*;
import com.blog.entity.Article.ArticleStatus;
import com.blog.repository.*;
import com.blog.service.ArticleService;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final Slugify slugify = Slugify.builder().build();

    @Override
    @Transactional
    public Article create(ArticleCreateRequest req, String authorUsername) {
        User author = userRepository.findByUsername(authorUsername)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Article article = new Article();
        article.setTitle(req.getTitle());
        article.setSlug(resolveSlug(req.getSlug(), req.getTitle()));
        article.setContent(req.getContent());
        article.setSummary(req.getSummary());
        article.setCoverImage(req.getCoverImage());
        article.setAuthor(author);
        article.setIsTop(req.getIsTop() != null ? req.getIsTop() : false);
        article.setAllowComment(req.getAllowComment() != null ? req.getAllowComment() : true);

        ArticleStatus status = "PUBLISHED".equals(req.getStatus())
                ? ArticleStatus.PUBLISHED : ArticleStatus.DRAFT;
        article.setStatus(status);
        if (status == ArticleStatus.PUBLISHED) article.setPublishedAt(LocalDateTime.now());

        if (req.getCategoryId() != null)
            categoryRepository.findById(req.getCategoryId()).ifPresent(article::setCategory);
        if (req.getTagNames() != null && !req.getTagNames().isEmpty())
            article.setTags(resolveTags(req.getTagNames()));

        return articleRepository.save(article);
    }

    @Override
    @Transactional
    public Article update(Long id, ArticleUpdateRequest req) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        if (req.getTitle() != null) article.setTitle(req.getTitle());
        if (req.getSummary() != null) article.setSummary(req.getSummary());
        if (req.getContent() != null) article.setContent(req.getContent());
        if (req.getCoverImage() != null) article.setCoverImage(req.getCoverImage());
        if (req.getIsTop() != null) article.setIsTop(req.getIsTop());
        if (req.getAllowComment() != null) article.setAllowComment(req.getAllowComment());
        if (req.getCategoryId() != null)
            categoryRepository.findById(req.getCategoryId()).ifPresent(article::setCategory);
        if (req.getTagNames() != null)
            article.setTags(resolveTags(req.getTagNames()));
        return articleRepository.save(article);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        articleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void togglePublish(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        if (article.getStatus() == ArticleStatus.DRAFT) {
            article.setStatus(ArticleStatus.PUBLISHED);
            if (article.getPublishedAt() == null) article.setPublishedAt(LocalDateTime.now());
        } else {
            article.setStatus(ArticleStatus.DRAFT);
        }
        articleRepository.save(article);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDetailResponse getBySlug(String slug) {
        Article a = articleRepository.findBySlugAndStatus(slug, ArticleStatus.PUBLISHED)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        return toDetailResponse(a);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ArticleListResponse> listPublished(int page, int size, Long categoryId, String tagSlug) {
        Pageable pageable = PageRequest.of(page - 1, size,
                Sort.by("isTop").descending().and(Sort.by("publishedAt").descending()));
        Page<Article> pageData;
        if (tagSlug != null) {
            pageData = articleRepository.findPublishedByTagSlug(tagSlug, pageable);
        } else if (categoryId != null) {
            pageData = articleRepository.findByStatusAndCategoryId(ArticleStatus.PUBLISHED, categoryId, pageable);
        } else {
            pageData = articleRepository.findByStatus(ArticleStatus.PUBLISHED, pageable);
        }
        return PageResult.of(pageData.getContent().stream().map(this::toListResponse).toList(),
                pageData.getTotalElements(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ArticleListResponse> listAll(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Article> pageData = articleRepository.findAll(pageable);
        return PageResult.of(pageData.getContent().stream().map(this::toListResponse).toList(),
                pageData.getTotalElements(), page, size);
    }

    private ArticleListResponse toListResponse(Article a) {
        ArticleListResponse r = new ArticleListResponse();
        r.setId(a.getId()); r.setTitle(a.getTitle()); r.setSlug(a.getSlug());
        r.setSummary(a.getSummary()); r.setCoverImage(a.getCoverImage());
        r.setViewCount(a.getViewCount()); r.setIsTop(a.getIsTop());
        r.setPublishedAt(a.getPublishedAt());
        r.setAuthorName(a.getAuthor().getUsername());
        if (a.getCategory() != null) r.setCategoryName(a.getCategory().getName());
        if (a.getTags() != null) r.setTagNames(a.getTags().stream().map(Tag::getName).toList());
        return r;
    }

    private ArticleDetailResponse toDetailResponse(Article a) {
        ArticleDetailResponse r = new ArticleDetailResponse();
        r.setId(a.getId()); r.setTitle(a.getTitle()); r.setSlug(a.getSlug());
        r.setSummary(a.getSummary()); r.setContent(a.getContent());
        r.setCoverImage(a.getCoverImage()); r.setViewCount(a.getViewCount());
        r.setIsTop(a.getIsTop()); r.setAllowComment(a.getAllowComment());
        r.setPublishedAt(a.getPublishedAt()); r.setCreatedAt(a.getCreatedAt());
        r.setAuthorName(a.getAuthor().getUsername());
        r.setAuthorAvatar(a.getAuthor().getAvatar());
        if (a.getCategory() != null) {
            r.setCategoryName(a.getCategory().getName());
            r.setCategoryId(a.getCategory().getId());
        }
        if (a.getTags() != null) r.setTagNames(a.getTags().stream().map(Tag::getName).toList());
        return r;
    }

    private Set<Tag> resolveTags(List<String> names) {
        List<Tag> existing = tagRepository.findByNameIn(names);
        Set<String> existingNames = existing.stream().map(Tag::getName).collect(Collectors.toSet());
        List<Tag> newTags = names.stream()
                .filter(n -> !existingNames.contains(n))
                .map(n -> { Tag t = new Tag(); t.setName(n); t.setSlug(slugify.slugify(n)); return t; })
                .map(tagRepository::save)
                .toList();
        Set<Tag> all = new HashSet<>(existing);
        all.addAll(newTags);
        return all;
    }

    private String resolveSlug(String slug, String title) {
        return (slug != null && !slug.isBlank()) ? slug : slugify.slugify(title);
    }
}
