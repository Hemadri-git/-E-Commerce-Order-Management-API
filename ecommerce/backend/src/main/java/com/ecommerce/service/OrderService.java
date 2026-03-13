package com.ecommerce.service;

import com.ecommerce.dto.OrderDTOs.*;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CartService cartService;
    @Autowired private ProductRepository productRepository;

    @Transactional
    public Order placeOrder(String username, PlaceOrderRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Cart cart = cartService.getCartByUsername(username);
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // Validate stock and build order items atomically
        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", cartItem.getProduct().getId()));

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException(
                    "Insufficient stock for '" + product.getName() + "'. Available: " + product.getStockQuantity());
            }

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(subtotal);

            orderItems.add(OrderItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build());

            // Deduct stock atomically
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(total)
                .shippingAddress(req.getShippingAddress())
                .build();

        Order savedOrder = orderRepository.save(order);

        // Link order items to order
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
        }
        savedOrder.setOrderItems(orderItems);
        orderRepository.save(savedOrder);

        // Clear cart after order placed
        cartService.clearCart(cart);

        return savedOrder;
    }

    public List<Order> getMyOrders(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public Page<Order> getAllOrders(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (status != null && !status.isBlank()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                return orderRepository.findByStatus(orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid order status: " + status);
            }
        }
        return orderRepository.findAll(pageable);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    @Transactional
    public Order updateOrderStatus(Long id, UpdateOrderStatusRequest req) {
        Order order = getOrderById(id);

        // Validate state machine transitions
        validateStatusTransition(order.getStatus(), req.getStatus());

        // If cancelling, restore stock
        if (req.getStatus() == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(req.getStatus());
        return orderRepository.save(order);
    }

    public Order getMyOrderById(String username, Long orderId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Order order = getOrderById(orderId);
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You do not have access to this order");
        }
        return order;
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        // Valid transitions
        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
        if (!valid) {
            throw new BadRequestException("Invalid status transition: " + current + " → " + next);
        }
    }
}
