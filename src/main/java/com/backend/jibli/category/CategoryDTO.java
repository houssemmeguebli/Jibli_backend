package com.backend.jibli.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Integer categoryId;
    private String name;
    private String description;
    private Integer iconId;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private List<Integer> attachmentIds;
    private List<Integer> productIds;
    private Integer userId;
    private Integer companyId;

}