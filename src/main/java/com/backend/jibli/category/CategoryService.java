package com.backend.jibli.category;

import com.backend.jibli.attachment.Attachment;
import com.backend.jibli.attachment.AttachmentDTO;
import com.backend.jibli.product.Product;
import com.backend.jibli.attachment.IAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService implements ICategoryService {

    private final ICategoryRepository categoryRepository;
    private final IAttachmentService attachmentService;

    @Autowired
    public CategoryService(ICategoryRepository categoryRepository, IAttachmentService attachmentService) {
        this.categoryRepository = categoryRepository;
        this.attachmentService = attachmentService;
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CategoryDTO> getCategoryById(Integer id) {
        return categoryRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }
        if (dto.getDescription() != null && dto.getDescription().length() > 500) {
            throw new IllegalArgumentException("Description cannot exceed 500 characters");
        }
        Category category = mapToEntity(dto);
        Category saved = categoryRepository.save(category);
        return mapToDTO(saved);
    }

    @Override
    public Optional<CategoryDTO> updateCategory(Integer id, CategoryDTO dto) {
        if (dto.getName() != null && dto.getName().isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        if (dto.getDescription() != null && dto.getDescription().length() > 500) {
            throw new IllegalArgumentException("Description cannot exceed 500 characters");
        }
        return categoryRepository.findById(id)
                .map(category -> {
                    if (dto.getName() != null) category.setName(dto.getName());
                    if (dto.getDescription() != null) category.setDescription(dto.getDescription());
                    if (dto.getIconId() != null) category.setIconId(dto.getIconId());
                    Category updated = categoryRepository.save(category);
                    return mapToDTO(updated);
                });
    }

    @Override
    public boolean deleteCategory(Integer id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public AttachmentDTO addAttachment(Integer categoryId, MultipartFile file) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("Category not found");
        }
        return attachmentService.createAttachment(file, "CATEGORY", categoryId);
    }

    @Override
    public List<AttachmentDTO> getCategoryAttachments(Integer categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("Category not found");
        }
        return attachmentService.getAttachmentsByEntity("CATEGORY", categoryId);
    }

    @Override
    public List<CategoryDTO> findCategoriesByUserUserId(Integer userId) {
        return categoryRepository.findCategoriesByUserUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private CategoryDTO mapToDTO(Category category) {
        if (category == null) return null;

        List<Integer> attachmentIds = category.getAttachments() != null
                ? category.getAttachments().stream()
                .map(Attachment::getAttachmentId)
                .collect(Collectors.toList())
                : List.of();

        // Get product IDs safely
        List<Integer> productIds = List.of();
        try {
            if (category.getProducts() != null) {
                productIds = category.getProducts().stream()
                        .map(Product::getProductId)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            productIds = List.of();
        }

        // Get userId from category
        Integer userId = category.getUser() != null ? category.getUser().getUserId() : null;

        // Get companyId from category
        Integer companyId = category.getCompany() != null ? category.getCompany().getCompanyId() : null;

        return new CategoryDTO(
                category.getCategoryId(),
                category.getName(),
                category.getDescription(),
                category.getIconId(),
                category.getCreatedAt(),
                category.getLastUpdated(),
                attachmentIds,
                productIds,
                userId,
                companyId
        );
    }

    private Category mapToEntity(CategoryDTO dto) {
        if (dto == null) return null;

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setIconId(dto.getIconId());
        return category;
    }
}