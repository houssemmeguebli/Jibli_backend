package com.backend.jibli.attachment;


import com.backend.jibli.category.ICategoryRepository;
import com.backend.jibli.product.IProductRepository;
import com.backend.jibli.user.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttachmentService implements IAttachmentService {

    private static final List<String> ALLOWED_ENTITY_TYPES = Arrays.asList("CATEGORY", "PRODUCT");
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif",
            "application/pdf",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final IAttachmentRepository attachmentRepository;
    private final ICategoryRepository categoryRepository;
    private final IProductRepository productRepository;
    private final IUserRepository userRepository;

    @Autowired
    public AttachmentService(IAttachmentRepository attachmentRepository,
                             ICategoryRepository categoryRepository,
                             IProductRepository productRepository,
                             IUserRepository userRepository) {
        this.attachmentRepository = attachmentRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
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
        validateFile(file);
        validateEntity(entityType, entityId);
        try {
            Attachment attachment = new Attachment();
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileType(file.getContentType());
            attachment.setData(file.getBytes());
            attachment.setEntityType(entityType);
            attachment.setEntityId(entityId);
            Attachment saved = attachmentRepository.save(attachment);
            return mapToDTO(saved);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public Optional<AttachmentDTO> updateAttachment(Integer id, MultipartFile file, String entityType, Integer entityId) {
        return attachmentRepository.findById(id)
                .map(attachment -> {
                    if (file != null) {
                        validateFile(file);
                        try {
                            attachment.setFileName(file.getOriginalFilename());
                            attachment.setFileType(file.getContentType());
                            attachment.setData(file.getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to update file", e);
                        }
                    }
                    if (entityType != null) {
                        validateEntity(entityType, entityId != null ? entityId : attachment.getEntityId());
                        attachment.setEntityType(entityType);
                        if (entityId != null) {
                            attachment.setEntityId(entityId);
                        }
                    }
                    attachment.setLastUpdated(LocalDateTime.now());
                    Attachment updated = attachmentRepository.save(attachment);
                    return mapToDTO(updated);
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
        validateEntity(entityType, entityId);
        return attachmentRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        String fileType = file.getContentType();
        if (fileType == null || !ALLOWED_FILE_TYPES.contains(fileType)) {
            throw new IllegalArgumentException("Invalid file type. Allowed: " + String.join(", ", ALLOWED_FILE_TYPES));
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10MB");
        }
    }

    private void validateEntity(String entityType, Integer entityId) {
        if (entityType == null || !ALLOWED_ENTITY_TYPES.contains(entityType)) {
            throw new IllegalArgumentException("Entity type must be one of: " + String.join(", ", ALLOWED_ENTITY_TYPES));
        }
        if (entityId == null) {
            throw new IllegalArgumentException("Entity ID is required");
        }
        if (entityType.equals("CATEGORY") && !categoryRepository.existsById(entityId)) {
            throw new IllegalArgumentException("Category not found");
        }
        if (entityType.equals("PRODUCT") && !productRepository.existsById(entityId)) {
            throw new IllegalArgumentException("Product not found");
        }
    }

    private AttachmentDTO mapToDTO(Attachment attachment) {
        return new AttachmentDTO(
                attachment.getAttachmentId(),
                attachment.getFileName(),
                attachment.getFileType(),
                attachment.getData(), // Include data for download
                attachment.getEntityType(),
                attachment.getEntityId(),
                attachment.getCreatedAt(),
                attachment.getLastUpdated()
        );
    }
}