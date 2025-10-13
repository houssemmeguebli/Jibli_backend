package com.backend.jibli.company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCompanyDTO {
    private Integer userCompanyId;
    private Integer userId;
    private Integer companyId;
    private String role;
}