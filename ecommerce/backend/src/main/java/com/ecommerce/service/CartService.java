package com.ecommerce.service;

import com.ecommerce.dto.CartDTOs.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    public Cart getCartByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart cart = Cart.builder().user(user).build();
                    return cartRepository.save(cart);
                });
    }

    @Transactional
    public Cart addToCart(String username, AddToCartRequest req) {
        Cart cart = getCartByUsername(username);
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", req.getProductId()));

        if (!product.isActive()) throw new BadRequestException("Product is not available");
        if (product.getStockQuantity() < req.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
        }

        // Check if item already in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(req.getProductId()))
                .findFirst().orElse(null);

        if (existingItem != null) {
            int newQty = existingItem.getQuantity() + req.getQuantity();
            if (product.getStockQuantity() < newQty) {
                throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
            }
            existingItem.setQuantity(newQty);
            cartItemRepository.save(existingItem);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(req.getQuantity())
                    .build();
            cart.getItems().add(item);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateCartItem(String username, Long itemId, UpdateCartItemRequest req) {
        Cart cart = getCartByUsername(username);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));

        if (req.getQuantity() == 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            if (item.getProduct().getStockQuantity() < req.getQuantity()) {
                throw new BadRequestException("Insufficient stock");
            }
            item.setQuantity(req.getQuantity());
            cartItemRepository.save(item);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeCartItem(String username, Long itemId) {
        Cart cart = getCartByUsername(username);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
