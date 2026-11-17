package com.hyderabad.booking.model;

import jakarta.persistence.*;

@Entity
@Table(name = "partner_documents")
public class PartnerDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Column(name = "doc_type", nullable = false, length = 50)
    private String docType;

    @Column(nullable = false, length = 255)
    private String url;

    public PartnerDocument() {}

    public PartnerDocument(Partner partner, String docType, String url) {
        this.partner = partner;
        this.docType = docType;
        this.url = url;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Partner getPartner() { return partner; }
    public void setPartner(Partner partner) { this.partner = partner; }

    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
