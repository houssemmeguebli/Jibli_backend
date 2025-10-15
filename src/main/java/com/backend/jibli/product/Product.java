package com.backend.jibli.product;

import com.backend.jibli.attachment.Attachment;
import com.backend.jibli.cart.CartItem;
import com.backend.jibli.category.Category;
import com.backend.jibli.company.Company;
import com.backend.jibli.order.OrderItem;
import com.backend.jibli.review.Review;
import com.backend.jibli.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;

    private String productName;
    private String productDescription;
    private Double productPrice;

    @Column(name = "discount_percentage")
    private Double discountPercentage = 0.0;

    private boolean isAvailable;
    private LocalDateTime lastUpdated;

    @ManyToOne
    @JoinColumn(name = "categoryId")
    @JsonIgnoreProperties({"products", "attachments", "user", "company"}) // CRITICAL FIX
    private Category category;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"product"})
    private List<Attachment> attachments;

    @OneToMany(mappedBy = "product")
    @JsonIgnoreProperties({"product"})
    private List<Review> reviews;

    @OneToMany(mappedBy = "product")
    @JsonIgnoreProperties({"product"})
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "product")
    @JsonIgnoreProperties({"product", "cart"})
    private List<CartItem> cartItems;

    @ManyToOne
    @JoinColumn(name = "userId")
    @JsonIgnoreProperties({"products", "carts", "orders", "reviews"})
    private User user;

    @ManyToOne
    @JoinColumn(name = "companyId")
    @JsonIgnoreProperties({"products", "carts", "categories"})
    private Company company;

    // Transient field - calculated on the fly, not stored in DB
    @Transient
    @JsonProperty("productFinalePrice")
    private Double productFinalePrice;

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.lastUpdated = LocalDateTime.now();

        if (this.discountPercentage == null) {
            this.discountPercentage = 0.0;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = LocalDateTime.now();

        if (this.discountPercentage == null) {
            this.discountPercentage = 0.0;
        }
    }

    @PostLoad
    @PostPersist
    @PostUpdate
    public void calculateFinalPrice() {
        if (this.productPrice == null) {
            this.productFinalePrice = 0.0;
            return;
        }

        if (discountPercentage == null || discountPercentage <= 0) {
            this.productFinalePrice = this.productPrice;
        } else {
            double validDiscount = Math.min(discountPercentage, 100.0);
            this.productFinalePrice = this.productPrice * (1 - validDiscount / 100);
            this.productFinalePrice = Math.round(this.productFinalePrice * 100.0) / 100.0;
        }
    }

    public Double getProductFinalePrice() {
        calculateFinalPrice();
        return this.productFinalePrice;
    }

    public void setDiscountPercentage(Double discountPercentage) {
        if (discountPercentage == null) {
            this.discountPercentage = 0.0;
        } else if (discountPercentage < 0) {
            this.discountPercentage = 0.0;
        } else if (discountPercentage > 100) {
            this.discountPercentage = 100.0;
        } else {
            this.discountPercentage = discountPercentage;
        }
        calculateFinalPrice();
    }

    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice;
        calculateFinalPrice();
    }
}