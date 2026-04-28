package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.dto.tag.TagVO;
import com.blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 标签数据访问层
 *
 * @author blog
 * @since 1.0.0
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 根据名称列表批量查询已存在的标签
     *
     * @param names 标签名称列表
     * @return 已存在的标签列表
     */
    @Select("<script>SELECT * FROM tags WHERE name IN " +
            "<foreach collection='names' item='name' open='(' separator=',' close=')'>" +
            "#{name}" +
            "</foreach></script>")
    List<Tag> selectByNames(@Param("names") List<String> names);

    /**
     * 查询文章关联的所有标签（通过 article_tags 表联查）
     *
     * @param articleId 文章 ID
     * @return 该文章的标签列表
     */
    @Select("SELECT t.* FROM tags t " +
            "JOIN article_tags at ON t.id = at.tag_id " +
            "WHERE at.article_id = #{articleId}")
    List<Tag> selectByArticleId(@Param("articleId") Long articleId);

    @Select("SELECT t.id, t.name, t.slug, COUNT(at.article_id) AS article_count " +
            "FROM tags t " +
            "LEFT JOIN article_tags at ON t.id = at.tag_id " +
            "GROUP BY t.id, t.name, t.slug " +
            "ORDER BY t.name ASC")
    List<TagVO> selectWithArticleCount();
}
