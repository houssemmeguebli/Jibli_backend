package com.backend.jibli.order;

import java.util.List;
import java.util.Optional;

public interface IOrderItemService {
    List<OrderItemDTO> getAllOrderItems();
    Optional<OrderItemDTO> getOrderItemById(Integer id);
    OrderItemDTO createOrderItem(OrderItemDTO dto);
    Optional<OrderItemDTO> updateOrderItem(Integer id, OrderItemDTO dto);
    boolean deleteOrderItem(Integer id);
    List<OrderItemDTO> getOrderItemsByOrder(Integer orderId);
    List<OrderItemDTO> getOrderItemsByProduct(Integer productId);

}