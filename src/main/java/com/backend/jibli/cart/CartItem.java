package com.backend.jibli.cart;

import com.backend.jibli.product.Product;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartItemId;

    @ManyToOne
    @JoinColumn(name = "cartId")
    @JsonBackReference
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "productId")
    @JsonBackReference
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler",
            "attachments",
            "reviews",
            "orderItems",
            "cartItems",
            "user",
            "company"
    })
    private Product product;

    private Integer quantity;
}