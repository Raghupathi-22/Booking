package com.hyderabad.booking.dto;

import jakarta.validation.constraints.NotBlank;

public class PartnerDocumentRequest {

    @NotBlank(message = "Document type is required")
    private String docType;

    @NotBlank(message = "URL is required")
    private String url;

    public PartnerDocumentRequest() {}

    public PartnerDocumentRequest(String docType, String url) {
        this.docType = docType;
        this.url = url;
    }

    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
