package com.portfolio.service;

import com.portfolio.entity.Blog;
import com.portfolio.entity.Category;
import com.portfolio.repository.BlogRepository;
import com.portfolio.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private CategoryRepository categoryRepository;


    @Cacheable(value = "blogs", key = "#page + '-' + #size")
    public Page<Blog> getBlogs(int page, int size) {
        return blogRepository.findAll(PageRequest.of(page, size));
    }

    @Cacheable(value = "blog", key = "#id")
    public Optional<Blog> getBlogById(Long id) {
        return blogRepository.findById(id);
    }

    @Cacheable(value = "recommendedBlogs", key = "#excludeId + '-' + #limit")
    public List<Blog> getRecommendedBlogs(Long excludeId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return blogRepository.findRecommendedBlogs(excludeId, pageable).getContent();
    }


    @CacheEvict(value = {"blogs", "blog", "recommendedBlogs"}, allEntries = true)
    public Blog saveBlog(Blog blog, Long categoryId, String categoryName) {
        Category category = null;
        Blog existing = blog.getId() == null ? null : blogRepository.findById(blog.getId())
                .orElseThrow(() -> new IllegalArgumentException("Blog not found"));

        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
        } else if (categoryName != null && !categoryName.trim().isEmpty()) {
            // Check if category already exists
            category = categoryRepository.findByName(categoryName.trim())
                    .orElseGet(() -> {
                        Category newCat = new Category();
                        newCat.setName(categoryName.trim());
                        return categoryRepository.save(newCat);
                    });
        } else if (existing != null) {
            category = existing.getCategory();
        }

        if (existing != null) {
            blog.setCreatedAt(existing.getCreatedAt());
        }
        blog.setCategory(category);
        return blogRepository.save(blog);
    }

    @CacheEvict(value = {"blogs", "blog", "recommendedBlogs"}, allEntries = true)
    public void deleteBlog(Long id) {
        blogRepository.deleteById(id);
    }
}
