package com.backend.jibli.product;


import com.backend.jibli.attachment.Attachment;
import com.backend.jibli.cart.CartItem;
import com.backend.jibli.category.Category;
import com.backend.jibli.company.Company;
import com.backend.jibli.order.OrderItem;
import com.backend.jibli.review.Review;
import com.backend.jibli.user.User;
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
    private String productDescription ;
    private Double productPrice ;
    private boolean  isAvailable;
    private LocalDateTime lastUpdated ;
    private Double discountPercentage = 0.0;

    @ManyToOne
    @JoinColumn(name = "categoryId")
    private Category category;

    private LocalDateTime createdAt ;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Attachment> attachments;

    @OneToMany(mappedBy = "product")
    private List<Review> reviews;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "product")
    private List<CartItem> cartItems;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne
    @JoinColumn(name="companyId")
    private Company company;

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}