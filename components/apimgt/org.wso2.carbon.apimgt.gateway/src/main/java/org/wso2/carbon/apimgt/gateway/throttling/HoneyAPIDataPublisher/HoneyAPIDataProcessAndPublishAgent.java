package org.wso2.carbon.apimgt.gateway.throttling.HoneyAPIDataPublisher;

import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.databridge.agent.DataPublisher;


public class HoneyAPIDataProcessAndPublishAgent implements Runnable {

    private static final String streamID = "org.wso2.honeypotAPI.request.stream:1.0.0";
    private final DataPublisher dataPublisher;

    private long currentTime;
    private String messageId;
    private String headerSet;
    private String messageBody;
    private String clientIp;
    private String apiMethod;

    /**
     * constructor of data agent
     */
    HoneyAPIDataProcessAndPublishAgent() {

        dataPublisher = getDataPublisher();
    }

    /**
     * This method will clean data references. This method should call whenever we return data process and publish
     * agent back to pool. Every time when we add new property we need to implement cleaning logic as well.
     */
    void clearDataReference() {

        this.messageBody = null;
        this.apiMethod = null;
        this.headerSet = null;
        this.clientIp = null;
    }

    /**
     * set required parameters which needs to publish from GW to TM
     * They are currentTime, messageId, apiMethod, headerSet, messageBody, clientIp
     */
    void setDataReference(long currentTime, String messageId, String apiMethod, String headerSet, String messageBody, String clientIp) {

        this.currentTime = currentTime;
        this.messageId = messageId;
        this.apiMethod = apiMethod;
        this.headerSet = headerSet;
        this.messageBody = messageBody;
        this.clientIp = clientIp;

    }

    /**
     * Finally publish data to TM side
     */
    public void run() {

        Object[] objects = new Object[]{this.currentTime, this.messageId, this.apiMethod, this.headerSet, this.messageBody,
                this.clientIp};
        org.wso2.carbon.databridge.commons.Event event = new org.wso2.carbon.databridge.commons.Event(streamID,
                System.currentTimeMillis(), null, null, objects);
        dataPublisher.tryPublish(event);
    }

    /**
     * gete throttle properties to initiate the publishing
     */
    protected ThrottleProperties getThrottleProperties() {
        return ServiceReferenceHolder.getInstance().getThrottleProperties();
    }

    /**
     * get data publisher
     */
    private DataPublisher getDataPublisher() {
        return HoneyAPIDataPublisher.getDataPublisher();
    }
}
