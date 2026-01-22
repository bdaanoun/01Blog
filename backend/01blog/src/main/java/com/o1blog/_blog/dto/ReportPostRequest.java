package com.o1blog._blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReportPostRequest {

    @NotBlank(message = "Reason is required")
    @Size(min = 10, max = 200, message = "Reason must be between 10 and 200 characters")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
