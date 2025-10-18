package com.backend.jibli.cart;

import com.backend.jibli.company.Company;
import com.backend.jibli.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartId;

    @ManyToOne
    @JoinColumn(name = "userId")
    @JsonIgnoreProperties({"carts", "orders", "products", "reviews", "categories", "attachments", "userCompanies"})
    private User user;

    @ManyToOne
    @JoinColumn(name = "companyId")
    @JsonIgnoreProperties({"carts", "products", "categories", "userCompanies"})
    private Company company;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<CartItem> cartItems;

    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private double totalPrice;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    public double getTotalPrice() {
        if (cartItems == null || cartItems.isEmpty()) {
            return 0.0;
        }
        return cartItems.stream()
                .mapToDouble(item -> {
                    if (item.getProduct() != null && item.getQuantity() != null) {
                        return item.getProduct().getProductFinalePrice() * item.getQuantity();
                    }
                    return 0.0;
                })
                .sum();
    }
}