package com.messaging.chat.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.slf4j.LoggerFactory
import spock.lang.Specification

class DPLoggerSpec extends Specification {

    private Logger delegateLogger
    private ListAppender<ILoggingEvent> appender
    private DPLogger logger

    def setup() {
        delegateLogger = (Logger) LoggerFactory.getLogger(DPLoggerSpec)
        delegateLogger.level = Level.DEBUG
        delegateLogger.additive = false

        appender = new ListAppender<>()
        appender.start()
        delegateLogger.addAppender(appender)

        logger = DPLogger.getLogger(DPLoggerSpec)
    }

    def cleanup() {
        delegateLogger.detachAppender(appender)
        appender.stop()
    }

    def "masks phone number written in log message"() {
        when:
        logger.info("User phone is 0501234567")

        then:
        appender.list.size() == 1
        appender.list[0].formattedMessage == "User phone is ******4567"
    }

    def "masks phone number passed as string argument"() {
        when:
        logger.info("User phone is {}", "0501234567")

        then:
        appender.list.size() == 1
        appender.list[0].formattedMessage == "User phone is ******4567"
    }

    def "preserves formatting while masking phone digits"() {
        when:
        logger.info("International phone {}", "+994 50 123 45 67")

        then:
        appender.list.size() == 1
        appender.list[0].formattedMessage == "International phone +*** ** *** 45 67"
    }

    def "does not mask non string arguments or short numeric values"() {
        when:
        logger.info("User id {} code {}", 123456789L, "1234")

        then:
        appender.list.size() == 1
        appender.list[0].formattedMessage == "User id 123456789 code 1234"
    }

    def "masks phone numbers for every supported level"() {
        when:
        logAction(logger)

        then:
        appender.list.size() == 1
        appender.list[0].level == expectedLevel
        appender.list[0].formattedMessage == "Phone ******4567"

        where:
        expectedLevel | logAction
        Level.INFO    | { DPLogger logger -> logger.info("Phone 0501234567") }
        Level.DEBUG   | { DPLogger logger -> logger.debug("Phone 0501234567") }
        Level.WARN    | { DPLogger logger -> logger.warn("Phone 0501234567") }
        Level.ERROR   | { DPLogger logger -> logger.error("Phone 0501234567") }
    }
}
