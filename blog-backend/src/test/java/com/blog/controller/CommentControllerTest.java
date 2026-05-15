package com.blog.controller;

import com.blog.dto.common.BatchIdsDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "comment:delete")
    void batchDelete_withValidIds_shouldReturn200() throws Exception {
        BatchIdsDTO dto = new BatchIdsDTO();
        dto.setIds(Arrays.asList(999_999_999L));
        mockMvc.perform(post("/api/comments/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.deleted").value(0));
    }

    @Test
    @WithMockUser(authorities = "comment:delete")
    void batchDelete_withEmptyIds_shouldReturn400() throws Exception {
        BatchIdsDTO dto = new BatchIdsDTO();
        dto.setIds(Collections.emptyList());
        mockMvc.perform(post("/api/comments/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "comment:delete")
    void batchDelete_withOver100Ids_shouldReturn400() throws Exception {
        BatchIdsDTO dto = new BatchIdsDTO();
        List<Long> ids = LongStream.rangeClosed(1, 101).boxed().collect(Collectors.toList());
        dto.setIds(ids);
        mockMvc.perform(post("/api/comments/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "other:permission")
    void batchDelete_withoutAuthority_shouldReturn403() throws Exception {
        BatchIdsDTO dto = new BatchIdsDTO();
        dto.setIds(Arrays.asList(1L));
        mockMvc.perform(post("/api/comments/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void batchDelete_unauthenticated_shouldReturn401() throws Exception {
        BatchIdsDTO dto = new BatchIdsDTO();
        dto.setIds(Arrays.asList(1L));
        mockMvc.perform(post("/api/comments/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "comment:approve")
    void batchStatus_toApproved_shouldReturn200() throws Exception {
        com.blog.dto.comment.BatchStatusDTO dto = new com.blog.dto.comment.BatchStatusDTO();
        dto.setIds(Arrays.asList(999_999_999L));
        dto.setStatus(com.blog.entity.Comment.CommentStatus.APPROVED);

        mockMvc.perform(post("/api/comments/batch-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updated").value(0));
    }

    @Test
    @WithMockUser(authorities = "comment:approve")
    void batchStatus_withEmptyIds_shouldReturn400() throws Exception {
        com.blog.dto.comment.BatchStatusDTO dto = new com.blog.dto.comment.BatchStatusDTO();
        dto.setIds(Collections.emptyList());
        dto.setStatus(com.blog.entity.Comment.CommentStatus.APPROVED);

        mockMvc.perform(post("/api/comments/batch-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "comment:approve")
    void batchStatus_withNullStatus_shouldReturn400() throws Exception {
        com.blog.dto.comment.BatchStatusDTO dto = new com.blog.dto.comment.BatchStatusDTO();
        dto.setIds(Arrays.asList(1L));
        dto.setStatus(null);

        mockMvc.perform(post("/api/comments/batch-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "other:permission")
    void batchStatus_withoutAuthority_shouldReturn403() throws Exception {
        com.blog.dto.comment.BatchStatusDTO dto = new com.blog.dto.comment.BatchStatusDTO();
        dto.setIds(Arrays.asList(1L));
        dto.setStatus(com.blog.entity.Comment.CommentStatus.APPROVED);

        mockMvc.perform(post("/api/comments/batch-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void batchStatus_unauthenticated_shouldReturn401() throws Exception {
        com.blog.dto.comment.BatchStatusDTO dto = new com.blog.dto.comment.BatchStatusDTO();
        dto.setIds(Arrays.asList(1L));
        dto.setStatus(com.blog.entity.Comment.CommentStatus.APPROVED);

        mockMvc.perform(post("/api/comments/batch-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
