package com.backend.jibli.company;

import com.backend.jibli.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IUserCompanyRepository extends JpaRepository<UserCompany, Integer> {
    boolean existsByUserUserIdAndCompanyCompanyId(Integer userId, Integer companyId);
    List<UserCompany> findByUserUserId(Integer userId);
    List<UserCompany> findByCompanyCompanyId(Integer companyId);
    @Query("SELECT uc.user FROM UserCompany uc WHERE uc.company.companyId = :companyId")
    List<User> findUsersByCompanyId(@Param("companyId") Integer companyId);

    /**
     * Find users with specific role in a company
     */
    @Query("SELECT uc.user FROM UserCompany uc WHERE uc.company.companyId = :companyId AND uc.role = :role")
    List<User> findUsersByCompanyIdAndRole(@Param("companyId") Integer companyId, @Param("role") String role);

    /**
     * Find specific user-company relationship
     */
    Optional<UserCompany> findByUserUserIdAndCompanyCompanyId(Integer userId, Integer companyId);
}