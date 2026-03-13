package com.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

public class ProductDTOs {

    @Data
    public static class CreateProductRequest {
        @NotBlank(message = "Product name is required")
        private String name;

        private String description;

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal price;

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock cannot be negative")
        private Integer stockQuantity;

        private String imageUrl;

        private Set<Long> categoryIds;
    }

    @Data
    public static class UpdateProductRequest {
        private String name;
        private String description;

        @DecimalMin(value = "0.01")
        private BigDecimal price;

        @Min(value = 0)
        private Integer stockQuantity;

        private String imageUrl;
        private Set<Long> categoryIds;
        private Boolean active;
    }

    @Data
    public static class ProductSearchRequest {
        private String name;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Long categoryId;
        private int page = 0;
        private int size = 10;
        private String sortBy = "id";
        private String sortDir = "asc";
    }
}
