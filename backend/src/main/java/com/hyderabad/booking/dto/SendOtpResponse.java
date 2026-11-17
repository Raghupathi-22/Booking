package com.hyderabad.booking.dto;

public class SendOtpResponse {
    private String message;
    private String devOtp; // Included in dev response for convenience

    public SendOtpResponse() {}

    public SendOtpResponse(String message, String devOtp) {
        this.message = message;
        this.devOtp = devOtp;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getDevOtp() { return devOtp; }
    public void setDevOtp(String devOtp) { this.devOtp = devOtp; }
}
