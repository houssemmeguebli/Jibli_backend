package com.backend.jibli.attachment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDTO {
    private Integer attachmentId;
    private String fileName;
    private String fileType;
    private Long fileSize; // In bytes
    private String entityType;
    private Integer entityId;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;

    // Constructor without data - used for listing
    public AttachmentDTO(Integer attachmentId, String fileName, String fileType,
                         byte[] data, String entityType, Integer entityId,
                         LocalDateTime createdAt, LocalDateTime lastUpdated) {
        this.attachmentId = attachmentId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = data != null ? (long) data.length : 0L;
        this.entityType = entityType;
        this.entityId = entityId;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
    }
}