package com.backend.jibli.order;

import com.backend.jibli.cart.ICartService;
import com.backend.jibli.company.Company;
import com.backend.jibli.company.CompanyDTO;
import com.backend.jibli.company.ICompanyRepository;
import com.backend.jibli.company.IUserCompanyRepository;
import com.backend.jibli.notification.NotificationService;
import com.backend.jibli.user.IUserRepository;
import com.backend.jibli.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OrderService implements IOrderService {

    private final IOrderRepository orderRepository;
    private final IUserRepository userRepository;
    private final ICompanyRepository companyRepository;
    private final IOrderItemService orderItemService;
    private final ICartService cartService;
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String CURRENCY = "TND"; // Tunisian Dinar

    @Autowired
    private NotificationService notificationService;

    @Autowired(required = false)
    private IUserCompanyRepository userCompanyRepository;

    @Autowired
    public OrderService(IOrderRepository orderRepository, IUserRepository userRepository,
                        ICompanyRepository companyRepository,
                        IOrderItemService orderItemService, ICartService cartService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.orderItemService = orderItemService;
        this.cartService = cartService;
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

        // ⚙️ Handle delivery fee properly
        double deliveryFee = 0.0;

        if (dto.getDeliveryFee() != null && dto.getDeliveryFee() > 0) {
            deliveryFee = dto.getDeliveryFee();
        } else if (dto.getCompanyId() != null) {
            Optional<Company> companyOpt = companyRepository.findById(dto.getCompanyId());
            if (companyOpt.isPresent()) {
                Company company = companyOpt.get();
                deliveryFee = company.getDeliveryFee() != null ? company.getDeliveryFee() : 0.0;
                order.setCompany(company);
            }
        }

        order.setDeliveryFee(deliveryFee);

        double subtotal = dto.getTotalAmount() != null ? dto.getTotalAmount() : 0.0;
        double finalTotal = subtotal + deliveryFee;
        order.setTotalAmount(finalTotal);

        log.info("📦 Creating order - Subtotal: {}, DeliveryFee: {}, Total: {}",
                subtotal, deliveryFee, finalTotal);

        order.setOrderStatus(OrderStatus.PENDING);

        Order saved = orderRepository.save(order);

        // 📱 Send notification to OWNER
        _notifyOwnerOrderCreated(saved);

        // 🧹 Clear cart
        if (saved.getUser() != null && saved.getUser().getUserId() != null) {
            int userId = saved.getUser().getUserId();
            int companyId = saved.getCompany().getCompanyId();

            try {
                cartService.deleteByUserUserIdAndCompanyCompanyId(userId, companyId);
                log.info("✅ Cart cleared for user: {}, company: {}", userId, companyId);
            } catch (Exception e) {
                log.error("⚠️ Failed to clear cart for user ID: {}", userId, e);
            }
        }

        return mapToDTO(saved);
    }

    @Override
    public Optional<OrderDTO> updateOrder(Integer id, OrderDTO dto) {
        return orderRepository.findById(id)
                .map(order -> {
                    OrderStatus oldStatus = order.getOrderStatus();
                    OrderStatus newStatus = dto.getOrderStatus();

                    if (newStatus != null) {
                        order.setOrderStatus(newStatus);
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
                    if (dto.getDeliveryId() != null) {
                        userRepository.findById(dto.getDeliveryId()).ifPresent(order::setDelivery);
                    }

                    Order updated = orderRepository.save(order);

                    if (newStatus != null && !newStatus.equals(oldStatus)) {
                        _handleStatusChangeNotifications(updated, oldStatus, newStatus);
                    }

                    return mapToDTO(updated);
                });
    }

    private void _handleStatusChangeNotifications(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        try {
            log.info("📨 Status change: {} → {} for order {}", oldStatus, newStatus, order.getOrderId());

            switch (newStatus) {
                case IN_PREPARATION:
                    if (oldStatus == OrderStatus.PENDING) {
                        _notifyCustomerOrderAccepted(order);
                    }
                    break;

                case WAITING:
                    if (order.getDelivery() != null && order.getDelivery().getUserId() != null) {
                        _notifyDeliveryAssigned(order);
                    }
                    break;

                case PICKED_UP:
                    if (oldStatus == OrderStatus.WAITING) {
                        _notifyOwnerDeliveryPickedUp(order);
                    }
                    break;

                case REJECTED:
                    if (oldStatus == OrderStatus.WAITING) {
                        _notifyOwnerDeliveryRejected(order);
                    }
                    break;

                case DELIVERED:
                    if (oldStatus == OrderStatus.PICKED_UP) {
                        _notifyOwnerOrderDelivered(order);
                        _notifyCustomerOrderDelivered(order);
                    }
                    break;

                case CANCELED:
                    log.info("⚠️ Order {} cancelled", order.getOrderId());
                    break;

                default:
                    log.debug("No notification action for status: {}", newStatus);
            }
        } catch (Exception e) {
            log.error("❌ Error handling status change notifications: {}", e.getMessage(), e);
        }
    }

    /**
     * STEP 1: Customer creates order (PENDING) → Owner gets notified
     */
    private void _notifyOwnerOrderCreated(Order order) {
        try {
            if (order.getCompany() == null || order.getCompany().getCompanyId() == null) {
                return;
            }

            List<User> companyUsers = _getCompanyUsers(order.getCompany().getCompanyId());
            String orderAmount = String.format("%.2f", order.getTotalAmount());
            String companyName = order.getCompany().getCompanyName();

            for (User owner : companyUsers) {
                if (owner.getUserId() != null) {
                    Long ownerId = owner.getUserId().longValue();
                    Map<String, String> ownerData = new HashMap<>();
                    ownerData.put("route", "/orders");
                    ownerData.put("orderId", order.getOrderId().toString());
                    ownerData.put("type", "ORDER_CREATED");
                    ownerData.put("status", "PENDING");
                    ownerData.put("customerName", order.getCustomerName());
                    ownerData.put("totalAmount", orderAmount);
                    ownerData.put("companyName", companyName);

                    String title = "🆕 Nouvelle Commande";
                    String body = String.format(
                            "Commande #%d de %s\n💰 Montant: %s %s\n📍 Adresse: %s\n📞 Tél: %s",
                            order.getOrderId(),
                            order.getCustomerName(),
                            orderAmount,
                            CURRENCY,
                            order.getCustomerAddress(),
                            order.getCustomerPhone()
                    );

                    notificationService.sendNotificationToUser(ownerId, title, body, ownerData);
                    log.info("✅ Order creation notification sent to owner: {}", owner.getUserId());
                }
            }
        } catch (Exception e) {
            log.error("❌ Error notifying owner: {}", e.getMessage());
        }
    }

    /**
     * STEP 2: Owner accepts order (IN_PREPARATION) → Customer gets notified
     */
    private void _notifyCustomerOrderAccepted(Order order) {
        try {
            if (order.getUser() == null || order.getUser().getUserId() == null) {
                return;
            }

            Long customerId = order.getUser().getUserId().longValue();
            String companyName = order.getCompany() != null ? order.getCompany().getCompanyName() : "Jibli";
            String estimatedTime = "30-45 minutes";

            Map<String, String> customerData = new HashMap<>();
            customerData.put("route", "/orders");
            customerData.put("orderId", order.getOrderId().toString());
            customerData.put("type", "ORDER_ACCEPTED");
            customerData.put("status", "IN_PREPARATION");
            customerData.put("companyName", companyName);

            String title = "✅ Commande Acceptée";
            String body = String.format(
                    "Votre commande #%d est en préparation\n🏪 Magasin: %s\n⏱️ Temps estimé: %s\n💰 Montant: %.2f %s",
                    order.getOrderId(),
                    companyName,
                    estimatedTime,
                    order.getTotalAmount(),
                    CURRENCY
            );

            notificationService.sendNotificationToUser(customerId, title, body, customerData);
            log.info("✅ Order accepted notification sent to customer: {}", order.getUser().getUserId());
        } catch (Exception e) {
            log.error("❌ Error notifying customer (order accepted): {}", e.getMessage());
        }
    }

    /**
     * STEP 3: Owner assigns to delivery (WAITING) → Delivery gets notified
     */
    private void _notifyDeliveryAssigned(Order order) {
        try {
            if (order.getDelivery() == null || order.getDelivery().getUserId() == null) {
                return;
            }

            Long deliveryId = order.getDelivery().getUserId().longValue();
            String orderAmount = String.format("%.2f", order.getTotalAmount());

            Map<String, String> deliveryData = new HashMap<>();
            deliveryData.put("route", "/deliveries");
            deliveryData.put("orderId", order.getOrderId().toString());
            deliveryData.put("type", "DELIVERY_ASSIGNED");
            deliveryData.put("status", "WAITING");
            deliveryData.put("customerName", order.getCustomerName());
            deliveryData.put("customerAddress", order.getCustomerAddress());
            deliveryData.put("customerPhone", order.getCustomerPhone());

            String title = "🚚 Nouvelle Livraison Assignée";
            String body = String.format(
                    "Commande #%d à livrer\n👤 Client: %s\n📍 Adresse: %s\n📞 Tél: %s\n💰 Montant: %s %s",
                    order.getOrderId(),
                    order.getCustomerName(),
                    order.getCustomerAddress(),
                    order.getCustomerPhone(),
                    orderAmount,
                    CURRENCY
            );

            notificationService.sendNotificationToUser(deliveryId, title, body, deliveryData);
            log.info("✅ Delivery assignment notification sent to delivery: {}", order.getDelivery().getUserId());
        } catch (Exception e) {
            log.error("❌ Error notifying delivery: {}", e.getMessage());
        }
    }

    /**
     * STEP 4: Delivery picks up order (PICKED_UP) → Owner gets notified
     */
    private void _notifyOwnerDeliveryPickedUp(Order order) {
        try {
            if (order.getCompany() == null || order.getCompany().getCompanyId() == null) {
                return;
            }

            List<User> companyUsers = _getCompanyUsers(order.getCompany().getCompanyId());
            String deliveryName = order.getDelivery() != null ? order.getDelivery().getFullName() : "Livreur";
            String customerName = order.getCustomerName();

            for (User owner : companyUsers) {
                if (owner.getUserId() != null) {
                    Long ownerId = owner.getUserId().longValue();
                    Map<String, String> ownerData = new HashMap<>();
                    ownerData.put("route", "/orders");
                    ownerData.put("orderId", order.getOrderId().toString());
                    ownerData.put("type", "DELIVERY_PICKED_UP");
                    ownerData.put("status", "PICKED_UP");
                    ownerData.put("deliveryName", deliveryName);

                    String title = "📍 Colis Récupéré & En Route";
                    String body = String.format(
                            "Commande #%d en cours de livraison\n🚗 Livreur: %s\n👤 Destination: %s\n⏱️ En route maintenant",
                            order.getOrderId(),
                            deliveryName,
                            customerName
                    );

                    notificationService.sendNotificationToUser(ownerId, title, body, ownerData);
                    log.info("✅ Delivery picked up notification sent to owner: {}", owner.getUserId());
                }
            }
        } catch (Exception e) {
            log.error("❌ Error notifying owner (picked up): {}", e.getMessage());
        }
    }

    /**
     * STEP 4b: Delivery rejects order (REJECTED) → Owner gets notified
     */
    private void _notifyOwnerDeliveryRejected(Order order) {
        try {
            if (order.getCompany() == null || order.getCompany().getCompanyId() == null) {
                return;
            }

            List<User> companyUsers = _getCompanyUsers(order.getCompany().getCompanyId());
            String deliveryName = order.getDelivery() != null ? order.getDelivery().getFullName() : "Livreur";

            for (User owner : companyUsers) {
                if (owner.getUserId() != null) {
                    Long ownerId = owner.getUserId().longValue();
                    Map<String, String> ownerData = new HashMap<>();
                    ownerData.put("route", "/orders");
                    ownerData.put("orderId", order.getOrderId().toString());
                    ownerData.put("type", "DELIVERY_REJECTED");
                    ownerData.put("status", "REJECTED");
                    ownerData.put("deliveryName", deliveryName);

                    String title = "❌ Livraison Rejetée";
                    String body = String.format(
                            "Commande #%d a été rejetée\n🚗 Livreur: %s\n⚠️ Action requise: Réassigner à un autre livreur",
                            order.getOrderId(),
                            deliveryName
                    );

                    notificationService.sendNotificationToUser(ownerId, title, body, ownerData);
                    log.info("✅ Delivery rejected notification sent to owner: {}", owner.getUserId());
                }
            }
        } catch (Exception e) {
            log.error("❌ Error notifying owner (rejected): {}", e.getMessage());
        }
    }

    /**
     * STEP 6a: Delivery completes order (DELIVERED) → Owner gets notified
     */
    private void _notifyOwnerOrderDelivered(Order order) {
        try {
            if (order.getCompany() == null || order.getCompany().getCompanyId() == null) {
                return;
            }

            List<User> companyUsers = _getCompanyUsers(order.getCompany().getCompanyId());
            String orderAmount = String.format("%.2f", order.getTotalAmount());

            for (User owner : companyUsers) {
                if (owner.getUserId() != null) {
                    Long ownerId = owner.getUserId().longValue();
                    Map<String, String> ownerData = new HashMap<>();
                    ownerData.put("route", "/orders");
                    ownerData.put("orderId", order.getOrderId().toString());
                    ownerData.put("type", "ORDER_DELIVERED");
                    ownerData.put("status", "DELIVERED");
                    ownerData.put("totalAmount", orderAmount);

                    String title = "✨ Commande Livrée avec Succès";
                    String body = String.format(
                            "Commande #%d livrée avec succès\n👤 Client: %s\n💰 Montant reçu: %s %s\n🎉 Transaction complétée",
                            order.getOrderId(),
                            order.getCustomerName(),
                            orderAmount,
                            CURRENCY
                    );

                    notificationService.sendNotificationToUser(ownerId, title, body, ownerData);
                    log.info("✅ Order delivered notification sent to owner: {}", owner.getUserId());
                }
            }
        } catch (Exception e) {
            log.error("❌ Error notifying owner (delivered): {}", e.getMessage());
        }
    }

    /**
     * STEP 6b: Delivery completes order (DELIVERED) → Customer gets notified
     */
    private void _notifyCustomerOrderDelivered(Order order) {
        try {
            if (order.getUser() == null || order.getUser().getUserId() == null) {
                return;
            }

            Long customerId = order.getUser().getUserId().longValue();
            String orderAmount = String.format("%.2f", order.getTotalAmount());

            Map<String, String> customerData = new HashMap<>();
            customerData.put("route", "/orders");
            customerData.put("orderId", order.getOrderId().toString());
            customerData.put("type", "ORDER_DELIVERED");
            customerData.put("status", "DELIVERED");
            customerData.put("totalAmount", orderAmount);

            String title = "✨ Commande Reçue!";
            String body = String.format(
                    "Votre commande #%d a été livrée avec succès!\n💰 Montant payé: %s %s\n⭐ N'oubliez pas de noter le vendeur\n🙏 Merci de votre confiance",
                    order.getOrderId(),
                    orderAmount,
                    CURRENCY
            );

            notificationService.sendNotificationToUser(customerId, title, body, customerData);
            log.info("✅ Order delivered notification sent to customer: {}", order.getUser().getUserId());
        } catch (Exception e) {
            log.error("❌ Error notifying customer (delivered): {}", e.getMessage());
        }
    }

    private List<User> _getCompanyUsers(Integer companyId) {
        try {
            if (userCompanyRepository != null) {
                return userCompanyRepository.findUsersByCompanyId(companyId);
            }
            return orderRepository.findUserByCompanyCompanyId(companyId);
        } catch (Exception e) {
            log.warn("⚠️ Failed to fetch company users: {}", e.getMessage());
            return List.of();
        }
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

    @Override
    public List<OrderDTO> findOrdersByCompanyCompanyId(Integer companyId) {
        return orderRepository.findOrdersByCompanyCompanyId(companyId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private OrderDTO mapToDTO(Order order) {
        if (order == null) return null;

        List<Integer> orderItemIds = order.getOrderItems() != null
                ? order.getOrderItems().stream()
                .map(OrderItem::getOrderItemId)
                .collect(Collectors.toList())
                : List.of();

        CompanyDTO companyDTO = order.getCompany() != null
                ? new CompanyDTO(
                order.getCompany().getCompanyId(),
                order.getCompany().getCompanyName(),
                order.getCompany().getCompanyDescription(),
                order.getCompany().getCompanySector(),
                order.getCompany().getCompanyAddress(),
                order.getCompany().getCompanyPhone(),
                order.getCompany().getTimeOpen(),
                order.getCompany().getTimeClose(),
                order.getCompany().getAverageRating()
        )
                : null;

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
                order.getDeliveryFee(),
                order.getOrderStatus(),
                order.getOrderDate(),
                order.getShippedDate(),
                order.getCreatedAt(),
                order.getLastUpdated(),
                orderItemIds,
                order.getDelivery() != null ? order.getDelivery().getUserId() : null,
                order.getAssignedBy() != null ? order.getAssignedBy().getUserId() : null,
                order.getPickedUpDate(),
                order.getInPreparationDate(),
                order.getAcceptedDate(),
                order.getWaitingDate(),
                order.getCanceledDate(),
                order.getDeliveredDate(),
                companyDTO
        );
    }

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
        order.setDeliveryFee(dto.getDeliveryFee());
        order.setOrderStatus(dto.getOrderStatus());
        order.setOrderDate(dto.getOrderDate());
        order.setShippedDate(dto.getShippedDate());
        order.setCreatedAt(dto.getCreatedAt());
        order.setLastUpdated(dto.getLastUpdated());
        order.setPickedUpDate(dto.getPickedUpDate());
        order.setInPreparationDate(dto.getInPreparationDate());
        order.setAcceptedDate(dto.getAcceptedDate());
        order.setWaitingDate(dto.getWaitingDate());
        order.setCanceledDate(dto.getCanceledDate());
        order.setDeliveredDate(dto.getDeliveredDate());

        if (dto.getUserId() != null) {
            userRepository.findById(dto.getUserId()).ifPresent(order::setUser);
        }

        if (dto.getCompanyId() != null) {
            Optional<Company> companyOpt = companyRepository.findById(dto.getCompanyId());
            companyOpt.ifPresent(order::setCompany);
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