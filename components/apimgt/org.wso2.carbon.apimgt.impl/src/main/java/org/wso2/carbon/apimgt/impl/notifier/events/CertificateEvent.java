package org.wso2.carbon.apimgt.impl.notifier.events;

public class CertificateEvent extends Event {
    private String alias;
    private String endpoint;

    public CertificateEvent(String eventId, long timeStamp, String type, String tenantDomain, String alias,
                            String endpoint) {

        this.alias = alias;
        this.endpoint = endpoint;
        this.eventId = eventId;
        this.timeStamp = timeStamp;
        this.type = type;
        this.tenantDomain = tenantDomain;
    }

    public String getAlias() {

        return alias;
    }

    public void setAlias(String alias) {

        this.alias = alias;
    }

    public String getEndpoint() {

        return endpoint;
    }

    public void setEndpoint(String endpoint) {

        this.endpoint = endpoint;
    }
}
