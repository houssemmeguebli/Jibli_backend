package com.backend.jibli.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IReviewRepository extends JpaRepository<Review,Integer> {
    boolean existsByUserUserIdAndProductProductId(Integer userId, Integer productId);
    List<Review> findByProductProductId(Integer productId);
}
