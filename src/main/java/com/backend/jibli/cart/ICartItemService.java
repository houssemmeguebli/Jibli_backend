package com.backend.jibli.cart;

import java.util.List;
import java.util.Optional;

public interface ICartItemService {
    List<CartItemDTO> getAllCartItems();

    List<CartItemDTO> getCartItemsByCartId(Integer cartId);

    Optional<CartItemDTO> getCartItemById(Integer id);

    CartItemDTO createCartItem(CartItemDTO dto);

    Optional<CartItemDTO> updateCartItem(Integer id, CartItemDTO dto);

    boolean deleteCartItem(Integer id);

    void deleteCartItemsByCartId(Integer cartId);

    CartItemDTO addProductToUserCart(Integer userId, CartItemDTO dto);

}



