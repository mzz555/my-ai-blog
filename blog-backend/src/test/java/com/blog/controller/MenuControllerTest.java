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
class MenuControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    private String adminToken;

    @BeforeEach
    void setup() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("Admin@2024");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        adminToken = body.path("data").path("accessToken").asText();
    }

    @Test
    void listMenus_withAdminToken_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/admin/menus")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void listMenus_withoutToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/menus"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createAndDeleteMenu_shouldWork() throws Exception {
        String body = """
            {"name":"测试菜单","path":"/test","icon":"","sort":99,"visible":true,"parentId":null}
            """;
        MvcResult created = mockMvc.perform(post("/api/admin/menus")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("测试菜单"))
                .andReturn();
        Long id = objectMapper.readTree(created.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(delete("/api/admin/menus/" + id)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
