package com.backend.jibli.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ICartRepository extends JpaRepository<Cart,Integer> {
    boolean existsByUserUserId(Integer userId);
    Optional<Cart> findByUserUserId(Integer userId);
    Optional<Cart> findByUserUserIdAndCompanyCompanyId(Integer userId, Integer companyId);


    @Modifying
    @Query(
            value = "DELETE ci, c " +
                    "FROM carts c " +
                    "LEFT JOIN cart_items ci ON ci.cart_id = c.cart_id " +
                    "WHERE c.user_id = :userId AND c.company_id = :companyId",
            nativeQuery = true
    )
    void deleteByUserUserIdAndCompanyCompanyId(@Param("userId") Integer userId,
                                                @Param("companyId") Integer companyId);

    @Query(value = """
    SELECT 
        comp.name AS companyName,
        comp.company_id AS companyId,
        comp.delivery_fee AS deliveryFee,
        c.cart_id AS cartId,
        c.total_price AS totalPrice,
        CONCAT(
            '[',
            GROUP_CONCAT(
                JSON_OBJECT(
                    'cartItemId', ci.cart_item_id,
                    'productId', p.product_id,
                    'productName', p.product_name,
                    'quantity', ci.quantity,
                    'productPrice', p.product_price,
                    'productFinalePrice', COALESCE(p.product_finale_price, p.product_price)
                )
            ),
            ']'
        ) AS cartItems
    FROM carts c
    JOIN companies comp ON c.company_id = comp.company_id
    JOIN cart_items ci ON c.cart_id = ci.cart_id
    JOIN products p ON ci.product_id = p.product_id
    WHERE c.user_id = :userId
    GROUP BY c.cart_id, comp.name, comp.company_id, comp.delivery_fee, c.total_price
    ORDER BY comp.name ASC
""", nativeQuery = true)
    List<Map<String, Object>> findUserCartsGroupedByCompany(@Param("userId") Integer userId);


}
