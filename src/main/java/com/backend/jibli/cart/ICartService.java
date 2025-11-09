package com.backend.jibli.cart;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ICartService {
    List<CartDTO> getAllCarts();
    Optional<CartDTO> getCartById(Integer id);
    CartDTO createCart(CartDTO dto);
    Optional<CartDTO> updateCart(Integer id, CartDTO dto);
    boolean deleteCart(Integer id);
    Optional<CartDTO> findByUserUserId(Integer userId);
    List<Map<String, Object>> getUserCartsGroupedByCompany(Integer userId);
    void deleteByUserUserIdAndCompanyCompanyId(Integer userId, Integer companyId);

}