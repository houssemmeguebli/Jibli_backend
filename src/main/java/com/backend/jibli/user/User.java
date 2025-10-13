package com.backend.jibli.user;

import com.backend.jibli.attachment.Attachment;
import com.backend.jibli.cart.Cart;
import com.backend.jibli.category.Category;
import com.backend.jibli.company.UserCompany;
import com.backend.jibli.order.Order;
import com.backend.jibli.product.Product;
import com.backend.jibli.review.Review;
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

    @Column(unique = true)
    private String fullName;

    @Column(unique = true)
    private String email;
    
    private String phone;

    private String address;

    private String gender;

    private Date dateOfBirth;

    private String password;

    private UserRole userRole ;
    
    private UserStatus userStatus;

    private LocalDateTime createdAt ;

    private LocalDateTime lastUpdated;

    @OneToMany(mappedBy = "user")
    private List<Order> orders;

    @OneToMany(mappedBy = "user")
    private List<Product> products;

    @OneToMany(mappedBy = "user")
    private List<Cart> carts;

    @OneToMany(mappedBy="user")
    private List<Review> reviews;

    @OneToMany(mappedBy = "user")
    private List<Category> categories;

    @OneToMany(mappedBy = "user")
    private List<Attachment> attachments;

    @OneToMany(mappedBy = "user")
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

