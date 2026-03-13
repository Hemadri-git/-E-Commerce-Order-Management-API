package com.ecommerce.dto;

import com.ecommerce.model.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class OrderDTOs {

    @Data
    public static class PlaceOrderRequest {
        @NotBlank(message = "Shipping address is required")
        private String shippingAddress;
    }

    @Data
    public static class UpdateOrderStatusRequest {
        @NotNull(message = "Status is required")
        private OrderStatus status;
    }
}
