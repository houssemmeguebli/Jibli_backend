package com.backend.jibli.cart;

import com.backend.jibli.product.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Integer cartItemId;
    private Integer productId;
    private Integer quantity;
    private Product product;
}