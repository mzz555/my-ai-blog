package com.blog.controller;

import com.blog.dto.user.UserCreateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "user:list")
    void create_withValidDto_shouldReturn200() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("ctrl_" + System.currentTimeMillis());
        dto.setEmail("ctrl_" + System.currentTimeMillis() + "@example.com");
        dto.setPassword("secret123");

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @WithMockUser(authorities = "user:list")
    void create_withBlankUsername_shouldReturn400() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("");
        dto.setEmail("blank@example.com");
        dto.setPassword("secret123");

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "other:permission")
    void create_withoutUserListAuthority_shouldReturn403() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("noauth_" + System.currentTimeMillis());
        dto.setEmail("noauth_" + System.currentTimeMillis() + "@example.com");
        dto.setPassword("secret123");

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
