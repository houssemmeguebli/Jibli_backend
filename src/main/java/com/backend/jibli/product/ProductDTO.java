package com.backend.jibli.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Integer productId;
    private String productName;
    private String productDescription;
    private Double productPrice;
    private Double productFinalePrice ;
    private boolean isAvailable;
    private Double discountPercentage = 0.0;
    private Integer categoryId;
    private  Integer userId;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private List<Integer> attachmentIds;
    private List<Integer> reviewIds;
    private List<Integer> orderItemIds;
    private List<Map<String, Object>> attachments;



}