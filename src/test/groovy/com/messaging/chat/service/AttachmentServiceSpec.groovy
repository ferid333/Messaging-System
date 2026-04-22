package com.messaging.chat.service

import com.messaging.chat.dao.entity.Attachment
import com.messaging.chat.dao.repository.AttachmentRepository
import com.messaging.chat.mapper.AttachmentMapper
import com.messaging.chat.model.constant.FileStatus
import com.messaging.chat.model.dto.request.PresignUploadRequest
import com.messaging.chat.model.exceptions.FileValidationException
import com.messaging.chat.model.exceptions.ResourceNotFound
import org.mapstruct.factory.Mappers
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.presigner.S3Presigner

class AttachmentServiceSpec extends Specification {

    private S3Presigner s3Presigner
    private S3Client s3Client
    private AttachmentRepository attachmentRepository
    private AttachmentService service

    def setup() {
        s3Presigner = Mock()
        s3Client = Mock()
        attachmentRepository = Mock()
        service = new AttachmentService(
                s3Presigner,
                s3Client,
                attachmentRepository,
                Mappers.getMapper(AttachmentMapper)
        )
        ReflectionTestUtils.setField(service, "bucketName", "bucket")
        ReflectionTestUtils.setField(service, "presignDurationMinutes", 5L)
    }

    def "presign upload rejects invalid file before saving"() {
        when:
        service.presignUpload(1L, request)

        then:
        thrown(FileValidationException)
        0 * attachmentRepository._
        0 * s3Presigner._

        where:
        request << [
                new PresignUploadRequest("big.png", "image/png", 5L * 1024L * 1024L, null),
                new PresignUploadRequest("script.sh", "text/plain", 1024L, null),
                new PresignUploadRequest("photo.png", "application/x-msdownload", 1024L, null)
        ]
    }

    def "confirm upload marks attachment uploaded when object metadata matches"() {
        given:
        def attachment = attachment(FileStatus.PENDING)
        def headObject = HeadObjectResponse.builder()
                .contentLength(1024L)
                .contentType("image/png")
                .build()

        when:
        def response = service.confirmUpload(1L, 10L)

        then:
        1 * attachmentRepository.findById(10L) >> Optional.of(attachment)
        1 * s3Client.headObject({ it.bucket() == "bucket" && it.key() == "attachments/1/photo.png" }) >> headObject
        1 * attachmentRepository.save({ it.status == FileStatus.UPLOADED }) >> { Attachment saved -> saved }
        0 * _

        and:
        response.id() == 10L
        response.storageKey() == "attachments/1/photo.png"
        response.status() == FileStatus.UPLOADED
    }

    def "confirm upload throws not found when attachment does not exist"() {
        when:
        service.confirmUpload(1L, 10L)

        then:
        1 * attachmentRepository.findById(10L) >> Optional.empty()
        0 * _
        thrown(ResourceNotFound)
    }

    def "confirm upload marks failed when uploaded metadata does not match"() {
        given:
        def attachment = attachment(FileStatus.PENDING)
        def headObject = HeadObjectResponse.builder()
                .contentLength(99L)
                .contentType("image/png")
                .build()

        when:
        service.confirmUpload(1L, 10L)

        then:
        1 * attachmentRepository.findById(10L) >> Optional.of(attachment)
        1 * s3Client.headObject(_) >> headObject
        1 * attachmentRepository.save({ it.status == FileStatus.FAILED }) >> { Attachment saved -> saved }
        0 * _
        thrown(FileValidationException)
    }

    private static Attachment attachment(FileStatus status) {
        new Attachment(
                id: 10L,
                storageKey: "attachments/1/photo.png",
                originalFileName: "photo.png",
                contentType: "image/png",
                fileSize: 1024L,
                status: status
        )
    }
}
