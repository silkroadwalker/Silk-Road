package com.silkroad.market.dto.advertisement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RejectAdvertisementRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    public RejectAdvertisementRequest() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}