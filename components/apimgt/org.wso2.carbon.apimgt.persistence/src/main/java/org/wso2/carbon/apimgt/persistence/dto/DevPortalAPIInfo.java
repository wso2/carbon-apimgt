package org.wso2.carbon.apimgt.persistence.dto;

/**
 * A subset of org.wso2.carbon.apimgt.persistence.models.DevPortalAPI. Minimal API information required only for
 * listing
 * APIs in DevPortal which are stored in the persistence layer are included in this.
 */
public class DevPortalAPIInfo {
    private String name;
    private String version;
    private String context;
    private String provider;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
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

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
