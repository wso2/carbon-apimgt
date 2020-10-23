package org.wso2.carbon.apimgt.persistence.dto;

/**
 * A subset of org.wso2.carbon.apimgt.persistence.models.PublisherAPI. Minimal API information required only for
 * listing APIs in publisher which are stored in the
 * persistence layer are included in this.
 */
public class PublisherAPIInfo {
    String name;
    String version;
    String context;
    String provider;
    String status;

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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
