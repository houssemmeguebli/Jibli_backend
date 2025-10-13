package com.backend.jibli.product;

import com.backend.jibli.attachment.AttachmentDTO;
import com.backend.jibli.order.OrderItemDTO;
import com.backend.jibli.review.ReviewDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface IProductService {
    List<ProductDTO> getAllProducts();
    Optional<ProductDTO> getProductById(Integer id);
    ProductDTO createProduct(ProductDTO dto);
    Optional<ProductDTO> updateProduct(Integer id, ProductDTO dto);
    boolean deleteProduct(Integer id);
    AttachmentDTO addAttachment(Integer productId, MultipartFile file);
    List<AttachmentDTO> getProductAttachments(Integer productId);
    List<ReviewDTO> getProductReviews(Integer productId);
    List<OrderItemDTO> getProductOrderItems(Integer productId);


}