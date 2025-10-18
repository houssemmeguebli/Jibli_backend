package com.backend.jibli.attachment;

import com.backend.jibli.category.ICategoryRepository;
import com.backend.jibli.product.IProductRepository;
import com.backend.jibli.user.IUserRepository;
import com.backend.jibli.company.ICompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttachmentService implements IAttachmentService {

    private static final List<String> ALLOWED_ENTITY_TYPES = Arrays.asList(
            "CATEGORY", "PRODUCT", "USER", "COMPANY"
    );
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final IAttachmentRepository attachmentRepository;
    private final ICategoryRepository categoryRepository;
    private final IProductRepository productRepository;
    private final IUserRepository userRepository;
    private final ICompanyRepository companyRepository;

    @Autowired
    public AttachmentService(IAttachmentRepository attachmentRepository,
                             ICategoryRepository categoryRepository,
                             IProductRepository productRepository,
                             IUserRepository userRepository,
                             ICompanyRepository companyRepository) {
        this.attachmentRepository = attachmentRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public List<AttachmentDTO> getAllAttachments() {
        return attachmentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AttachmentDTO> getAttachmentById(Integer id) {
        return attachmentRepository.findById(id)
                .map(this::mapToDTO);
    }

    @Override
    public AttachmentDTO createAttachment(MultipartFile file, String entityType, Integer entityId) {
        // Validate inputs
        validateFile(file);
        validateEntity(entityType, entityId);

        try {
            Attachment attachment = new Attachment();
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileType(file.getContentType());
            attachment.setData(file.getBytes());

            // Set generic fields
            String normalizedType = entityType.toUpperCase();
            attachment.setEntityType(normalizedType);
            attachment.setEntityId(entityId);

            Attachment saved = attachmentRepository.save(attachment);
            return mapToDTO(saved);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file data: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store attachment: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<AttachmentDTO> updateAttachment(Integer id, MultipartFile file,
                                                    String entityType, Integer entityId) {
        return attachmentRepository.findById(id)
                .map(attachment -> {
                    try {
                        // Update file if provided
                        if (file != null && !file.isEmpty()) {
                            validateFile(file);
                            attachment.setFileName(file.getOriginalFilename());
                            attachment.setFileType(file.getContentType());
                            attachment.setData(file.getBytes());
                        }

                        // Update entity reference if provided
                        if (entityType != null && !entityType.trim().isEmpty()) {
                            Integer targetEntityId = entityId != null ? entityId : attachment.getEntityId();
                            validateEntity(entityType, targetEntityId);

                            String normalizedType = entityType.toUpperCase();
                            attachment.setEntityType(normalizedType);
                            attachment.setEntityId(targetEntityId);
                        }

                        Attachment updated = attachmentRepository.save(attachment);
                        return mapToDTO(updated);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to read file data: " + e.getMessage(), e);
                    }
                });
    }

    @Override
    public boolean deleteAttachment(Integer id) {
        if (attachmentRepository.existsById(id)) {
            attachmentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<AttachmentDTO> getAttachmentsByEntity(String entityType, Integer entityId) {
        validateEntityType(entityType);
        if (entityId == null) {
            throw new IllegalArgumentException("Entity ID is required");
        }

        return attachmentRepository.findByEntityTypeAndEntityId(entityType.toUpperCase(), entityId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required and cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("File must have a valid filename");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_FILE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid file type: " + contentType + ". Allowed types: " +
                            String.join(", ", ALLOWED_FILE_TYPES)
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size (" + file.getSize() + " bytes) exceeds maximum allowed size of 10MB"
            );
        }

        if (file.getSize() == 0) {
            throw new IllegalArgumentException("File is empty (0 bytes)");
        }
    }

    private void validateEntity(String entityType, Integer entityId) {
        validateEntityType(entityType);

        if (entityId == null) {
            throw new IllegalArgumentException("Entity ID is required");
        }

        String normalizedType = entityType.toUpperCase();

        switch (normalizedType) {
            case "CATEGORY":
                if (!categoryRepository.existsById(entityId)) {
                    throw new IllegalArgumentException("Category with ID " + entityId + " not found");
                }
                break;
            case "PRODUCT":
                if (!productRepository.existsById(entityId)) {
                    throw new IllegalArgumentException("Product with ID " + entityId + " not found");
                }
                break;
            case "USER":
                if (!userRepository.existsById(entityId)) {
                    throw new IllegalArgumentException("User with ID " + entityId + " not found");
                }
                break;
            case "COMPANY":
                if (!companyRepository.existsById(entityId)) {
                    throw new IllegalArgumentException("Company with ID " + entityId + " not found");
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
    }

    private void validateEntityType(String entityType) {
        if (entityType == null || entityType.trim().isEmpty()) {
            throw new IllegalArgumentException("Entity type is required");
        }

        if (!ALLOWED_ENTITY_TYPES.contains(entityType.toUpperCase())) {
            throw new IllegalArgumentException(
                    "Invalid entity type: " + entityType + ". Must be one of: " +
                            String.join(", ", ALLOWED_ENTITY_TYPES)
            );
        }
    }

    private AttachmentDTO mapToDTO(Attachment attachment) {
        return new AttachmentDTO(
                attachment.getAttachmentId(),
                attachment.getFileName(),
                attachment.getFileType(),
                attachment.getData(), // Used to calculate size only
                attachment.getEntityType(),
                attachment.getEntityId(),
                attachment.getCreatedAt(),
                attachment.getLastUpdated()
        );
    }
}