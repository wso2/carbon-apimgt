package org.wso2.carbon.apimgt.gatewaybridge.dto;

import java.io.Serializable;

/**
 * Class for representing the webhook subscription.
 */
public class WebhookSubscriptionDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String subscriberName;
    private String callback;
    private String topic;
    private long updatedTime;
    private long expiryTime;

    public WebhookSubscriptionDTO() {
    }

    public WebhookSubscriptionDTO(String subscriberName, String callback, String topic) {
        this.subscriberName = subscriberName;
        this.callback = callback;
        this.topic = topic;
        this.expiryTime = 10;
    }

    public WebhookSubscriptionDTO(String subscriberName, String callback, String topic, long expiryTime) {
        this.subscriberName = subscriberName;
        this.callback = callback;
        this.topic = topic;
        this.expiryTime = expiryTime;
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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
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

