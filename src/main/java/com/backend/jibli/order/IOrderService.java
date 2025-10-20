package com.backend.jibli.order;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IOrderService {
    List<OrderDTO> getAllOrders();
    Optional<OrderDTO> getOrderById(Integer id);
    OrderDTO createOrder(OrderDTO dto);
    Optional<OrderDTO> updateOrder(Integer id, OrderDTO dto);
    boolean deleteOrder(Integer id);

    List<OrderItemDTO> getOrderItems(Integer orderId);
    List<OrderDTO> findOrderByUserId(Integer userId);
    List<OrderDTO> findByIdWithProducts(@Param("orderId") Integer orderId);
    List<OrderDTO> findAllWithProducts();
    List<OrderDTO> findOrdersByDeliveryId(@Param("deliveryId") Integer deliveryId);


}