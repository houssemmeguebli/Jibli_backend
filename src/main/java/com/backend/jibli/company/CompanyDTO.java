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
    private LocalDateTime companyCreatedAt;
    private List<Integer> userCompanyIds;
}