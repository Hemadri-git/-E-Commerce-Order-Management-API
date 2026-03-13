package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CartDTOs.*;
import com.ecommerce.model.Cart;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<Cart>> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        Cart cart = cartService.getCartByUsername(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<Cart>> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddToCartRequest request) {
        Cart cart = cartService.addToCart(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Cart>> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        Cart cart = cartService.updateCartItem(userDetails.getUsername(), itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart updated", cart));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<Cart>> removeCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {
        Cart cart = cartService.removeCartItem(userDetails.getUsername(), itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        Cart cart = cartService.getCartByUsername(userDetails.getUsername());
        cartService.clearCart(cart);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
