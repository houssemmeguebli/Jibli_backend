package com.backend.jibli.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final IOrderService orderService;

    @Autowired
    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO dto) {
        try {
            OrderDTO created = orderService.createOrder(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Integer id, @RequestBody OrderDTO dto) {
        try {
            return orderService.updateOrder(id, dto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer id) {
        boolean deleted = orderService.deleteOrder(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/order-items")
    public ResponseEntity<List<OrderItemDTO>> getOrderItems(@PathVariable Integer id) {
        try {
            List<OrderItemDTO> orderItems = orderService.getOrderItems(id);
            return ResponseEntity.ok(orderItems);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUserId(@PathVariable Integer userId) {
        List<OrderDTO> orders = orderService.findOrderByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orderProducs")
    public ResponseEntity<List<OrderDTO>> findAllWithProducts() {
        List<OrderDTO> orders = orderService.findAllWithProducts();
        return ResponseEntity.ok(orders);
    }
    @GetMapping("/orderProducts/{orderId}")
    public  ResponseEntity<List<OrderDTO>> findByIdWithProducts(@PathVariable Integer orderId) {
        List<OrderDTO> orders = orderService.findByIdWithProducts(orderId);
        return ResponseEntity.ok(orders);

    }
    @PatchMapping("/orderStatus/{id}")
    public ResponseEntity<OrderDTO> patchOrder(@PathVariable Integer id, @RequestBody OrderDTO dto) {
        try {
            return orderService.updateOrder(id, dto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @GetMapping("/deliveryOrders/{deliveryId}")
    public  ResponseEntity<List<OrderDTO>> findOrdersByDeliveryId(@PathVariable Integer deliveryId) {
        List<OrderDTO> orders = orderService.findOrdersByDeliveryId(deliveryId);
        return ResponseEntity.ok(orders);

    }
    @GetMapping("/companyOrders/{companyId}")
    public  ResponseEntity<List<OrderDTO>> findOrdersByCompanyCompanyId(@PathVariable Integer companyId) {
        List<OrderDTO> orders = orderService.findOrdersByCompanyCompanyId(companyId);
        return ResponseEntity.ok(orders);

    }


}