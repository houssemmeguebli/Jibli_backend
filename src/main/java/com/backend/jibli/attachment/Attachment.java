package com.backend.jibli.attachment;

import com.backend.jibli.category.Category;
import com.backend.jibli.company.Company;
import com.backend.jibli.product.Product;
import com.backend.jibli.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer attachmentId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] data;

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private Integer entityId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastUpdated;

    // JPA Relationship - only for Product entities
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entityId", referencedColumnName = "productId",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "FK_attachment_product"))
    @JsonIgnore
    @JsonBackReference
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryId", insertable = false, updatable = false)
    @JsonIgnore
    @JsonBackReference
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    @JsonIgnore
    @JsonBackReference
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "companyId", insertable = false, updatable = false)
    @JsonIgnore
    private Company company;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.lastUpdated = now;
        syncForeignKeys();
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
        syncForeignKeys();
    }

    // Sync the specific foreign key columns based on entityType and entityId
    private void syncForeignKeys() {
        if (entityType != null && entityId != null) {
            switch (entityType.toUpperCase()) {
                case "PRODUCT":
                    // JPA will handle the relationship if product is loaded
                    break;
                case "CATEGORY":
                    break;
                case "USER":
                    break;
                case "COMPANY":
                    break;
            }
        }
    }
}