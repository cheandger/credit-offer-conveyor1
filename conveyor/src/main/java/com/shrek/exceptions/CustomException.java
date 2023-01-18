package com.shrek.exceptions;

import java.time.OffsetDateTime;
import java.util.UUID;

public abstract class CustomException extends RuntimeException {

    private final int code;
    private final String id = UUID.randomUUID().toString();
    private final OffsetDateTime timestamp = OffsetDateTime.now();

    public CustomException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getId() {
        return id;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+" {" +
                "code=" + code +
                ", id='" + id + '\'' +
                ", timestamp=" + timestamp +
                ", message=" + super.getMessage() +
                '}';
    }
}
