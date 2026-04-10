package com.blog.common;

import lombok.Data;
import java.util.List;

/**
 * 分页结果包装类
 * <p>用于包装所有分页查询的响应数据，配合 {@link Result} 一起返回。</p>
 *
 * @param <T> 列表元素类型
 * @author blog
 * @since 1.0.0
 */
@Data
public class PageResult<T> {

    /** 当前页数据列表 */
    private List<T> list;

    /** 总记录数 */
    private long total;

    /** 当前页码（从 1 开始） */
    private int page;

    /** 每页数量 */
    private int size;

    /**
     * 构造分页结果
     *
     * @param list  当前页数据
     * @param total 总记录数
     * @param page  当前页码
     * @param size  每页数量
     * @param <T>   列表元素类型
     * @return 分页结果对象
     */
    public static <T> PageResult<T> of(List<T> list, long total, int page, int size) {
        PageResult<T> r = new PageResult<>();
        r.setList(list);
        r.setTotal(total);
        r.setPage(page);
        r.setSize(size);
        return r;
    }
}
