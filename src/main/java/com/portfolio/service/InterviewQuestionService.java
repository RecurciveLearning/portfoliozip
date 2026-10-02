package com.portfolio.service;

import com.portfolio.dto.InterviewQuestionForm;
import com.portfolio.entity.InterviewQuestion;
import com.portfolio.repository.InterviewQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InterviewQuestionService {

    private final InterviewQuestionRepository repository;

    public InterviewQuestionService(InterviewQuestionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<InterviewQuestion> getRecentQuestions() {
        return repository.findTop100ByOrderByCreatedAtDesc();
    }

    @Transactional
    public InterviewQuestion submit(InterviewQuestionForm form) {
        return repository.save(new InterviewQuestion(
                cleanOptional(form.getCompany()),
                cleanOptional(form.getRole()),
                form.getAskedOn(),
                form.getQuestion().strip(),
                form.getAnswer().strip()
        ));
    }

    @Transactional
    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    private String cleanOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.strip();
    }
}