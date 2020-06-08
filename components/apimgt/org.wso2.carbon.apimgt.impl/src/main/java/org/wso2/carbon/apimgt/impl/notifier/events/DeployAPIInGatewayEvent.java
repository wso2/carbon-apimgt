package org.wso2.carbon.apimgt.impl.notifier.events;

public class APIDeployInGatewayEvent extends Event {

    private String gatewayLabel;
    private String apiName;
    private String apiId;

    public APIDeployInGatewayEvent(String eventId, long timestamp, String type, int tenantId, String apiName, String apiId,
                                   String gatewayLabel) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantId = tenantId;

        this.apiName = apiName;
        this.apiId = apiId;
        this.gatewayLabel = gatewayLabel;

    }

    public String getGatewayLabel() {

        return gatewayLabel;
    }

    public void setGatewayLabel(String gatewayLabel) {

        this.gatewayLabel = gatewayLabel;
    }

    public String getApiName() {

        return apiName;
    }

    public void setApiName(String apiName) {

        this.apiName = apiName;
    }

    public String getApiId() {

        return apiId;
    }

    public void setApiId(String apiId) {

        this.apiId = apiId;
    }

}
