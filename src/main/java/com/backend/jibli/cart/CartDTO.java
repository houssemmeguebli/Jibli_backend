package com.backend.jibli.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Integer cartId;
    private Integer userId;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private List<CartItemDTO> cartItems;
    private double totalPrice;
}
