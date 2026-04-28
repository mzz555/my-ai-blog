package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.tag.TagRequest;
import com.blog.dto.tag.TagVO;
import com.blog.entity.Tag;
import java.util.List;

/**
 * 标签服务接口
 *
 * @author blog
 * @since 1.0.0
 */
public interface TagService extends IService<Tag> {

    /**
     * 查询所有标签列表
     *
     * @return 标签列表
     */
    List<TagVO> listAll();

    /**
     * 创建新标签（slug 自动从名称生成）
     *
     * @param request 标签创建请求
     * @return 创建后的标签实体
     */
    Tag create(TagRequest request);

    /**
     * 删除标签
     *
     * @param id 标签 ID
     */
    void delete(Long id);
}
