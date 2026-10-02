package com.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class InterviewQuestionForm {

    @Size(max = 120, message = "Company name must be 120 characters or fewer.")
    private String company;

    @Size(max = 120, message = "Role must be 120 characters or fewer.")
    private String role;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate askedOn;

    @NotBlank(message = "Add the interview question.")
    @Size(max = 2000, message = "Question must be 2,000 characters or fewer.")
    private String question;

    @NotBlank(message = "Add an answer or your approach.")
    @Size(max = 8000, message = "Answer must be 8,000 characters or fewer.")
    private String answer;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDate getAskedOn() {
        return askedOn;
    }

    public void setAskedOn(LocalDate askedOn) {
        this.askedOn = askedOn;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}