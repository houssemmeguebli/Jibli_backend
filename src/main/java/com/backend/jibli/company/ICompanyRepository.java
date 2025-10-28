package com.backend.jibli.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ICompanyRepository extends JpaRepository<Company,Integer> {

    List<Company> findByUserUserId(Integer userId);

    @Query("SELECT DISTINCT c FROM Company c LEFT JOIN FETCH c.products WHERE c.user.userId = :userId")
    List<Company> findByUserUserIdWithProducts(@Param("userId") Integer userId);


    @Query("SELECT c FROM Company c " +
            "LEFT JOIN FETCH c.products " +
            "WHERE c.companyId = :companyId")
    Company findByCompanyIdWithProducts(@Param("companyId") Integer companyId);

    @Query("SELECT c FROM Company c " +
            "LEFT JOIN FETCH c.reviews " +
            "WHERE c.companyId = :companyId")
    Company findByCompanyIdWithReviews(@Param("companyId") Integer companyId);

    @Query("SELECT c FROM Company c " +
            "LEFT JOIN FETCH c.categories " +
            "WHERE c.companyId = :companyId")
    Company findByCompanyIdWithCategories(@Param("companyId") Integer companyId);
}
