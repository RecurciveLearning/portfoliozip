package com.portfolio.controller;

import com.portfolio.repository.InterviewQuestionRepository;
import com.portfolio.entity.InterviewQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "admin.username=test-admin",
        "admin.password=test-only-password",
        "spring.datasource.url=jdbc:h2:mem:portfolio-test;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class InterviewQuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InterviewQuestionRepository questionRepository;

    @BeforeEach
    void clearQuestions() {
        questionRepository.deleteAll();
    }

    @Test
    void visitorCanOpenTheInterviewQuestionBoard() throws Exception {
        mockMvc.perform(get("/interview-questions"))
                .andExpect(status().isOk())
                .andExpect(view().name("interview-questions"))
                .andExpect(content().string(containsString("Add an interview question")))
                .andExpect(content().string(containsString("No questions have been shared yet")));
    }

    @Test
    void visitorCanShareAQuestionAndAnswer() throws Exception {
        mockMvc.perform(post("/interview-questions")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("company", "Example Labs")
                        .param("role", "Backend Engineer")
                        .param("askedOn", "2026-09-30")
                        .param("question", "How would you design a URL shortener?")
                        .param("answer", "Start with requirements, then discuss IDs, storage, and redirects."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/interview-questions"));

        mockMvc.perform(get("/interview-questions"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Example Labs")))
                .andExpect(content().string(containsString("How would you design a URL shortener?")))
                .andExpect(content().string(containsString("Start with requirements, then discuss IDs")))
                .andExpect(content().string(containsString("Share Q&amp;A")))
                .andExpect(content().string(containsString("id=\"question-" +
                        questionRepository.findAll().get(0).getId() + "\"")))
                .andExpect(content().string(containsString("Delete this interview question and answer")))
                .andExpect(content().string(containsString("Confirm delete")))
                .andExpect(content().string(containsString("Delete this post for everyone?")));
    }

    @Test
    void anyVisitorCanDeleteAnInterviewQuestion() throws Exception {
        InterviewQuestion question = questionRepository.save(new InterviewQuestion(
                "Example Labs", "Backend Engineer", LocalDate.now(),
                "Question to remove", "Answer to remove"
        ));

        mockMvc.perform(post("/interview-questions/{id}/delete", question.getId())
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/interview-questions"));

        assertFalse(questionRepository.existsById(question.getId()));
        mockMvc.perform(get("/interview-questions"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No questions have been shared yet")));
    }

    @Test
    void invalidQuestionOrAnswerIsNotPublished() throws Exception {
        mockMvc.perform(post("/interview-questions")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("question", " ")
                        .param("answer", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("interview-questions"))
                .andExpect(content().string(containsString("Add the interview question.")))
                .andExpect(content().string(containsString("Add an answer or your approach.")));

        org.junit.jupiter.api.Assertions.assertEquals(0, questionRepository.count());
    }
}