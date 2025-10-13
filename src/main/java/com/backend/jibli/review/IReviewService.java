package com.backend.jibli.review;

import java.util.List;
import java.util.Optional;

public interface IReviewService {
    List<ReviewDTO> getAllReviews();
    Optional<ReviewDTO> getReviewById(Integer id);
    ReviewDTO createReview(ReviewDTO dto);
    Optional<ReviewDTO> updateReview(Integer id, ReviewDTO dto);
    boolean deleteReview(Integer id);
    List<ReviewDTO> getReviewsByProduct(Integer productId);
}