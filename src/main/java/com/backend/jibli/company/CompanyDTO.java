package com.backend.jibli.company;

import com.backend.jibli.category.CategoryDTO;
import com.backend.jibli.product.ProductDTO;
import com.backend.jibli.review.ReviewDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDTO {
    private Integer companyId;
    private String companyName;
    private String companyDescription;
    private String companySector;
    private String companyAddress;
    private String companyPhone;
    private String companyEmail;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private CompanyStatus companyStatus;
    private Double averageRating;
    private Double  deliveryFee;
    private List<ProductDTO> products;
    private List<ReviewDTO> reviews;
    private List<CategoryDTO> categories;
    private LocalTime timeOpen;
    private LocalTime timeClose;
    private Integer userId;


    public CompanyDTO(
            Integer companyId,
            String companyName,
            String companyDescription,
            String companySector,
            String companyAddress,
            String companyPhone,
            LocalTime timeOpen,
            LocalTime timeClose,
            Double averageRating

    ) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyDescription = companyDescription;
        this.companySector = companySector;
        this.companyAddress = companyAddress;
        this.companyPhone = companyPhone;
        this.timeOpen = timeOpen;
        this.timeClose = timeClose;
        this.averageRating = averageRating;
    }




}