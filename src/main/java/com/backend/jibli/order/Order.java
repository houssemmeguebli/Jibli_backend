package com.backend.jibli.order;
import com.backend.jibli.company.Company;
import com.backend.jibli.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;
    private LocalDateTime orderDate = LocalDateTime.now();
    private LocalDateTime shippedDate;
    private String customerName;
    private String customerEmail;
    private String customerAddress;
    private String customerPhone;
    private String orderNotes;
    private Integer totalProducts;
    private Integer quantity;
    private Double discount;
    private Double totalAmount;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt ;

    @ManyToOne
    @JoinColumn(name = "userId")
    @JsonBackReference
    private User user;

    @OneToMany(mappedBy = "order")
    @JsonIgnore
    private List<OrderItem> orderItems;

    private LocalDateTime lastUpdated;
    @ManyToOne
    @JoinColumn(name="companyId")
    @JsonIgnore
    private Company company;

    @ManyToOne
    @JoinColumn(name = "deliveryId")
    @JsonIgnore
    private User delivery;

    @ManyToOne
    @JoinColumn(name = "assignedById")
    @JsonIgnore
    private User assignedBy;


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


