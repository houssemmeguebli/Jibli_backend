package com.backend.jibli.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Integer orderId;
    private Integer userId;
    private Integer companyId;
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
    private LocalDateTime orderDate;
    private LocalDateTime shippedDate;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private List<Integer> orderItemIds;
    private Integer deliveryId;
    private Integer assignedById;
    private LocalDateTime deliveredDate;
    private LocalDateTime inPreparationDate;
    private LocalDateTime pickedUpDate;
    private LocalDateTime waitingDate;
    private LocalDateTime acceptedDate;
    private LocalDateTime canceledDate;
}
