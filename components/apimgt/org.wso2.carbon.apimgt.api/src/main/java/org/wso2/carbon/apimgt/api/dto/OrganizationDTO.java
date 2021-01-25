package org.wso2.carbon.apimgt.api.dto;

public class OrganizationDTO {

    private String requestedTenantDomain;
    private String orgId;

    public OrganizationDTO(String orgId) {
        this.orgId = orgId;
    }

    public String getRequestedTenantDomain() {
        return requestedTenantDomain;
    }

    public void setRequestedTenantDomain(String requestedTenantDomain) {
        this.requestedTenantDomain = requestedTenantDomain;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
}
