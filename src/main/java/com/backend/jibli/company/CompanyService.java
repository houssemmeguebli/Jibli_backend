package com.backend.jibli.company;

import com.backend.jibli.attachment.Attachment;
import com.backend.jibli.category.Category;
import com.backend.jibli.category.CategoryDTO;
import com.backend.jibli.product.Product;
import com.backend.jibli.product.ProductDTO;
import com.backend.jibli.review.Review;
import com.backend.jibli.review.ReviewDTO;
import com.backend.jibli.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompanyService implements ICompanyService {

    private final ICompanyRepository companyRepository;

    @Autowired
    public CompanyService(ICompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDTO> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanyDTO> getCompanyById(Integer id) {
        return companyRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public CompanyDTO createCompany(CompanyDTO dto) {
        if (dto.getCompanyName() == null || dto.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("Company name is required");
        }
        if (dto.getCompanySector() == null || dto.getCompanySector().isBlank()) {
            throw new IllegalArgumentException("Company sector is required");
        }
        if (dto.getCompanyDescription() != null && dto.getCompanyDescription().length() > 1000) {
            throw new IllegalArgumentException("Description cannot exceed 1000 characters");
        }
        if (dto.getCompanyPhone() == null || dto.getCompanyPhone().isBlank()) {
            throw new IllegalArgumentException("Company phone is required");
        }
        if (dto.getCompanyEmail() == null || dto.getCompanyEmail().isBlank()) {
            throw new IllegalArgumentException("Company email is required");
        }
        if (dto.getTimeOpen() == null ) {
            throw new IllegalArgumentException("Opening time is required");
        }
        if (dto.getTimeClose() == null ) {
            throw new IllegalArgumentException("Closing time is required");
        }

        Company company = mapToEntity(dto);
        Company saved = companyRepository.save(company);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public Optional<CompanyDTO> updateCompany(Integer id, CompanyDTO dto) {
        if (dto.getCompanyName() != null && dto.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("Company name cannot be empty");
        }
        if (dto.getCompanySector() != null && dto.getCompanySector().isBlank()) {
            throw new IllegalArgumentException("Company sector cannot be empty");
        }
        if (dto.getCompanyDescription() != null && dto.getCompanyDescription().length() > 1000) {
            throw new IllegalArgumentException("Description cannot exceed 1000 characters");
        }

        return companyRepository.findById(id)
                .map(company -> {
                    if (dto.getCompanyName() != null) company.setCompanyName(dto.getCompanyName());
                    if (dto.getCompanyDescription() != null) company.setCompanyDescription(dto.getCompanyDescription());
                    if (dto.getCompanySector() != null) company.setCompanySector(dto.getCompanySector());
                    if (dto.getCompanyAddress() != null) company.setCompanyAddress(dto.getCompanyAddress());
                    if (dto.getCompanyPhone() != null) company.setCompanyPhone(dto.getCompanyPhone());
                    if (dto.getCompanyEmail() != null) company.setCompanyEmail(dto.getCompanyEmail());
                    if (dto.getTimeOpen() != null) company.setTimeOpen(dto.getTimeOpen());
                    if (dto.getTimeClose() != null) company.setTimeClose(dto.getTimeClose());
                    if(dto.getCompanyStatus() != null) company.setCompanyStatus(dto.getCompanyStatus());


                    Company updated = companyRepository.save(company);
                    return mapToDTO(updated);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDTO> findByUserUserId(Integer userId) {
        return companyRepository.findByUserUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteCompany(Integer id) {
        if (companyRepository.existsById(id)) {
            companyRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDTO> findByUserUserIdWithProducts(Integer userId) {
        return companyRepository.findByUserUserId(userId).stream()
                .map(this::mapToDTOWithProducts)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDTO findByCompanyIdWithProducts(Integer companyId) {
        Company company = companyRepository.findByCompanyIdWithProducts(companyId);

        if (company == null) {
            throw new RuntimeException("Company not found with id: " + companyId);
        }

        return mapToDTOWithProducts(company);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDTO findByCompanyIdWithReviews(Integer companyId) {
        Company company = companyRepository.findByCompanyIdWithReviews(companyId);

        if (company == null) {
            throw new RuntimeException("Company not found with id: " + companyId);
        }

        return mapToDTOWithReviews(company);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDTO findByCompanyIdWithCategories(Integer companyId) {
        Company company = companyRepository.findByCompanyIdWithCategories(companyId);

        if (company == null) {
            throw new RuntimeException("Company not found with id: " + companyId);
        }

        return mapToDTOWithCategories(company);
    }

    @Override
    public List<CompanyDTO> findAllActiveCompanies() {
        return companyRepository.findAllActiveCompanies().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

    }

    private CompanyDTO mapToDTO(Company company) {
        if (company == null) return null;

        // Calculate average rating
        Double averageRating = company.getReviews() != null && !company.getReviews().isEmpty()
                ? company.getReviews().stream()
                .filter(r -> r.getRating() != null && r.getRating() > 0)
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0)
                : 0.0;

        CompanyDTO dto = new CompanyDTO();
        dto.setCompanyId(company.getCompanyId());
        dto.setCompanyName(company.getCompanyName());
        dto.setCompanyDescription(company.getCompanyDescription());
        dto.setCompanySector(company.getCompanySector());
        dto.setCompanyAddress(company.getCompanyAddress());
        dto.setCompanyPhone(company.getCompanyPhone());
        dto.setCompanyEmail(company.getCompanyEmail());
        dto.setCreatedAt(company.getCreatedAt());
        dto.setLastUpdated(company.getLastUpdated());
        dto.setCompanyStatus(company.getCompanyStatus());
        dto.setAverageRating(averageRating);
        dto.setDeliveryFee(company.getDeliveryFee());
        dto.setTimeOpen(company.getTimeOpen());
        dto.setTimeClose(company.getTimeClose());
        // Safe null check for user
        if (company.getUser() != null) {
            dto.setUserId(company.getUser().getUserId());
        } else {
            dto.setUserId(null);
        }
        return dto;
    }

    private CompanyDTO mapToDTOWithProducts(Company company) {
        if (company == null) return null;

        // Calculate average rating
        Double averageRating = company.getReviews() != null && !company.getReviews().isEmpty()
                ? company.getReviews().stream()
                .filter(r -> r.getRating() != null && r.getRating() > 0)
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0)
                : 0.0;

        // Convert products to DTOs
        List<ProductDTO> productDTOs = company.getProducts() != null
                ? company.getProducts().stream()
                .map(this::mapProductToDTO)
                .collect(Collectors.toList())
                : Collections.emptyList();

        CompanyDTO dto = new CompanyDTO();
        dto.setCompanyId(company.getCompanyId());
        dto.setCompanyName(company.getCompanyName());
        dto.setCompanyDescription(company.getCompanyDescription());
        dto.setCompanySector(company.getCompanySector());
        dto.setCompanyAddress(company.getCompanyAddress());
        dto.setCompanyPhone(company.getCompanyPhone());
        dto.setCompanyEmail(company.getCompanyEmail());
        dto.setCreatedAt(company.getCreatedAt());
        dto.setLastUpdated(company.getLastUpdated());
        dto.setCompanyStatus(company.getCompanyStatus());
        dto.setAverageRating(averageRating);
        dto.setDeliveryFee(company.getDeliveryFee());
        dto.setProducts(productDTOs);
        dto.setTimeOpen(company.getTimeOpen());
        dto.setTimeClose(company.getTimeClose());
        dto.setUserId(company.getUser().getUserId());

        return dto;
    }

    private CompanyDTO mapToDTOWithReviews(Company company) {
        if (company == null) return null;

        // Calculate average rating
        Double averageRating = company.getReviews() != null && !company.getReviews().isEmpty()
                ? company.getReviews().stream()
                .filter(r -> r.getRating() != null && r.getRating() > 0)
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0)
                : 0.0;

        // Convert reviews to DTOs
        List<ReviewDTO> reviewDTOs = company.getReviews() != null
                ? company.getReviews().stream()
                .map(this::mapReviewToDTO)
                .collect(Collectors.toList())
                : Collections.emptyList();

        CompanyDTO dto = new CompanyDTO();
        dto.setCompanyId(company.getCompanyId());
        dto.setCompanyName(company.getCompanyName());
        dto.setCompanyDescription(company.getCompanyDescription());
        dto.setCompanySector(company.getCompanySector());
        dto.setCompanyAddress(company.getCompanyAddress());
        dto.setCompanyPhone(company.getCompanyPhone());
        dto.setCompanyEmail(company.getCompanyEmail());
        dto.setCreatedAt(company.getCreatedAt());
        dto.setLastUpdated(company.getLastUpdated());
        dto.setCompanyStatus(company.getCompanyStatus());
        dto.setAverageRating(averageRating);
        dto.setDeliveryFee(company.getDeliveryFee());
        dto.setReviews(reviewDTOs);
        dto.setTimeOpen(company.getTimeOpen());
        dto.setTimeClose(company.getTimeClose());
        dto.setUserId(company.getUser().getUserId());

        return dto;
    }

    private CompanyDTO mapToDTOWithCategories(Company company) {
        if (company == null) return null;

        // Calculate average rating
        Double averageRating = company.getReviews() != null && !company.getReviews().isEmpty()
                ? company.getReviews().stream()
                .filter(r -> r.getRating() != null && r.getRating() > 0)
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0)
                : 0.0;

        // Convert categories to DTOs
        List<CategoryDTO> categoryDTOs = company.getCategories() != null
                ? company.getCategories().stream()
                .map(this::mapCategoryToDTO)
                .collect(Collectors.toList())
                : Collections.emptyList();

        CompanyDTO dto = new CompanyDTO();
        dto.setCompanyId(company.getCompanyId());
        dto.setCompanyName(company.getCompanyName());
        dto.setCompanyDescription(company.getCompanyDescription());
        dto.setCompanySector(company.getCompanySector());
        dto.setCompanyAddress(company.getCompanyAddress());
        dto.setCompanyPhone(company.getCompanyPhone());
        dto.setCompanyEmail(company.getCompanyEmail());
        dto.setCreatedAt(company.getCreatedAt());
        dto.setLastUpdated(company.getLastUpdated());
        dto.setCompanyStatus(company.getCompanyStatus());
        dto.setAverageRating(averageRating);
        dto.setDeliveryFee(company.getDeliveryFee());
        dto.setCategories(categoryDTOs);
        dto.setTimeOpen(company.getTimeOpen());
        dto.setTimeClose(company.getTimeClose());
        dto.setUserId(company.getUser().getUserId());


        return dto;
    }

    private ProductDTO mapProductToDTO(Product product) {
        if (product == null) return null;

        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setProductDescription(product.getProductDescription());
        dto.setProductPrice(product.getProductPrice());
        dto.setProductFinalePrice(product.getProductFinalePrice());
        dto.setDiscountPercentage(product.getDiscountPercentage());
        dto.setAvailable(product.isAvailable());

        return dto;
    }

    private ReviewDTO mapReviewToDTO(Review review) {
        if (review == null) return null;

        ReviewDTO dto = new ReviewDTO();
        dto.setReviewId(review.getReviewId());
        dto.setUserId(review.getUser() != null ? review.getUser().getUserId() : null);
        dto.setProductId(review.getProduct() != null ? review.getProduct().getProductId() : null);
        dto.setCompanyId(review.getCompany() != null ? review.getCompany().getCompanyId() : null);
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setLastUpdated(review.getLastUpdated());

        return dto;
    }
    private CategoryDTO mapCategoryToDTO(Category category) {
        if (category == null) return null;

        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());

        return dto;
    }

    private Company mapToEntity(CompanyDTO dto) {
        if (dto == null) return null;

        Company company = new Company();
        company.setCompanyName(dto.getCompanyName());
        company.setCompanyDescription(dto.getCompanyDescription());
        company.setCompanySector(dto.getCompanySector());
        company.setCompanyAddress(dto.getCompanyAddress());
        company.setCompanyPhone(dto.getCompanyPhone());
        company.setCompanyEmail(dto.getCompanyEmail());
        company.setCompanyStatus(dto.getCompanyStatus());
        company.setDeliveryFee(dto.getDeliveryFee());
        company.setTimeOpen(dto.getTimeOpen());
        company.setTimeClose(dto.getTimeClose());
        if (dto.getUserId() != null) {
            User user = new User();
            user.setUserId(dto.getUserId());
            company.setUser(user);
        }

        return company;
    }
}