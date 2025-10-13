package com.backend.jibli.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRepository extends JpaRepository<User, Integer> {
    boolean existsByFullName(String username);
    boolean existsByEmail(String email);
}