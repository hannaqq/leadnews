package com.news.wemedia.service;

public class PublicationScheduleException extends RuntimeException {

    public PublicationScheduleException(String message) {
        super(message);
    }

    public PublicationScheduleException(String message, Throwable cause) {
        super(message, cause);
    }
}
