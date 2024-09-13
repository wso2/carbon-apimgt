package org.wso2.carbon.apimgt.impl.notifier.events;

public class LLMProviderEvent extends Event {

    private String name;
    private String apiVersion;

    public LLMProviderEvent(String eventId, long timeStamp, String type, int tenantId, String tenantDomain,
                            String name, String apiVersion) {

        this.eventId = eventId;
        this.timeStamp = timeStamp;
        this.type = type;
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
        this.name = name;
        this.apiVersion = apiVersion;
    }

}
