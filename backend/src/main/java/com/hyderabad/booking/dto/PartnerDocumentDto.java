package com.hyderabad.booking.dto;

import com.hyderabad.booking.model.PartnerDocument;

public class PartnerDocumentDto {
    private Long id;
    private Long partnerId;
    private String docType;
    private String url;

    public PartnerDocumentDto() {}

    public PartnerDocumentDto(PartnerDocument doc) {
        this.id = doc.getId();
        if (doc.getPartner() != null) {
            this.partnerId = doc.getPartner().getId();
        }
        this.docType = doc.getDocType();
        this.url = doc.getUrl();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }

    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
