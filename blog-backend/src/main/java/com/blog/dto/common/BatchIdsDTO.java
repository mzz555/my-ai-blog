package com.blog.dto.common;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

/**
 * 通用批量 ids 入参。
 * 最多 100 条，防止误操作和性能问题。
 */
@Data
public class BatchIdsDTO {
    @NotEmpty(message = "ids 不能为空")
    @Size(max = 100, message = "批量不能超过 100 条")
    private List<Long> ids;
}
