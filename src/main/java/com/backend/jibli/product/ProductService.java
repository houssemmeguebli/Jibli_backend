package com.backend.jibli.product;


import com.backend.jibli.attachment.Attachment;
import com.backend.jibli.attachment.AttachmentDTO;
import com.backend.jibli.attachment.IAttachmentService;
import com.backend.jibli.category.Category;
import com.backend.jibli.order.IOrderItemService;
import com.backend.jibli.order.OrderItem;
import com.backend.jibli.order.OrderItemDTO;
import com.backend.jibli.review.IReviewService;
import com.backend.jibli.review.Review;
import com.backend.jibli.review.ReviewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService implements IProductService {

    private final IProductRepository productRepository;
    private final IAttachmentService attachmentService;
    private final IReviewService reviewService;
    private final IOrderItemService orderItemService;

    @Autowired
    public ProductService(IProductRepository productRepository, IAttachmentService attachmentService, IReviewService reviewService, IOrderItemService orderItemService) {
        this.productRepository = productRepository;
        this.attachmentService = attachmentService;
        this.reviewService = reviewService;
        this.orderItemService = orderItemService;
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ProductDTO> getProductById(Integer id) {
        return productRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public ProductDTO createProduct(ProductDTO dto) {
        if (dto.getProductName() == null || dto.getProductName().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (dto.getProductPrice() == null || dto.getProductPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("Category ID is required");
        }
        if (dto.getProductDescription() != null && dto.getProductDescription().length() > 1000) {
            throw new IllegalArgumentException("Description cannot exceed 1000 characters");
        }
        Product product = mapToEntity(dto);
        Product saved = productRepository.save(product);
        return mapToDTO(saved);
    }

    @Override
    public Optional<ProductDTO> updateProduct(Integer id, ProductDTO dto) {
        if (dto.getProductName() != null && dto.getProductName().isBlank()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (dto.getProductPrice() != null && dto.getProductPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (dto.getProductDescription() != null && dto.getProductDescription().length() > 1000) {
            throw new IllegalArgumentException("Description cannot exceed 1000 characters");
        }
        return productRepository.findById(id)
                .map(product -> {
                    if (dto.getProductName() != null) product.setProductName(dto.getProductName());
                    if (dto.getProductDescription() != null) product.setProductDescription(dto.getProductDescription());
                    if (dto.getProductPrice() != null) product.setProductPrice(dto.getProductPrice());
                    if (dto.getCategoryId() != null) {
                        Category category = new Category();
                        category.setCategoryId(dto.getCategoryId());
                        product.setCategory(category);
                    }
                    Product updated = productRepository.save(product);
                    return mapToDTO(updated);
                });
    }

    @Override
    public boolean deleteProduct(Integer id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public AttachmentDTO addAttachment(Integer productId, MultipartFile file) {
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Product not found");
        }
        return attachmentService.createAttachment(file, "PRODUCT", productId);
    }

    @Override
    public List<AttachmentDTO> getProductAttachments(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Product not found");
        }
        return attachmentService.getAttachmentsByEntity("PRODUCT", productId);
    }

    @Override
    public List<ReviewDTO> getProductReviews(Integer productId) {
        return reviewService.getReviewsByProduct(productId);
    }

    @Override
    public List<OrderItemDTO> getProductOrderItems(Integer productId) {
        return orderItemService.getOrderItemsByProduct(productId);
    }

    private ProductDTO mapToDTO(Product product) {
        List<Integer> attachmentIds = product.getAttachments() != null
                ? product.getAttachments().stream()
                .map(Attachment::getAttachmentId)
                .collect(Collectors.toList())
                : List.of();
        List<Integer> reviewIds = product.getReviews() != null
                ? product.getReviews().stream()
                .map(Review::getReviewId)
                .collect(Collectors.toList())
                : List.of();
        List<Integer> orderItemIds = product.getOrderItems() != null
                ? product.getOrderItems().stream()
                .map(OrderItem::getOrderItemId)
                .collect(Collectors.toList())
                : List.of();
        return new ProductDTO(
                product.getProductId(),
                product.getProductName(),
                product.getProductDescription(),
                product.getProductPrice(),
                product.isAvailable(),
                product.getDiscountPercentage(),
                product.getCategory() != null ? product.getCategory().getCategoryId() : null,
                product.getCreatedAt(),
                product.getLastUpdated(),
                attachmentIds,
                reviewIds,
                orderItemIds
        );
    }

    private Product mapToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setProductDescription(dto.getProductDescription());
        product.setProductPrice(dto.getProductPrice());
        if (dto.getCategoryId() != null) {
            Category category = new Category();
            category.setCategoryId(dto.getCategoryId());
            product.setCategory(category);
        }
        return product;
    }
}