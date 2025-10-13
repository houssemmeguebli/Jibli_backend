package com.backend.jibli.cart;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ICartRepository extends JpaRepository<Cart,Integer> {
    boolean existsByUserUserId(Integer userId);

}
