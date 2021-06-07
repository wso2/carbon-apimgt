package org.wso2.carbon.apimgt.gatewaybridge.dto;

import java.io.Serializable;

/**
 * Class for representing the webhook subscription.
 */
public class WebhookSubscriptionDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String subscriberName;
    private String callback;
    private String vHost;
    private long updatedTime;
    private long expiryTime;

    public WebhookSubscriptionDTO() {
    }

    public WebhookSubscriptionDTO(String subscriberName, String callback, String vHost) {
        this.subscriberName = subscriberName;
        this.callback = callback;
        this.vHost = vHost;
        this.expiryTime = 10;
    }

    public WebhookSubscriptionDTO(String subscriberName, String callback, String vHost, long expiryTime) {
        this.subscriberName = subscriberName;
        this.callback = callback;
        this.vHost = vHost;
        this.expiryTime = expiryTime;
    }

    public String getvHost() {
        return vHost;
    }

    public void setvHost(String vHost) {
        this.vHost = vHost;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }
}

