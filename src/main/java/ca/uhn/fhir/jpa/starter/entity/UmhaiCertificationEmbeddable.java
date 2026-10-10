package ca.uhn.fhir.jpa.starter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

/**
 * A regulatory, security or clinical certification held by an application
 * (maps to UmhaiInstance.certification). Stored in the
 * umhai_instance_certifications collection table.
 */
@Embeddable
public class UmhaiCertificationEmbeddable {

    @Column(name = "cert_type", length = 50, nullable = false)
    private String type;

    @Column(name = "cert_status", length = 20, nullable = false)
    private String status;

    @Column(name = "issued")
    @Temporal(TemporalType.TIMESTAMP)
    private Date issued;

    @Column(name = "expires")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expires;

    @Column(name = "validator", length = 200)
    private String validator;

    public UmhaiCertificationEmbeddable() {
    }

    public UmhaiCertificationEmbeddable(String type, String status, Date issued, Date expires, String validator) {
        this.type = type;
        this.status = status;
        this.issued = issued;
        this.expires = expires;
        this.validator = validator;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getIssued() { return issued; }
    public void setIssued(Date issued) { this.issued = issued; }

    public Date getExpires() { return expires; }
    public void setExpires(Date expires) { this.expires = expires; }

    public String getValidator() { return validator; }
    public void setValidator(String validator) { this.validator = validator; }
}
