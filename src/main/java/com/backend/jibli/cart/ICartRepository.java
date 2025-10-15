package com.backend.jibli.cart;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ICartRepository extends JpaRepository<Cart,Integer> {
    boolean existsByUserUserId(Integer userId);
    Optional<Cart> findByUserUserId(Integer userId);

}
