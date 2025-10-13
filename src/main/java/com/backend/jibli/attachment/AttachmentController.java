package com.backend.jibli.attachment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {

    private final IAttachmentService attachmentService;
    private final IAttachmentRepository attachmentRepository;

    @Autowired
    public AttachmentController(IAttachmentService attachmentService, IAttachmentRepository attachmentRepository) {
        this.attachmentService = attachmentService;
        this.attachmentRepository = attachmentRepository;
    }

    @GetMapping
    public ResponseEntity<List<AttachmentDTO>> getAllAttachments() {
        List<AttachmentDTO> attachments = attachmentService.getAllAttachments();
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttachmentDTO> getAttachmentById(@PathVariable Integer id) {
        return attachmentService.getAttachmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> downloadAttachment(@PathVariable Integer id) {
        return attachmentRepository.findById(id)
                .map(attachment -> {
                    ByteArrayResource resource = new ByteArrayResource(attachment.getData());
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(attachment.getFileType()))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                            .body(resource);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AttachmentDTO> createAttachment(@RequestParam("file") MultipartFile file,
                                                          @RequestParam("entityType") String entityType,
                                                          @RequestParam("entityId") Integer entityId) {
        try {
            AttachmentDTO created = attachmentService.createAttachment(file, entityType, entityId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Integer id) {
        boolean deleted = attachmentService.deleteAttachment(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AttachmentDTO>> getAttachmentsByEntity(@PathVariable String entityType, @PathVariable Integer entityId) {
        try {
            List<AttachmentDTO> attachments = attachmentService.getAttachmentsByEntity(entityType, entityId);
            return ResponseEntity.ok(attachments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
