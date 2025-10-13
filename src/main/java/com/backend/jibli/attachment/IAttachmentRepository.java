package com.backend.jibli.attachment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IAttachmentRepository extends JpaRepository<Attachment, Integer> {
    List<Attachment> findByEntityTypeAndEntityId(String entityType, Integer entityId);
}