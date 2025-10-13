package com.backend.jibli.order;

import java.util.List;
import java.util.Optional;

public interface IOrderService {
    List<OrderDTO> getAllOrders();
    Optional<OrderDTO> getOrderById(Integer id);
    OrderDTO createOrder(OrderDTO dto);
    Optional<OrderDTO> updateOrder(Integer id, OrderDTO dto);
    boolean deleteOrder(Integer id);

    List<OrderItemDTO> getOrderItems(Integer orderId);
}