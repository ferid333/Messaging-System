package com.messaging.chat.logging;

import java.util.Arrays;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DPLogger {

    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\w)(\\+?\\d[\\d\\s().-]{7,}\\d)(?!\\w)");

    private final Logger logger;

    private DPLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public static DPLogger getLogger(Class<?> clazz) {
        return new DPLogger(clazz);
    }

    public void info(String msg, Object... args) {
        logger.info(mask(msg), maskArgs(args));
    }

    public void debug(String msg, Object... args) {
        logger.debug(mask(msg), maskArgs(args));
    }

    public void warn(String msg, Object... args) {
        logger.warn(mask(msg), maskArgs(args));
    }

    public void error(String msg, Object... args) {
        logger.error(mask(msg), maskArgs(args));
    }

    private String mask(String message) {
        if (message == null) {
            return null;
        }
        return PHONE_PATTERN.matcher(message).replaceAll(this::maskPhoneNumber);
    }

    private Object[] maskArgs(Object[] args) {
        if (args == null) {
            return null;
        }

        return Arrays.stream(args)
                .map(arg -> arg instanceof String text ? mask(text) : arg)
                .toArray();
    }

    private String maskPhoneNumber(MatchResult matchResult) {
        String original = matchResult.group(1);
        String digitsOnly = original.replaceAll("\\D", "");

        if (digitsOnly.length() <= 4) {
            return original;
        }

        int digitsToMask = digitsOnly.length() - 4;
        int maskedDigits = 0;
        StringBuilder result = new StringBuilder(original.length());

        for (char currentChar : original.toCharArray()) {
            if (Character.isDigit(currentChar) && maskedDigits < digitsToMask) {
                result.append('*');
                maskedDigits++;
            } else {
                result.append(currentChar);
            }
        }

        return result.toString();
    }
}
