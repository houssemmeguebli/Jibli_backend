package com.backend.jibli.company;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDTO {
    private Integer companyId;
    private String companyName;
    private String companyDescription;
    private String companySector;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private List<Integer> userIds;
    private String imageUrl;
    private Double averageRating;
    private Integer userId;
}