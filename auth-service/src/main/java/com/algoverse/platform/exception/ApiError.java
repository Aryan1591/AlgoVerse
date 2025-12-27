package com.algoverse.platform.exception;

import java.time.LocalDateTime;
import java.util.List;

public class ApiError {

    private LocalDateTime timestamp;
    private List<String> message;
    private String details;

    public ApiError(LocalDateTime timestamp, List<String> message, String details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public List<String> getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }
}
