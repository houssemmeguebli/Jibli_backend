package com.backend.jibli.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer userId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String gender;
    private Date dateOfBirth;
    private String password;
    private String userRole;
    private String userStatus;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private List<Integer> userCompanyIds;
    private boolean isAvailable;

}