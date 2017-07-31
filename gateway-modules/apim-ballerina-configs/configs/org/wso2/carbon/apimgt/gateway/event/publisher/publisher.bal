package org.wso2.carbon.apimgt.gateway.event.publisher;

import org.wso2.carbon.apimgt.gateway.dto;
import org.wso2.carbon.apimgt.gateway.event.analytics.util;
import org.wso2.carbon.apimgt.gateway.event.holder;
import org.wso2.carbon.apimgt.ballerina.publisher;

function publishRequestEvent (dto:EventHolderDTO event) {
    json payload = util:getRequestEventPayload(event);
    publisher:EventConnector das = holder:getAnalyticsPublisher();
    publisher:EventConnector.publish(das, payload);
}

function publishThrottleAnalyticsEvent (dto:ThrottleEventAnalyticsHolderDTO event) {
    json payload = util:getThrottleAnalyticsEventPayload(event);
    publisher:EventConnector das = holder:getAnalyticsPublisher();
    publisher:EventConnector.publish(das, payload);
}

function publishFaultEvent (dto:FaultEventHolderDTO event) {
    json payload = util:getFaultEventPayload(event);
    publisher:EventConnector das = holder:getAnalyticsPublisher();
    publisher:EventConnector.publish(das, payload);
}

function publishThrottleEvent (dto:ThrottleEventHolderDTO event) {
    json payload = util:getThrottleEventPayload(event);
    publisher:EventConnector das = holder:getThrottlingPublisher();
    publisher:EventConnector.publish(das, payload);
}
