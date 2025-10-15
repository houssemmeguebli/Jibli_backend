package com.backend.jibli.product;

import com.backend.jibli.attachment.AttachmentDTO;
import com.backend.jibli.order.OrderItemDTO;
import com.backend.jibli.review.ReviewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final IProductService productService;

    @Autowired
    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Integer id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProductDTO>>  findByUserUserId(@PathVariable Integer userId) {
        List<ProductDTO> products =  productService.findByUserUserId(userId);
        return ResponseEntity.ok(products);
    }


    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO dto) {
        try {
            ProductDTO created = productService.createProduct(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Integer id, @RequestBody ProductDTO dto) {
        try {
            return productService.updateProduct(id, dto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/attachments")
    public ResponseEntity<AttachmentDTO> addAttachment(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        try {
            AttachmentDTO attachment = productService.addAttachment(id, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(attachment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}/attachments")
    public ResponseEntity<List<AttachmentDTO>> getProductAttachments(@PathVariable Integer id) {
        try {
            List<AttachmentDTO> attachments = productService.getProductAttachments(id);
            return ResponseEntity.ok(attachments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewDTO>> getProductReviews(@PathVariable Integer id) {
        try {
            List<ReviewDTO> reviews = productService.getProductReviews(id);
            return ResponseEntity.ok(reviews);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}/order-items")
    public ResponseEntity<List<OrderItemDTO>> getProductOrderItems(@PathVariable Integer id) {
        try {
            List<OrderItemDTO> orderItems = productService.getProductOrderItems(id);
            return ResponseEntity.ok(orderItems);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}