package com.backend.jibli.company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IUserCompanyRepository extends JpaRepository<UserCompany, Integer> {
    boolean existsByUserUserIdAndCompanyCompanyId(Integer userId, Integer companyId);
    List<UserCompany> findByUserUserId(Integer userId);
    List<UserCompany> findByCompanyCompanyId(Integer companyId);
}