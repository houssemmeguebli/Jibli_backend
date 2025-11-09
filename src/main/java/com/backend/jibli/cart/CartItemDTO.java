package com.backend.jibli.cart;

import com.backend.jibli.product.Product;
import com.backend.jibli.product.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Integer cartItemId;
    private Integer cartId;
    private Integer productId;
    private Integer quantity;
    private ProductDTO product;
}