package com.backend.jibli.category;

import com.backend.jibli.attachment.Attachment;
import com.backend.jibli.product.Product;
import com.backend.jibli.user.User;
import com.backend.jibli.company.Company;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;

    private String name;
    private String description;
    private Integer iconId;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;

    @ManyToOne
    @JoinColumn(name = "userId")
    @JsonIgnoreProperties({"categories", "products", "carts", "orders"})
    private User user;

    @ManyToOne
    @JoinColumn(name = "companyId")
    @JsonIgnoreProperties({"categories", "products", "carts"})
    private Company company;

    @OneToMany(mappedBy = "entityId", cascade = CascadeType.ALL)
    @JsonIgnore  // Ignore to prevent deep nesting
    private List<Attachment> attachments;

    @OneToMany(mappedBy = "category")
    @JsonIgnore  // CRITICAL FIX - This breaks the circular reference
    private List<Product> products;

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