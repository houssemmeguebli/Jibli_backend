package com.backend.jibli.category;

import com.backend.jibli.attachment.AttachmentDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ICategoryService {
    List<CategoryDTO> getAllCategories();
    Optional<CategoryDTO> getCategoryById(Integer id);
    CategoryDTO createCategory(CategoryDTO dto);
    Optional<CategoryDTO> updateCategory(Integer id, CategoryDTO dto);
    boolean deleteCategory(Integer id);
    AttachmentDTO addAttachment(Integer categoryId, MultipartFile file);
    List<AttachmentDTO> getCategoryAttachments(Integer categoryId);
    List<CategoryDTO> findCategoriesByUserUserId(Integer userId);

}