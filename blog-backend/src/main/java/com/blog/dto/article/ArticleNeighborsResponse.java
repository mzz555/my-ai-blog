package com.blog.dto.article;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleNeighborsResponse {
    private NeighborItem prev;
    private NeighborItem next;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NeighborItem {
        private String title;
        private String slug;
    }
}
