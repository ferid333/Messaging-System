package com.messaging.chat.mapper

import com.messaging.chat.dao.entity.Attachment
import com.messaging.chat.model.constant.FileStatus
import com.messaging.chat.model.dto.request.PresignUploadRequest
import org.mapstruct.factory.Mappers
import spock.lang.Specification

class AttachmentMapperSpec extends Specification {

    private AttachmentMapper mapper = Mappers.getMapper(AttachmentMapper)

    def "maps presign request to pending attachment entity"() {
        given:
        def request = new PresignUploadRequest("photo.png", "image/png", 1024L, "checksum")

        when:
        def entity = mapper.toPendingEntity(1L, "attachments/1/photo.png", request)

        then:
        entity.id == null
        entity.uploaderId == 1L
        entity.storageKey == "attachments/1/photo.png"
        entity.originalFileName == "photo.png"
        entity.contentType == "image/png"
        entity.fileSize == 1024L
        entity.checksum == "checksum"
        entity.status == FileStatus.PENDING
        entity.message == null
    }

    def "maps attachment to confirm response"() {
        given:
        def attachment = new Attachment(
                id: 10L,
                storageKey: "attachments/1/photo.png",
                status: FileStatus.UPLOADED
        )

        when:
        def response = mapper.toAttachmentConfirmResponse(attachment)

        then:
        response.id() == 10L
        response.storageKey() == "attachments/1/photo.png"
        response.status() == FileStatus.UPLOADED
    }
}
