package com.backend.jibli.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IOrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserUserId(Integer userId);
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product")
    List<Order> findAllWithProducts();

    // ✅ Fetch a specific order (with all related entities)
    @Query("""
    SELECT o FROM Order o
    LEFT JOIN FETCH o.orderItems oi
    LEFT JOIN FETCH oi.product
    WHERE o.orderId = :orderId
    """)
    Optional<Order> findByIdWithProducts(@Param("orderId") Integer orderId);

    @Query("""
    SELECT DISTINCT o FROM Order o
    LEFT JOIN FETCH o.orderItems oi
    LEFT JOIN FETCH oi.product
    LEFT JOIN FETCH o.delivery
    LEFT JOIN FETCH o.assignedBy
    WHERE o.delivery.userId = :deliveryId
    """)
    List<Order> findOrdersByDeliveryId(@Param("deliveryId") Integer deliveryId);


}
