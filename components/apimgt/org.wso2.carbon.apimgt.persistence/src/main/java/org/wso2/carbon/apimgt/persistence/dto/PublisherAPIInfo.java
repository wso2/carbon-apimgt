package org.wso2.carbon.apimgt.persistence.dto;

/**
 * A subset of org.wso2.carbon.apimgt.persistence.models.PublisherAPI. Minimal API information required only for
 * listing APIs in publisher which are stored in the
 * persistence layer are included in this.
 */
public class PublisherAPIInfo {
    private String id;
    private String apiName;
    private String version;
    private String providerName;
    private String context;
    private String status;
    private String type;
    private String thumbnail; // thumbnail is not required for normal ApiGet

    public String getApiName() {
        return apiName;
    }

    public String getVersion() {
        return version;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
