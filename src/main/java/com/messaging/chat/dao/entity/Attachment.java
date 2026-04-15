package com.messaging.chat.dao.entity;

import com.messaging.chat.model.constant.FileStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "attachments")
@Getter
@Setter
public class Attachment extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Column(name = "uploader_id")
    private Long uploaderId;

    @Column(name = "storage_key")
    private String storageKey;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "thumbnail_storage_key")
    private String thumbnailStorageKey;

    @Column(name = "public_url")
    private String publicUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FileStatus status;

}
