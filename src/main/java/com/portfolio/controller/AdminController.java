package com.portfolio.controller;
import com.portfolio.entity.*;
import com.portfolio.repository.CategoryRepository;
import com.portfolio.service.PortfolioService;
import com.portfolio.service.CertificationService;
import com.portfolio.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private CertificationService certificationService;

    @Autowired
    private BlogService blogService;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("profile", portfolioService.getUserProfile());
        model.addAttribute("education", portfolioService.getAllEducation());
        model.addAttribute("experiences", portfolioService.getAllExperience());
        model.addAttribute("skills", portfolioService.getAllSkills());
        model.addAttribute("projects", portfolioService.getAllProjects());
        model.addAttribute("contactMessages", portfolioService.getAllContactMessages());
        model.addAttribute("meetingRequests", portfolioService.getAllMeetingRequests());
        model.addAttribute("resume", portfolioService.getLatestResume());
        model.addAttribute("certifications", certificationService.getAllCertifications());
        model.addAttribute("certification", new Certification());
        model.addAttribute("blogs", blogService.getBlogs(0, 100).getContent());
        return "admin/dashboard";
    }

    @GetMapping("/blogs/new")
    public String newBlog(Model model) {
        model.addAttribute("blog", new Blog());
        model.addAttribute("categories", categoryRepository.findAll());
        return "blog-form";
    }

    @GetMapping("/blogs/edit/{id}")
    public String editBlog(@PathVariable Long id, Model model) {
        Blog blog = blogService.getBlogById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Blog not found."));
        model.addAttribute("blog", blog);
        model.addAttribute("categories", categoryRepository.findAll());
        return "blog-form";
    }

    @PostMapping("/blogs/delete/{id}")
    public String deleteBlog(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        blogService.deleteBlog(id);
        redirectAttributes.addFlashAttribute("success", "Blog deleted successfully.");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/profile/save")
    public String saveProfile(@ModelAttribute UserProfile profile, RedirectAttributes redirectAttributes) {
        portfolioService.saveUserProfile(profile);
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/education/save")
    public String saveEducation(@ModelAttribute Education education, RedirectAttributes redirectAttributes) {
        portfolioService.saveEducation(education);
        redirectAttributes.addFlashAttribute("success", "Education added successfully!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/experience/save")
    public String saveExperience(@ModelAttribute Experience experience, RedirectAttributes redirectAttributes) {
        portfolioService.saveExperience(experience);
        redirectAttributes.addFlashAttribute("success", "Experience added successfully!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/skill/save")
    public String saveSkill(@ModelAttribute Skill skill, RedirectAttributes redirectAttributes) {
        portfolioService.saveSkill(skill);
        redirectAttributes.addFlashAttribute("success", "Skill added successfully!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/project/save")
    public String saveProject(@ModelAttribute Project project, RedirectAttributes redirectAttributes) {
        portfolioService.saveProject(project);
        redirectAttributes.addFlashAttribute("success", "Project added successfully!");
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/certification/list")
    @ResponseBody
    public List<Map<String, Object>> listCertifications() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        List<Map<String, Object>> result = new ArrayList<>();
        for (Certification cert : certificationService.getAllCertifications()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cert.getId());
            map.put("name", cert.getName());
            map.put("organization", cert.getOrganization());
            map.put("issueDate", cert.getIssueDate() != null ? cert.getIssueDate().format(formatter) : "");
            map.put("verified", cert.isVerified());
            map.put("certificateUrl", cert.getCertificateUrl());
            map.put("description", cert.getDescription());
            result.add(map);
        }
        return result;
    }

    @PostMapping("/certification/save")
    @ResponseBody
    public Map<String, Object> saveCertification(@ModelAttribute Certification cert) {
        Map<String, Object> response = new HashMap<>();
        try {
            Certification saved = certificationService.save(cert);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            response.put("success", true);
            response.put("cert", Map.of(
                    "id", saved.getId(),
                    "name", saved.getName(),
                    "organization", saved.getOrganization(),
                    "issueDate", saved.getIssueDate() != null ? saved.getIssueDate().format(formatter) : "",
                    "verified", saved.isVerified(),
                    "certificateUrl", saved.getCertificateUrl()
            ));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @DeleteMapping("/certification/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteCertification(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            certificationService.deleteById(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    /** ✅ Verify All Certifications (AJAX Trigger Async) */
    @PostMapping("/certification/verify-all")
    @ResponseBody
    public Map<String, Object> verifyAll() {
        Map<String, Object> response = new HashMap<>();
        try {
            certificationService.verifyAllCertificates(); // async
            response.put("success", true);
            response.put("message", "Verification started in background!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/education/delete/{id}")
    public String deleteEducation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        portfolioService.deleteEducation(id);
        redirectAttributes.addFlashAttribute("success", "Education deleted successfully!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/experience/delete/{id}")
    public String deleteExperience(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        portfolioService.deleteExperience(id);
        redirectAttributes.addFlashAttribute("success", "Experience deleted successfully!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/skill/delete/{id}")
    public String deleteSkill(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        portfolioService.deleteSkill(id);
        redirectAttributes.addFlashAttribute("success", "Skill deleted successfully!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/project/delete/{id}")
    public String deleteProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        portfolioService.deleteProject(id);
        redirectAttributes.addFlashAttribute("success", "Project deleted successfully!");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/resume/upload")
    public String uploadResume(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a file to upload");
            return "redirect:/admin/dashboard";
        }
        if (file.getSize() > 5L * 1024 * 1024) {
            redirectAttributes.addFlashAttribute("error", "The PDF must be 5 MB or smaller.");
            return "redirect:/admin/dashboard";
        }
        try {
            byte[] contents = file.getBytes();
            if (contents.length < 5 || contents[0] != '%' || contents[1] != 'P'
                    || contents[2] != 'D' || contents[3] != 'F' || contents[4] != '-') {
                redirectAttributes.addFlashAttribute("error", "The selected file is not a valid PDF.");
                return "redirect:/admin/dashboard";
            }

            Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "resume")
                    .toAbsolutePath().normalize();
            String fileName = "Himanshu_Resume.pdf";
            Files.createDirectories(uploadDir);
            Path newFilePath = uploadDir.resolve(fileName);
            Path tempFile = Files.createTempFile(uploadDir, "resume-", ".tmp");
            Files.write(tempFile, contents);
            try {
                Files.move(tempFile, newFilePath, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(tempFile, newFilePath, StandardCopyOption.REPLACE_EXISTING);
            }

            Resume oldResume = portfolioService.getLatestResume();
            if (oldResume != null) {
                portfolioService.deleteResume(oldResume.getId());
            }
            Resume newResume = new Resume();
            newResume.setFileName(fileName);
            portfolioService.saveResume(newResume);
            redirectAttributes.addFlashAttribute("success", "Resume updated successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload the resume. Please try again.");
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/meeting/update/{id}")
    public String updateMeeting(@PathVariable Long id, @RequestParam String meetingLink,
                                @RequestParam String status, RedirectAttributes redirectAttributes) {
        String normalizedStatus = status == null ? "" : status.trim().toUpperCase();
        if (!List.of("PENDING", "CONFIRMED", "CANCELLED").contains(normalizedStatus)) {
            redirectAttributes.addFlashAttribute("error", "Choose a valid meeting status.");
            return "redirect:/admin/dashboard";
        }
        portfolioService.getMeetingRequestById(id).ifPresent(meeting -> {
            meeting.setMeetingLink(meetingLink);
            meeting.setStatus(normalizedStatus);
            portfolioService.updateMeetingRequest(meeting);
        });

        redirectAttributes.addFlashAttribute("success", "Meeting request updated successfully!");
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/error")
    public String adminError() {
        return "admin/error";
    }
}
