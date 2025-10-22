package com.backend.jibli.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IUserRepository extends JpaRepository<User, Integer> {
    boolean existsByFullName(String username);
    boolean existsByEmail(String email);

    List<User> findAllByUserRole(UserRole userRole);
}