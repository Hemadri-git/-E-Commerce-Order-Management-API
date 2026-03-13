package com.ecommerce.service;

import com.ecommerce.dto.ProductDTOs.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;

    public Page<Product> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findByActiveTrue(pageable);
    }

    public Page<Product> searchProducts(ProductSearchRequest req) {
        Sort sort = req.getSortDir().equalsIgnoreCase("desc")
                ? Sort.by(req.getSortBy()).descending()
                : Sort.by(req.getSortBy()).ascending();
        Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), sort);
        return productRepository.searchProducts(req.getName(), req.getMinPrice(), req.getMaxPrice(), req.getCategoryId(), pageable);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Transactional
    public Product createProduct(CreateProductRequest req) {
        Set<Category> categories = resolveCategories(req.getCategoryIds());
        Product product = Product.builder()
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .stockQuantity(req.getStockQuantity())
                .imageUrl(req.getImageUrl())
                .active(true)
                .categories(categories)
                .build();
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, UpdateProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        if (req.getName() != null) product.setName(req.getName());
        if (req.getDescription() != null) product.setDescription(req.getDescription());
        if (req.getPrice() != null) product.setPrice(req.getPrice());
        if (req.getStockQuantity() != null) product.setStockQuantity(req.getStockQuantity());
        if (req.getImageUrl() != null) product.setImageUrl(req.getImageUrl());
        if (req.getActive() != null) product.setActive(req.getActive());
        if (req.getCategoryIds() != null) product.setCategories(resolveCategories(req.getCategoryIds()));

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setActive(false); // Soft delete
        productRepository.save(product);
    }

    private Set<Category> resolveCategories(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return new HashSet<>();
        Set<Category> categories = new HashSet<>();
        for (Long catId : categoryIds) {
            Category cat = categoryRepository.findById(catId)
                    .orElseThrow(() -> new BadRequestException("Category not found: " + catId));
            categories.add(cat);
        }
        return categories;
    }
}
