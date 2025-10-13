package com.backend.jibli.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Integer productId;
    private String productName;
    private String productDescription;
    private Double productPrice;
    private boolean isAvailable;
    private Double discountPercentage = 0.0;
    private Integer categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private List<Integer> attachmentIds;
    private List<Integer> reviewIds;
    private List<Integer> orderItemIds;
}