package org.commerce.authenticationservice.exception.response;

import java.time.LocalDateTime;
import java.util.List;

public class ExceptionValidatorResponse {

    private String message;
    private int httpStatus;
    private LocalDateTime timestamp;
    private String path;
    private List<String> details;

    public ExceptionValidatorResponse() {
    }

    public ExceptionValidatorResponse(String message, int httpStatus, String path, List<String> details) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.httpStatus = httpStatus;
        this.path = path;
        this.details = details;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }
}
