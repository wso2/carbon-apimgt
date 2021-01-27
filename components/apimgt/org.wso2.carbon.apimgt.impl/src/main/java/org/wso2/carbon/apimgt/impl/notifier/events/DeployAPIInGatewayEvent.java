package org.wso2.carbon.apimgt.impl.notifier.events;

import java.util.Set;

public class DeployAPIInGatewayEvent extends Event {

    private String apiId;
    private Set<String> gatewayLabels;
    private String context;
    private String version;

    public DeployAPIInGatewayEvent(String eventId, long timestamp, String type, String tenanrDomain, String apiId,
                                   Set<String> gatewayLabels) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantDomain = tenanrDomain;

        this.apiId = apiId;
        this.gatewayLabels = gatewayLabels;

    }

    public DeployAPIInGatewayEvent(String eventId, long timestamp, String type, String tenanrDomain, String apiId,
                                   Set<String> gatewayLabels, String context, String version) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantDomain = tenanrDomain;

        this.apiId = apiId;
        this.gatewayLabels = gatewayLabels;
        this.context = context;
        this.version = version;

    }

    public Set<String> getGatewayLabels() {

        return gatewayLabels;
    }

    public void setGatewayLabels(Set<String> gatewayLabels) {

        this.gatewayLabels = gatewayLabels;
    }

    public String getApiId() {

        return apiId;
    }

    public void setApiId(String apiId) {

        this.apiId = apiId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
