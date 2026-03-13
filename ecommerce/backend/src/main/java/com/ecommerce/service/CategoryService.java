package com.ecommerce.service;

import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Category;
import com.ecommerce.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    public Category createCategory(String name, String description) {
        if (categoryRepository.existsByName(name)) {
            throw new BadRequestException("Category already exists: " + name);
        }
        return categoryRepository.save(Category.builder().name(name).description(description).build());
    }

    public Category updateCategory(Long id, String name, String description) {
        Category cat = getCategoryById(id);
        if (name != null) cat.setName(name);
        if (description != null) cat.setDescription(description);
        return categoryRepository.save(cat);
    }

    public void deleteCategory(Long id) {
        Category cat = getCategoryById(id);
        categoryRepository.delete(cat);
    }
}
