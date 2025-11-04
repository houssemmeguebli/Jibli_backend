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
    private LocalDateTime deliveredDate;
    private LocalDateTime inPreparationDate;
    private LocalDateTime pickedUpDate;
    private LocalDateTime waitingDate;
    private LocalDateTime acceptedDate;
    private LocalDateTime canceledDate;
    private String customerName;
    private String customerEmail;
    private String customerAddress;
    private String customerPhone;
    private String orderNotes;
    private Integer totalProducts;
    private Integer quantity;
    private Double discount;
    private Double totalAmount;
    private Double deliveryFee;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "userId")
    @JsonBackReference
    private User user;

    @OneToMany(mappedBy = "order")
    @JsonIgnore
    private List<OrderItem> orderItems;

    private LocalDateTime lastUpdated;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="companyId")
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
        if (this.orderStatus == null) {
            this.orderStatus = OrderStatus.PENDING;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
        updateStatusTimestamp();
    }


    private void updateStatusTimestamp() {
        if (this.orderStatus != null) {
            LocalDateTime now = LocalDateTime.now();
            switch (this.orderStatus) {
                case IN_PREPARATION:
                    this.inPreparationDate = now;
                    break;
                case PICKED_UP:
                    this.pickedUpDate = now;
                    this.shippedDate = now;
                    break;
                case WAITING:
                    this.waitingDate = now;
                    break;
                case ACCEPTED:
                    this.acceptedDate = now;
                    break;
                case DELIVERED:
                    this.deliveredDate = now;
                    break;
                case CANCELED:
                    this.canceledDate = now;
                    break;
                default:
                    break;
            }
        }
    }

    public void setOrderStatus(OrderStatus newStatus) {
        this.orderStatus = newStatus;
        updateStatusTimestamp();
    }
}