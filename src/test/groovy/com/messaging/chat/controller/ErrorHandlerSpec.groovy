package com.messaging.chat.controller

import com.messaging.chat.model.exceptions.FileValidationException
import com.messaging.chat.model.exceptions.ResourceNotFound
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

class ErrorHandlerSpec extends Specification {

    private ErrorHandler errorHandler = new ErrorHandler()

    def "resource not found maps to 404 response"() {
        when:
        def response = errorHandler.handleResourceNotFound(new ResourceNotFound("missing"))

        then:
        response.statusCode == HttpStatus.NOT_FOUND
        response.body == [message: "missing"]
    }

    def "response status exception keeps status and reason"() {
        when:
        def response = errorHandler.handleResponseStatusException(
                new ResponseStatusException(HttpStatus.FORBIDDEN, "denied")
        )

        then:
        response.statusCode == HttpStatus.FORBIDDEN
        response.body == [message: "denied"]
    }

    def "generic exception maps to 500 without leaking details"() {
        when:
        def response = errorHandler.handleException(new RuntimeException("secret"))

        then:
        response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
        response.body == [message: "Internal server error"]
    }

    def "file validation exception maps to message body"() {
        expect:
        errorHandler.handleFileValidationException(new FileValidationException("bad file")) == [message: "bad file"]
    }
}
