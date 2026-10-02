package com.portfolio.controller;

import com.portfolio.dto.InterviewQuestionForm;
import com.portfolio.service.InterviewQuestionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/interview-questions")
public class InterviewQuestionController {

    private final InterviewQuestionService questionService;

    public InterviewQuestionController(InterviewQuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public String showBoard(Model model) {
        model.addAttribute("questionForm", new InterviewQuestionForm());
        model.addAttribute("questions", questionService.getRecentQuestions());
        return "interview-questions";
    }

    @PostMapping
    public String submitQuestion(@Valid @ModelAttribute("questionForm") InterviewQuestionForm questionForm,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("questions", questionService.getRecentQuestions());
            return "interview-questions";
        }

        questionService.submit(questionForm);
        redirectAttributes.addFlashAttribute("successMessage", "Your question and answer have been shared.");
        return "redirect:/interview-questions";
    }

    @PostMapping("/{id}/delete")
    public String deleteQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (questionService.delete(id)) {
            redirectAttributes.addFlashAttribute("successMessage", "The interview question was deleted for everyone.");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "That interview question was already deleted.");
        }
        return "redirect:/interview-questions";
    }
}