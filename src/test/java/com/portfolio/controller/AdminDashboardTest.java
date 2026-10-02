package com.portfolio.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "admin.username=test-admin",
        "admin.password=test-only-password",
        "spring.datasource.url=jdbc:h2:mem:portfolio-test;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AdminDashboardTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void dashboardRendersBlogAndMeetingManagement() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(content().string(containsString("Blog Posts")))
                .andExpect(content().string(containsString("Meeting Requests")))
                .andExpect(content().string(containsString("No posts yet")));
    }

    @Test
    void blogEditorRequiresAdminLogin() throws Exception {
        mockMvc.perform(get("/blogs/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/admin/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanOpenBlogEditor() throws Exception {
        mockMvc.perform(get("/admin/blogs/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("blog-form"))
                .andExpect(content().string(containsString("Create a New Blog")));
    }

    @Test
    void visitorCanOpenNotepad() throws Exception {
        mockMvc.perform(get("/notes/notepad"))
                .andExpect(status().isOk())
                .andExpect(view().name("notepad"));
    }

    @Test
    void visitorCanCreateReadUpdateAndDeleteSharedNotes() throws Exception {
        MvcResult created = mockMvc.perform(post("/notes/save")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Public test note\",\"content\":\"First version\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Public test note"))
                .andExpect(jsonPath("$.content").value("First version"))
                .andReturn();

        String id = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(get("/notes/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")]").exists());

        mockMvc.perform(get("/notes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("First version"));

        mockMvc.perform(post("/notes/save")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + id + ",\"title\":\"Updated test note\",\"content\":\"Second version\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated test note"))
                .andExpect(jsonPath("$.content").value("Second version"));

        mockMvc.perform(delete("/notes/" + id)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/notes/" + id))
                .andExpect(status().isNotFound());
    }
}