package com.backend.jibli.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IProductRepository extends JpaRepository<Product, Integer> {
    @Query("""
    SELECT p
    FROM Product p
    LEFT JOIN FETCH p.attachments
    WHERE p.user.userId = :userId
    ORDER BY p.productId
    """)
    List<Product> findByUserUserId(Integer userId);
}