-- V3: articles 表添加点赞数字段
ALTER TABLE articles ADD COLUMN like_count INT NOT NULL DEFAULT 0;
