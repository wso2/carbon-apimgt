package org.wso2.carbon.apimgt.gateway.event.analytics.util;

import org.wso2.carbon.apimgt.gateway.dto;

function getRequestEventPayload (dto:EventHolderDTO eventHolderDTO) (json) {
    json event = {};
    event.streamName= eventHolderDTO.streamName;
    event.executionPlanName= eventHolderDTO.executionPlanName;
    event.timestamp= eventHolderDTO.timestamp + "";
    dto:RequestEventDTO requestEventDTO = eventHolderDTO.requestEventDTO;
    dto:ResponseEventDTO responseEventDTO = eventHolderDTO.responseEventDTO;
    dto:LatencyEventDTO latencyEventDTO = eventHolderDTO.latencyEventDTO;


    json dataArr = [requestEventDTO.api, requestEventDTO.context, requestEventDTO.version, requestEventDTO.publisher,
                    requestEventDTO.subscriptionPolicy, requestEventDTO.uriTemplate, requestEventDTO.httpMethod,
                    requestEventDTO.consumerKey, requestEventDTO.applicationName, requestEventDTO.applicationId,
                    requestEventDTO.applicationOwner, requestEventDTO.userId, requestEventDTO.subscriber,
                    requestEventDTO.requestCount + "", requestEventDTO.requestTime + "", requestEventDTO.gatewayDomain,
                    requestEventDTO.gatewayIp, requestEventDTO. isThrottled, requestEventDTO.throttledReason,
                    requestEventDTO.throttledPolicy, requestEventDTO.clientIp, requestEventDTO.userAgent,
                    requestEventDTO.hostName, responseEventDTO.responseCount + "", responseEventDTO.responseTime + "",
                    responseEventDTO.cacheHit, responseEventDTO.contentLength + "", responseEventDTO.protocol,
                    responseEventDTO.statusCode + "", responseEventDTO.destination, latencyEventDTO.responseTime + "",
                    latencyEventDTO.serviceTime + "", latencyEventDTO.backendTime + "", latencyEventDTO.backendLatency + "",
                    latencyEventDTO.securityLatency + "", latencyEventDTO.throttlingLatency + "",
                    latencyEventDTO.request_mediationLatency + "", latencyEventDTO.response_mediationLatency + "",
                    latencyEventDTO.otherLatency + ""];

    event.data = dataArr;
    return event;
}

function getThrottleEventPayload (dto:ThrottleEventHolderDTO throttleEventHolderDTO) (json) {
    json event = {};
    event.streamName= throttleEventHolderDTO.streamName;
    event.executionPlanName= throttleEventHolderDTO.executionPlanName;
    event.timestamp= throttleEventHolderDTO.timestamp + "";

    dto:ThrottleEventDTO throttleEventDTO = throttleEventHolderDTO.throttleEventDTO;

    json dataArr = [throttleEventDTO.messageID, throttleEventDTO.appKey, throttleEventDTO.applicationTier,
                    throttleEventDTO.apiKey, throttleEventDTO.apiTier, throttleEventDTO.subscriptionKey,
                    throttleEventDTO.subscriptionTier, throttleEventDTO.resourceLevelThrottleKey,
                    throttleEventDTO.resourceTier, throttleEventDTO.userId, throttleEventDTO.apiContext,
                    throttleEventDTO.apiVersion, throttleEventDTO.appTenant, throttleEventDTO.apiTenant,
                    throttleEventDTO.applicationId, throttleEventDTO.apiName, throttleEventDTO.properties];

    event.data = dataArr;

    return event;
}

function validate (dto:EventHolderDTO eventHolderDTO) {

}