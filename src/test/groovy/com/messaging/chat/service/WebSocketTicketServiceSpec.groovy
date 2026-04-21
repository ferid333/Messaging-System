package com.messaging.chat.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import spock.lang.Specification

class WebSocketTicketServiceSpec extends Specification {

    private StringRedisTemplate redisTemplate
    private ValueOperations<String, String> valueOperations
    private WebSocketTicketService service

    def setup() {
        redisTemplate = Mock()
        valueOperations = Mock()
        service = new WebSocketTicketService(redisTemplate)
    }

    def "resolves user id from one-time redis ticket"() {
        when:
        def result = service.resolveUserId("abc")

        then:
        1 * redisTemplate.opsForValue() >> valueOperations
        1 * valueOperations.getAndDelete("chat:ws-ticket:abc") >> "123"
        0 * _

        and:
        result.present
        result.get() == 123L
    }

    def "returns empty when ticket is missing blank or invalid"() {
        when:
        def result = service.resolveUserId("abc")

        then:
        1 * redisTemplate.opsForValue() >> valueOperations
        1 * valueOperations.getAndDelete("chat:ws-ticket:abc") >> redisValue
        0 * _

        and:
        result.empty

        where:
        redisValue << [null, "", "not-number"]
    }
}
