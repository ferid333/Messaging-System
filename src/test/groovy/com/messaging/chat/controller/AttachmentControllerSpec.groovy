package com.messaging.chat.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.messaging.chat.model.constant.FileStatus
import com.messaging.chat.model.dto.request.PresignUploadRequest
import com.messaging.chat.model.dto.response.AttachmentConfirmResponse
import com.messaging.chat.model.dto.response.PresignUploadResponse
import com.messaging.chat.service.AttachmentService
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import spock.lang.Specification

import java.time.Instant

import static com.messaging.chat.model.constant.Headers.USER_ID_HEADER
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AttachmentControllerSpec extends Specification {

    private AttachmentService attachmentService
    private MockMvc mockMvc
    private ObjectMapper objectMapper

    def setup() {
        attachmentService = Mock()
        objectMapper = new ObjectMapper().findAndRegisterModules()

        def validator = new LocalValidatorFactoryBean()
        validator.afterPropertiesSet()

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AttachmentController(attachmentService))
                .setValidator(validator)
                .build()
    }

    def "presign upload delegates to service and returns response"() {
        given:
        def request = new PresignUploadRequest(
                "photo.png",
                "image/png",
                1024L,
                "1B2M2Y8AsgTpgAmY7PhCfg=="
        )
        def response = new PresignUploadResponse(
                10L,
                "attachments/1/photo.png",
                "https://s3.example/upload",
                "PUT",
                Instant.parse("2026-04-19T02:17:36Z"),
                ["content-type": "image/png"]
        )

        when:
        def result = mockMvc.perform(post("/ms-chat/attachments/presign-upload")
                .header(USER_ID_HEADER, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then:
        1 * attachmentService.presignUpload(1L, {
            it.fileName() == "photo.png" &&
                    it.contentType() == "image/png" &&
                    it.fileSize() == 1024L &&
                    it.checksum() == "1B2M2Y8AsgTpgAmY7PhCfg=="
        }) >> response
        0 * _

        and:
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.attachmentId').value(10))
                .andExpect(jsonPath('$.storageKey').value("attachments/1/photo.png"))
                .andExpect(jsonPath('$.uploadUrl').value("https://s3.example/upload"))
                .andExpect(jsonPath('$.httpMethod').value("PUT"))
                .andExpect(jsonPath('$.expiresAt').value("2026-04-19T02:17:36Z"))
                .andExpect(jsonPath('$.uploadHeaders.content-type').value("image/png"))
    }

    def "presign upload returns bad request when body is invalid"() {
        given:
        def invalidBody = [
                fileName   : "",
                contentType: "image/png",
                fileSize   : 1024L
        ]

        when:
        def result = mockMvc.perform(post("/ms-chat/attachments/presign-upload")
                .header(USER_ID_HEADER, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBody)))

        then:
        0 * attachmentService._

        and:
        result.andExpect(status().isBadRequest())
    }

    def "presign upload returns bad request when user id header is missing"() {
        given:
        def request = new PresignUploadRequest("photo.png", "image/png", 1024L, null)

        when:
        def result = mockMvc.perform(post("/ms-chat/attachments/presign-upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

        then:
        0 * attachmentService._

        and:
        result.andExpect(status().isBadRequest())
    }

    def "confirm upload delegates to service and returns response"() {
        given:
        def response = new AttachmentConfirmResponse(
                10L,
                "attachments/1/photo.png",
                FileStatus.UPLOADED
        )

        when:
        def result = mockMvc.perform(post("/ms-chat/attachments/10/confirm")
                .header(USER_ID_HEADER, "1"))

        then:
        1 * attachmentService.confirmUpload(1L, 10L) >> response
        0 * _

        and:
        result.andExpect(status().isOk())
                .andExpect(jsonPath('$.id').value(10))
                .andExpect(jsonPath('$.storageKey').value("attachments/1/photo.png"))
                .andExpect(jsonPath('$.status').value("UPLOADED"))
    }

    def "confirm upload returns bad request when user id header is missing"() {
        when:
        def result = mockMvc.perform(post("/ms-chat/attachments/10/confirm"))

        then:
        0 * attachmentService._

        and:
        result.andExpect(status().isBadRequest())
    }
}
