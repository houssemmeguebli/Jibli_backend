package com.backend.jibli.order;

import com.backend.jibli.user.IUserRepository;
import com.backend.jibli.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService implements IOrderService {

    private final IOrderRepository orderRepository;
    private final IUserRepository userRepository;
    private final IOrderItemService orderItemService;

    @Autowired
    public OrderService(IOrderRepository orderRepository, IUserRepository userRepository, IOrderItemService orderItemService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderItemService = orderItemService;
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<OrderDTO> getOrderById(Integer id) {
        return orderRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public OrderDTO createOrder(OrderDTO dto) {
        if (dto.getOrderStatus() == null) {
            throw new IllegalArgumentException("Status is required");
        }
        Order order = mapToEntity(dto);
        Order saved = orderRepository.save(order);
        return mapToDTO(saved);
    }

    @Override
    public Optional<OrderDTO> updateOrder(Integer id, OrderDTO dto) {
        if (dto.getUserId() != null && !userRepository.existsById(dto.getUserId())) {
            throw new IllegalArgumentException("User not found");
        }
        return orderRepository.findById(id)
                .map(order -> {
                    if (dto.getUserId() != null) {
                        User user = new User();
                        user.setUserId(dto.getUserId());
                        order.setUser(user);
                    }
                    if (dto.getOrderStatus() != null) order.setOrderStatus(dto.getOrderStatus());
                    Order updated = orderRepository.save(order);
                    return mapToDTO(updated);
                });
    }

    @Override
    public boolean deleteOrder(Integer id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<OrderItemDTO> getOrderItems(Integer orderId) {
        return orderItemService.getOrderItemsByOrder(orderId);
    }

    private OrderDTO mapToDTO(Order order) {
        List<Integer> orderItemIds = order.getOrderItems() != null
                ? order.getOrderItems().stream()
                .map(OrderItem::getOrderItemId)
                .collect(Collectors.toList())
                : List.of();
        return new OrderDTO(
        );
    }

    private Order mapToEntity(OrderDTO dto) {
        Order order = new Order();

        if (dto.getUserId() != null) {
            User user = new User();
            user.setUserId(dto.getUserId());
            order.setUser(user);
        }

        order.setOrderStatus(dto.getOrderStatus());
        return order;
    }

}