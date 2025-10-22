package com.backend.jibli.user;

import com.backend.jibli.attachment.Attachment;
import com.backend.jibli.cart.Cart;
import com.backend.jibli.category.Category;
import com.backend.jibli.company.UserCompany;
import com.backend.jibli.order.Order;
import com.backend.jibli.product.Product;
import com.backend.jibli.review.Review;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    private String fullName;

    @Column(unique = true)
    private String email;

    private String phone;
    private String address;
    private String gender;
    private Date dateOfBirth;
    private String password;
    private UserRole userRole;
    private UserStatus userStatus;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private boolean isAvailable;

    // ✅ ONLY include orders, everything else is ignored
    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<Order> orders;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Product> products;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Cart> carts;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Review> reviews;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Category> categories;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Attachment> attachments;

    @OneToMany(mappedBy = "user")
    @JsonIgnore  // ✅ This is the key fix - was causing infinite loop
    private List<UserCompany> userCompanies;

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