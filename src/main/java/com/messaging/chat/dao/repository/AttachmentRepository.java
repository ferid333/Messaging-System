package com.messaging.chat.dao.repository;

import com.messaging.chat.dao.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findAllByIdIn(Collection<Long> attachmentIds);
}
