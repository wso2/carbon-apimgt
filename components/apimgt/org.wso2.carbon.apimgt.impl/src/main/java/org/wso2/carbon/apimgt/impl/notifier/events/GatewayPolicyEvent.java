package org.wso2.carbon.apimgt.impl.notifier.events;

import java.util.Set;

public class GatewayPolicyEvent extends Event{
    private String gatewayPolicyMappingUuid;
    private Set<String> gatewayLabels;

    public GatewayPolicyEvent(String eventId, long timestamp, String type, String tenantDomain, String gatewayPolicyMappingUuid,
                              Set<String> gatewayLabels) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantDomain = tenantDomain;
        this.gatewayPolicyMappingUuid = gatewayPolicyMappingUuid;
        this.gatewayLabels = gatewayLabels;
    }

    public String getGatewayPolicyMappingUuid() {
        return gatewayPolicyMappingUuid;
    }

    public void setGatewayPolicyMappingUuid(String gatewayPolicyMappingUuid) {
        this.gatewayPolicyMappingUuid = gatewayPolicyMappingUuid;
    }

    public Set<String> getGatewayLabels() {
        return gatewayLabels;
    }

    public void setGatewayLabels(Set<String> gatewayLabels) {
        this.gatewayLabels = gatewayLabels;
    }
}
