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
    private byte[] data;
    private String entityType;
    private Integer entityId;

    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;

}