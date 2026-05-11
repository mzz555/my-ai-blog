package com.blog.dto.comment;

import com.blog.entity.Comment.CommentStatus;
import com.blog.dto.common.BatchIdsDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论批量改状态入参。
 * 仅支持改为 APPROVED 或 REJECTED；想改为 PENDING 不开放（业务规则）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BatchStatusDTO extends BatchIdsDTO {
    @NotNull(message = "status 不能为空")
    private CommentStatus status;
}
