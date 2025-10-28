package com.backend.jibli.attachment;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;


import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface IAttachmentService {
    List<AttachmentDTO> getAllAttachments();
    Optional<AttachmentDTO> getAttachmentById(Integer id);
    AttachmentDTO createAttachment(MultipartFile file, String entityType, Integer entityId);
    Optional<AttachmentDTO> updateAttachment(Integer id, MultipartFile file, String entityType, Integer entityId);
    boolean deleteAttachment(Integer id);
    List<AttachmentDTO> getAttachmentsByEntity(String entityType, Integer entityId);
    List<AttachmentDTO> findByProductProductId(Integer productId);

}