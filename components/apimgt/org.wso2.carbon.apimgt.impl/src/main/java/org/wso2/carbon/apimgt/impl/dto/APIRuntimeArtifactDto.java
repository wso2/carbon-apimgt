package org.wso2.carbon.apimgt.impl.dto;

public class APIRuntimeArtifactDto extends RuntimeArtifactDto {
    private String name;
    private String version;
    private String provider;
    private String tenantDomain;
    private String apiId;
    private String revision;
    private String label;
    private String type;

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getVersion() {

        return version;
    }

    public void setVersion(String version) {

        this.version = version;
    }

    public String getProvider() {

        return provider;
    }

    public void setProvider(String provider) {

        this.provider = provider;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    public String getApiId() {

        return apiId;
    }

    public void setApiId(String apiId) {

        this.apiId = apiId;
    }

    public String getRevision() {

        return revision;
    }

    public void setRevision(String revision) {

        this.revision = revision;
    }

    public String getLabel() {

        return label;
    }

    public void setLabel(String label) {

        this.label = label;
    }
}
