package org.wso2.carbon.apimgt.impl.notifier.events;

import java.util.Set;

public class DeployAPIInGatewayEvent extends Event {

    private String apiId;
    private Set<String> gatewayLabels;

    public DeployAPIInGatewayEvent(String eventId, long timestamp, String type, int tenantId, String apiId,
                                   Set<String> gatewayLabels) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantId = tenantId;

        this.apiId = apiId;
        this.gatewayLabels = gatewayLabels;

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

}
