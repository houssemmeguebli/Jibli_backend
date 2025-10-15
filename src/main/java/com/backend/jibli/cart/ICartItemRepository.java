package com.backend.jibli.cart;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ICartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCartCartId(Integer cartId);
    Optional<CartItem> findByCartCartIdAndProductProductId(Integer cartId, Integer productId);
    boolean existsByCartCartId(Integer cartId);
    void deleteByCartCartId(Integer cartId);
}
