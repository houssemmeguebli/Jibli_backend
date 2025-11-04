package com.backend.jibli.company;

import com.backend.jibli.cart.Cart;
import com.backend.jibli.order.Order;
import com.backend.jibli.product.Product;
import com.backend.jibli.review.Review;
import com.backend.jibli.category.Category;
import com.backend.jibli.attachment.Attachment;
import com.backend.jibli.user.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Integer companyId;

    @Column(name = "name")
    private String companyName;

    @Column(name = "description")
    private String companyDescription;

    @Column(name = "sector")
    private String companySector;

    @Column(name = "company_address")
    private String companyAddress;

    @Column(name = "company_phone")
    private String companyPhone;

    @Column(name = "company_email")
    private String companyEmail;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "company_status")
    private CompanyStatus companyStatus = CompanyStatus.INACTIVE;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "delivery_fee")
    private Double deliveryFee;


    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UserCompany> userCompanies;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> products;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Category> categories;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Attachment> attachments;

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Review> reviews;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    @JsonIgnore
    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Cart> carts;
    @Column(name = "time_open")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime timeOpen;

    @Column(name = "time_close")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime timeClose;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id")
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