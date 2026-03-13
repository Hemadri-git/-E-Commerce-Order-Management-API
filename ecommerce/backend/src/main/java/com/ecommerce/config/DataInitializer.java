package com.ecommerce.config;

import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedCustomer();
        seedProducts();
    }

    private void seedAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@ecommerce.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            logger.info("✅ Admin user created: username=admin, password=admin123");
        }
    }

    private void seedCustomer() {
        if (!userRepository.existsByUsername("customer")) {
            User customer = User.builder()
                    .username("customer")
                    .email("customer@ecommerce.com")
                    .password(passwordEncoder.encode("customer123"))
                    .role(Role.CUSTOMER)
                    .active(true)
                    .build();
            userRepository.save(customer);
            logger.info("✅ Customer user created: username=customer, password=customer123");
        }
    }

    private void seedProducts() {
        if (productRepository.count() > 0) return;

        Category electronics = categoryRepository.findByName("Electronics")
                .orElseGet(() -> categoryRepository.save(
                        Category.builder().name("Electronics").description("Gadgets and tech").build()));
        Category clothing = categoryRepository.findByName("Clothing")
                .orElseGet(() -> categoryRepository.save(
                        Category.builder().name("Clothing").description("Fashion for everyone").build()));
        Category books = categoryRepository.findByName("Books")
                .orElseGet(() -> categoryRepository.save(
                        Category.builder().name("Books").description("Knowledge and stories").build()));
        Category homeKitchen = categoryRepository.findByName("Home & Kitchen")
                .orElseGet(() -> categoryRepository.save(
                        Category.builder().name("Home & Kitchen").description("Home essentials").build()));
        Category sports = categoryRepository.findByName("Sports")
                .orElseGet(() -> categoryRepository.save(
                        Category.builder().name("Sports").description("Sports and outdoors").build()));

        productRepository.saveAll(java.util.List.of(
            buildProduct("iPhone 15 Pro", "Latest Apple flagship with A17 Pro chip, 48MP camera system", new BigDecimal("999.99"), 50, "https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=400", Set.of(electronics)),
            buildProduct("Samsung Galaxy S24 Ultra", "Android powerhouse with 200MP camera and S Pen", new BigDecimal("1199.99"), 35, "https://images.unsplash.com/photo-1610945264803-c22b62d2a7b3?w=400", Set.of(electronics)),
            buildProduct("Sony WH-1000XM5", "Industry-leading noise cancelling wireless headphones", new BigDecimal("349.99"), 80, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400", Set.of(electronics)),
            buildProduct("MacBook Air M3", "Supercharged by M3 chip, ultra-thin and lightweight", new BigDecimal("1299.99"), 25, "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400", Set.of(electronics)),
            buildProduct("Men's Slim Fit Jeans", "Classic blue denim, comfortable stretch fabric", new BigDecimal("59.99"), 150, "https://images.unsplash.com/photo-1542272604-787c3835535d?w=400", Set.of(clothing)),
            buildProduct("Women's Yoga Pants", "High-waist, moisture-wicking performance leggings", new BigDecimal("49.99"), 200, "https://images.unsplash.com/photo-1506629082955-511b1aa562c8?w=400", Set.of(clothing, sports)),
            buildProduct("Unisex Hoodie", "Soft fleece pullover, perfect for any season", new BigDecimal("39.99"), 120, "https://images.unsplash.com/photo-1556821840-3a63f15732ce?w=400", Set.of(clothing)),
            buildProduct("Clean Code", "A handbook of agile software craftsmanship by Robert C. Martin", new BigDecimal("34.99"), 100, "https://images.unsplash.com/photo-1589998059171-988d887df646?w=400", Set.of(books)),
            buildProduct("The Pragmatic Programmer", "Your journey to mastery — 20th Anniversary Edition", new BigDecimal("49.99"), 75, "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=400", Set.of(books)),
            buildProduct("Instant Pot Duo 7-in-1", "Electric pressure cooker, slow cooker, rice cooker and more", new BigDecimal("89.99"), 60, "https://images.unsplash.com/photo-1585515320310-259814833e62?w=400", Set.of(homeKitchen)),
            buildProduct("Nike Air Max 270", "Lightweight running shoes with Max Air unit", new BigDecimal("150.00"), 90, "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400", Set.of(sports)),
            buildProduct("Yoga Mat Premium", "Non-slip, eco-friendly 6mm thick exercise mat", new BigDecimal("29.99"), 180, "https://images.unsplash.com/photo-1601925228217-b01bc6a9ea20?w=400", Set.of(sports))
        ));
        logger.info("✅ Sample products seeded successfully");
    }

    private Product buildProduct(String name, String desc, BigDecimal price, int stock, String imageUrl, Set<Category> categories) {
        return Product.builder()
                .name(name)
                .description(desc)
                .price(price)
                .stockQuantity(stock)
                .imageUrl(imageUrl)
                .active(true)
                .categories(categories)
                .build();
    }
}
