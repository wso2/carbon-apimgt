package org.wso2.carbon.apimgt.gateway.dto;

public class TenantInfo {
    private String domain;
    private int tenantId;
    private boolean active;
    private String email;
    private String adminFirstName;
    private String adminLastName;
    private String adminFullName;
    private String admin;

    public TenantInfo() {
    }

    public TenantInfo(String domain, int tenantId, boolean active, String email, String adminFirstName,
                      String adminLastName, String adminFullName, String admin) {
        this.domain = domain;
        this.tenantId = tenantId;
        this.active = active;
        this.email = email;
        this.adminFirstName = adminFirstName;
        this.adminLastName = adminLastName;
        this.adminFullName = adminFullName;
        this.admin = admin;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdminFirstName() {
        return adminFirstName;
    }

    public void setAdminFirstName(String adminFirstName) {
        this.adminFirstName = adminFirstName;
    }

    public String getAdminLastName() {
        return adminLastName;
    }

    public void setAdminLastName(String adminLastName) {
        this.adminLastName = adminLastName;
    }

    public String getAdminFullName() {
        return adminFullName;
    }

    public void setAdminFullName(String adminFullName) {
        this.adminFullName = adminFullName;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }
}