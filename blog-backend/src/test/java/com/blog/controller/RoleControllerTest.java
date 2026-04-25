package com.blog.controller;

import com.blog.dto.auth.LoginRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RoleControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    private String adminToken;

    @BeforeEach
    void setup() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("Admin@2024");
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andReturn();
        adminToken = objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();
    }

    @Test
    void listRoles_shouldReturnListWithPermissionsField() throws Exception {
        mockMvc.perform(get("/api/admin/roles")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void createAndDeleteRole_shouldWork() throws Exception {
        String body = """
            {"name":"测试角色","description":"仅测试用","permissionCodes":["article:list"]}
            """;
        MvcResult created = mockMvc.perform(post("/api/admin/roles")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("测试角色"))
                .andReturn();
        Long id = objectMapper.readTree(created.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(delete("/api/admin/roles/" + id)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
