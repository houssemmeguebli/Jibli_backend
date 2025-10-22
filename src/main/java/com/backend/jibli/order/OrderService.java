package com.backend.jibli.order;

import com.backend.jibli.company.Company;
import com.backend.jibli.user.IUserRepository;
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
            throw new IllegalArgumentException("Order status is required");
        }

        Order order = mapToEntity(dto);
        Order saved = orderRepository.save(order);
        return mapToDTO(saved);
    }


    @Override
    public Optional<OrderDTO> updateOrder(Integer id, OrderDTO dto) {
        return orderRepository.findById(id)
                .map(order -> {
                    // Update only non-null fields
                    if (dto.getOrderStatus() != null) {
                        order.setOrderStatus(dto.getOrderStatus());
                    }
                    if (dto.getCustomerName() != null) {
                        order.setCustomerName(dto.getCustomerName());
                    }
                    if (dto.getTotalAmount() != null) {
                        order.setTotalAmount(dto.getTotalAmount());
                    }
                    if (dto.getQuantity() != null) {
                        order.setQuantity(dto.getQuantity());
                    }
                    if (dto.getCustomerEmail() != null) {
                        order.setCustomerEmail(dto.getCustomerEmail());
                    }
                    if (dto.getCustomerPhone() != null) {
                        order.setCustomerPhone(dto.getCustomerPhone());
                    }
                    if (dto.getCustomerAddress() != null) {
                        order.setCustomerAddress(dto.getCustomerAddress());
                    }

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

    @Override
    public List<OrderDTO> findOrderByUserId(Integer userId) {
        return orderRepository.findByUserUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ⚠️ FIXED: These two must return Entities, not DTOs
    @Override
    public List<OrderDTO> findByIdWithProducts(Integer orderId) {
        return orderRepository.findByIdWithProducts(orderId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

    }

    @Override
    public List<OrderDTO> findAllWithProducts() {
        return orderRepository.findAllWithProducts().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> findOrdersByDeliveryId(Integer deliveryId) {
        return orderRepository.findOrdersByDeliveryId(deliveryId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    // ✅ Mapping Order → DTO
    private OrderDTO mapToDTO(Order order) {
        if (order == null) return null;

        List<Integer> orderItemIds = order.getOrderItems() != null
                ? order.getOrderItems().stream()
                .map(OrderItem::getOrderItemId)
                .collect(Collectors.toList())
                : List.of();

        return new OrderDTO(
                order.getOrderId(),
                order.getUser() != null ? order.getUser().getUserId() : null,
                order.getCompany() != null ? order.getCompany().getCompanyId() : null,
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getCustomerAddress(),
                order.getCustomerPhone(),
                order.getOrderNotes(),
                order.getTotalProducts(),
                order.getQuantity(),
                order.getDiscount(),
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getOrderDate(),
                order.getShippedDate(),
                order.getCreatedAt(),
                order.getLastUpdated(),
                orderItemIds,
                order.getDelivery() != null ? order.getDelivery().getUserId() : null,
                order.getAssignedBy() != null ? order.getAssignedBy().getUserId() : null
        );
    }

    // ✅ Mapping DTO → Order
    private Order mapToEntity(OrderDTO dto) {
        if (dto == null) return null;

        Order order = new Order();
        order.setOrderId(dto.getOrderId());
        order.setCustomerName(dto.getCustomerName());
        order.setCustomerEmail(dto.getCustomerEmail());
        order.setCustomerAddress(dto.getCustomerAddress());
        order.setCustomerPhone(dto.getCustomerPhone());
        order.setOrderNotes(dto.getOrderNotes());
        order.setTotalProducts(dto.getTotalProducts());
        order.setQuantity(dto.getQuantity());
        order.setDiscount(dto.getDiscount());
        order.setTotalAmount(dto.getTotalAmount());
        order.setOrderStatus(dto.getOrderStatus());
        order.setOrderDate(dto.getOrderDate());
        order.setShippedDate(dto.getShippedDate());
        order.setCreatedAt(dto.getCreatedAt());
        order.setLastUpdated(dto.getLastUpdated());


        if (dto.getUserId() != null) {
            userRepository.findById(dto.getUserId()).ifPresent(order::setUser);
        }

        if (dto.getCompanyId() != null) {
            Company company = new Company();
            company.setCompanyId(dto.getCompanyId());
            order.setCompany(company);
        }
        if (dto.getDeliveryId() != null) {
            userRepository.findById(dto.getDeliveryId()).ifPresent(order::setDelivery);
        } else {
            order.setDelivery(null);
        }

        if (dto.getAssignedById() != null) {
            userRepository.findById(dto.getAssignedById()).ifPresent(order::setAssignedBy);
        } else {
            order.setAssignedBy(null);
        }
        return order;
    }
}
