package com.backend.jibli.company;

import com.backend.jibli.cart.Cart;
import com.backend.jibli.order.Order;
import com.backend.jibli.product.Product;
import com.backend.jibli.review.Review;
import com.backend.jibli.category.Category;
import com.backend.jibli.attachment.Attachment;


import com.backend.jibli.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer companyId;
    private String companyName;
    private String companyDescription;
    private String companySector;
    private LocalDateTime createdAt ;
    private LocalDateTime lastUpdated ;

    @OneToMany(mappedBy = "company")
    private List<UserCompany> userCompanies;

    @OneToMany(mappedBy = "company")
    private List<Product> products;
    @OneToMany(mappedBy = "company")
    private List<Category> categories;
    @OneToMany(mappedBy = "company")
    private List<Attachment> attachments;
    @OneToMany(mappedBy = "company")
    private List<Review> reviews;
    @OneToMany(mappedBy = "company")
    private List<Order> orders;
    @OneToMany(mappedBy="company")
    private List<Cart> carts;
    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

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