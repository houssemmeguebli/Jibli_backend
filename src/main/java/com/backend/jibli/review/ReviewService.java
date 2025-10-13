package com.backend.jibli.review;

import com.backend.jibli.product.IProductRepository;
import com.backend.jibli.product.Product;
import com.backend.jibli.user.IUserRepository;
import com.backend.jibli.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService implements IReviewService {

    private final IReviewRepository reviewRepository;
    private final IUserRepository userRepository;
    private final IProductRepository productRepository;

    @Autowired
    public ReviewService(IReviewRepository reviewRepository, IUserRepository userRepository, IProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ReviewDTO> getReviewById(Integer id) {
        return reviewRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public ReviewDTO createReview(ReviewDTO dto) {
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (dto.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (!userRepository.existsById(dto.getUserId())) {
            throw new IllegalArgumentException("User not found");
        }
        if (!productRepository.existsById(dto.getProductId())) {
            throw new IllegalArgumentException("Product not found");
        }
        if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (dto.getComment() == null || dto.getComment().isBlank()) {
            throw new IllegalArgumentException("Comment is required");
        }
        if (dto.getComment().length() > 1000) {
            throw new IllegalArgumentException("Comment cannot exceed 1000 characters");
        }
        if (reviewRepository.existsByUserUserIdAndProductProductId(dto.getUserId(), dto.getProductId())) {
            throw new IllegalArgumentException("User has already reviewed this product");
        }
        Review review = mapToEntity(dto);
        Review saved = reviewRepository.save(review);
        return mapToDTO(saved);
    }

    @Override
    public Optional<ReviewDTO> updateReview(Integer id, ReviewDTO dto) {
        if (dto.getUserId() != null && !userRepository.existsById(dto.getUserId())) {
            throw new IllegalArgumentException("User not found");
        }
        if (dto.getProductId() != null && !productRepository.existsById(dto.getProductId())) {
            throw new IllegalArgumentException("Product not found");
        }
        if (dto.getRating() != null && (dto.getRating() < 1 || dto.getRating() > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (dto.getComment() != null && dto.getComment().isBlank()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }
        if (dto.getComment() != null && dto.getComment().length() > 1000) {
            throw new IllegalArgumentException("Comment cannot exceed 1000 characters");
        }
        return reviewRepository.findById(id)
                .map(review -> {
                    if (dto.getUserId() != null) {
                        User user = new User();
                        user.setUserId(dto.getUserId());
                        review.setUser(user);
                    }
                    if (dto.getProductId() != null) {
                        Product product = new Product();
                        product.setProductId(dto.getProductId());
                        review.setProduct(product);
                    }
                    if (dto.getRating() != null) review.setRating(dto.getRating());
                    if (dto.getComment() != null) review.setComment(dto.getComment());
                    Review updated = reviewRepository.save(review);
                    return mapToDTO(updated);
                });
    }

    @Override
    public boolean deleteReview(Integer id) {
        if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<ReviewDTO> getReviewsByProduct(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Product not found");
        }
        return reviewRepository.findByProductProductId(productId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ReviewDTO mapToDTO(Review review) {
        return new ReviewDTO(
                review.getReviewId(),
                review.getUser() != null ? review.getUser().getUserId() : null,
                review.getProduct() != null ? review.getProduct().getProductId() : null,
                review.getCompany()!= null ? review.getCompany().getCompanyId() : null,
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getLastUpdated()

        );
    }

    private Review mapToEntity(ReviewDTO dto) {
        Review review = new Review();
        if (dto.getUserId() != null) {
            User user = new User();
            user.setUserId(dto.getUserId());
            review.setUser(user);
        }
        if (dto.getProductId() != null) {
            Product product = new Product();
            product.setProductId(dto.getProductId());
            review.setProduct(product);
        }
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        return review;
    }
}