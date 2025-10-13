package com.backend.jibli.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Integer reviewId;
    private Integer userId;
    private Integer productId;
    private Integer companyId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
}