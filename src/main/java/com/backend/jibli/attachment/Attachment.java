package com.backend.jibli.attachment;

import com.backend.jibli.category.Category;
import com.backend.jibli.company.Company;
import com.backend.jibli.product.Product;
import com.backend.jibli.user.User;
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
    private String fileName;
    private String fileType;
    @Lob
    private byte[] data;
    private String entityType;
    private Integer entityId;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastUpdated;
    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.lastUpdated = LocalDateTime.now();
    }



    @ManyToOne
    @JoinColumn(name = "productId")
    private Product product;
    @ManyToOne
    @JoinColumn(name = "categoryId")
    private Category category;
    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;
    @ManyToOne
    @JoinColumn(name="companyId")
    private Company company;

   @PreUpdate
    public void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }


}