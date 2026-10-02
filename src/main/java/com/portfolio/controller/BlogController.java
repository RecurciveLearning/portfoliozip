package com.portfolio.controller;

import com.portfolio.entity.Blog;
import com.portfolio.entity.Category;
import com.portfolio.repository.BlogRepository;
import com.portfolio.repository.CategoryRepository;
import com.portfolio.service.BlogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/blogs")
public class BlogController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BlogRepository blogRepository;

    /** List of blogs with pagination */
    @GetMapping
    public String blogList(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "6") int size) {

        Page<Blog> blogsPage = blogService.getBlogs(page, size);

        model.addAttribute("blogs", blogsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", blogsPage.getTotalPages());
        model.addAttribute("pageSize", size);

        return "blogs"; // Thymeleaf template: blogs.html
    }

    /** Blog detail page */
    @GetMapping("/{id}")
    public String blogDetail(@PathVariable Long id, HttpServletRequest request, Model model) {
        Blog blog = blogService.getBlogById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found."));

        // Shorten image URL for display if too long
        String imageUrl = blog.getImage();
        String shortImageUrl = (imageUrl != null && imageUrl.length() > 100)
                ? imageUrl.substring(0, 100) + "..."
                : imageUrl;

        model.addAttribute("blog", blog);
        model.addAttribute("shortImageUrl", shortImageUrl);
        model.addAttribute("currentUrl", request.getRequestURL().toString());
        model.addAttribute("recommendedBlogs", blogService.getRecommendedBlogs(id, 3));

        return "blog-detail"; // Thymeleaf template
    }

    /** Show blog creation form */
    @GetMapping("/new")
    public String newBlogForm(Model model) {
        model.addAttribute("blog", new Blog());
        model.addAttribute("categories", categoryRepository.findAll());
        return "blog-form"; // Thymeleaf form
    }

    @PostMapping("/save")
    public String saveBlog(@ModelAttribute Blog blog,
                           @RequestParam(value = "category.id", required = false) Long categoryId,
                           @RequestParam(value = "categoryName", required = false) String categoryName,
                           org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        blogService.saveBlog(blog, categoryId, categoryName);

        redirectAttributes.addFlashAttribute("success", "Blog saved successfully.");
        return "redirect:/admin/dashboard";
    }
}
