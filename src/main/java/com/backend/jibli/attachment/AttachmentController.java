package com.backend.jibli.attachment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {

    private final IAttachmentService attachmentService;
    private final IAttachmentRepository attachmentRepository;

    @Autowired
    public AttachmentController(IAttachmentService attachmentService,
                                IAttachmentRepository attachmentRepository) {
        this.attachmentService = attachmentService;
        this.attachmentRepository = attachmentRepository;
    }

    @GetMapping
    public ResponseEntity<List<AttachmentDTO>> getAllAttachments() {
        List<AttachmentDTO> attachments = attachmentService.getAllAttachments();
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAttachmentById(@PathVariable Integer id) {
        return attachmentService.getAttachmentById(id)
                .map(dto -> ResponseEntity.ok(dto))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body((AttachmentDTO) createErrorResponse("Attachment not found with ID: " + id))); // Pas de cast
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadAttachment(@PathVariable Integer id) {
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
        if (optionalAttachment.isPresent()) {
            Attachment attachment = optionalAttachment.get();
            if (attachment.getData() == null || attachment.getData().length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Attachment data not found for ID: " + id));
            }
            ByteArrayResource resource = new ByteArrayResource(attachment.getData());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                    .contentLength(attachment.getData().length)
                    .body(resource);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Attachment not found with ID: " + id)); // Pas de cast
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Integer entityId) {
        try {
            AttachmentDTO created = attachmentService.createAttachment(file, entityType, entityId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to upload attachment: " + e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateAttachment(
            @PathVariable Integer id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "entityType", required = false) String entityType,
            @RequestParam(value = "entityId", required = false) Integer entityId) {
        try {
            return attachmentService.updateAttachment(id, file, entityType, entityId)
                    .map(dto -> ResponseEntity.ok(dto))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body((AttachmentDTO) createErrorResponse("Attachment not found with ID: " + id))); // Pas de cast
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update attachment: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAttachment(@PathVariable Integer id) {
        try {
            boolean deleted = attachmentService.deleteAttachment(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Attachment not found with ID: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to delete attachment: " + e.getMessage()));
        }
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<?> getAttachmentsByEntity(
            @PathVariable String entityType,
            @PathVariable Integer entityId) {
        try {
            List<AttachmentDTO> attachments = attachmentService
                    .getAttachmentsByEntity(entityType, entityId);
            return ResponseEntity.ok(attachments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve attachments: " + e.getMessage()));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}