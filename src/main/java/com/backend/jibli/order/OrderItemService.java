package com.backend.jibli.order;

import com.backend.jibli.product.IProductRepository;
import com.backend.jibli.product.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderItemService implements IOrderItemService {

    private final IOrderItemRepository orderItemRepository;
    private final IOrderRepository orderRepository;
    private final IProductRepository productRepository;

    @Autowired
    public OrderItemService(IOrderItemRepository orderItemRepository, IOrderRepository orderRepository, IProductRepository productRepository) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<OrderItemDTO> getAllOrderItems() {
        return orderItemRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<OrderItemDTO> getOrderItemById(Integer id) {
        return orderItemRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public OrderItemDTO createOrderItem(OrderItemDTO dto) {
        if (dto.getOrderId() == null) {
            throw new IllegalArgumentException("Order ID is required");
        }
        if (dto.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (!orderRepository.existsById(dto.getOrderId())) {
            throw new IllegalArgumentException("Order not found");
        }
        if (!productRepository.existsById(dto.getProductId())) {
            throw new IllegalArgumentException("Product not found");
        }
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (dto.getUnitPrice() == null || dto.getUnitPrice() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        OrderItem orderItem = mapToEntity(dto);
        OrderItem saved = orderItemRepository.save(orderItem);
        return mapToDTO(saved);
    }

    @Override
    public Optional<OrderItemDTO> updateOrderItem(Integer id, OrderItemDTO dto) {
        if (dto.getOrderId() != null && !orderRepository.existsById(dto.getOrderId())) {
            throw new IllegalArgumentException("Order not found");
        }
        if (dto.getProductId() != null && !productRepository.existsById(dto.getProductId())) {
            throw new IllegalArgumentException("Product not found");
        }
        if (dto.getQuantity() != null && dto.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (dto.getUnitPrice() != null && dto.getUnitPrice() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        return orderItemRepository.findById(id)
                .map(orderItem -> {
                    if (dto.getOrderId() != null) {
                        Order order = new Order();
                        order.setOrderId(dto.getOrderId());
                        orderItem.setOrder(order);
                    }
                    if (dto.getProductId() != null) {
                        Product product = new Product();
                        product.setProductId(dto.getProductId());
                        orderItem.setProduct(product);
                    }
                    if (dto.getQuantity() != null) orderItem.setQuantity(dto.getQuantity());
                    if (dto.getUnitPrice() != null) orderItem.setUnitPrice(dto.getUnitPrice());
                    OrderItem updated = orderItemRepository.save(orderItem);
                    return mapToDTO(updated);
                });
    }

    @Override
    public boolean deleteOrderItem(Integer id) {
        if (orderItemRepository.existsById(id)) {
            orderItemRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<OrderItemDTO> getOrderItemsByOrder(Integer orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new IllegalArgumentException("Order not found");
        }
        return orderItemRepository.findByOrderOrderId(orderId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderItemDTO> getOrderItemsByProduct(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Product not found");
        }
        return orderItemRepository.findByProductProductId(productId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private OrderItemDTO mapToDTO(OrderItem orderItem) {
        return new OrderItemDTO(
                orderItem.getOrderItemId(),
                orderItem.getOrder() != null ? orderItem.getOrder().getOrderId() : null,
                orderItem.getProduct() != null ? orderItem.getProduct().getProductId() : null,
                orderItem.getQuantity(),
                orderItem.getUnitPrice()
        );
    }

    private OrderItem mapToEntity(OrderItemDTO dto) {
        OrderItem orderItem = new OrderItem();
        if (dto.getOrderId() != null) {
            Order order = new Order();
            order.setOrderId(dto.getOrderId());
            orderItem.setOrder(order);
        }
        if (dto.getProductId() != null) {
            Product product = new Product();
            product.setProductId(dto.getProductId());
            orderItem.setProduct(product);
        }
        orderItem.setQuantity(dto.getQuantity());
        orderItem.setUnitPrice(dto.getUnitPrice());
        return orderItem;
    }
}