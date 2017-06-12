package org.wso2.carbon.apimgt.gateway.event.analytics.util;

import org.wso2.carbon.apimgt.gateway.dto;

function getRequestEventPayload (dto:EventHolderDTO eventHolderDTO) (json) {
    json event = {};
    event.streamName = eventHolderDTO.streamName;
    event.streamVersion = eventHolderDTO.version;
    dto:RequestEventDTO requestEventDTO = eventHolderDTO.requestEventDTO;
    dto:ResponseEventDTO responseEventDTO = eventHolderDTO.responseEventDTO;
    dto:LatencyEventDTO latencyEventDTO = eventHolderDTO.latencyEventDTO;

    json metaData = [];
    json correlationData = [];
    json payloadData = [requestEventDTO.api, requestEventDTO.context, requestEventDTO.version, requestEventDTO.publisher,
                    requestEventDTO.subscriptionPolicy, requestEventDTO.uriTemplate, requestEventDTO.httpMethod,
                    requestEventDTO.consumerKey, requestEventDTO.applicationName, requestEventDTO.applicationId,
                    requestEventDTO.applicationOwner, requestEventDTO.userId, requestEventDTO.subscriber,
                    requestEventDTO.requestCount , requestEventDTO.requestTime , requestEventDTO.gatewayDomain,
                    requestEventDTO.gatewayIp, requestEventDTO. isThrottled, requestEventDTO.throttledReason,
                    requestEventDTO.throttledPolicy, requestEventDTO.clientIp, requestEventDTO.userAgent,
                    requestEventDTO.hostName, responseEventDTO.responseCount , responseEventDTO.responseTime ,
                    responseEventDTO.cacheHit, responseEventDTO.contentLength , responseEventDTO.protocol,
                    responseEventDTO.statusCode , responseEventDTO.destination, latencyEventDTO.responseTime ,
                    latencyEventDTO.serviceTime , latencyEventDTO.backendTime , latencyEventDTO.backendLatency ,
                    latencyEventDTO.securityLatency , latencyEventDTO.throttlingLatency ,
                    latencyEventDTO.request_mediationLatency , latencyEventDTO.response_mediationLatency ,
                    latencyEventDTO.otherLatency ];

    event.metaData = metaData;
    event.correlationData = correlationData;
    event.payloadData = payloadData;
    return event;
}

function getThrottleEventPayload (dto:ThrottleEventHolderDTO throttleEventHolderDTO) (json) {
    json event = {};
    event.streamName= throttleEventHolderDTO.streamName;
    event.streamVersion= throttleEventHolderDTO.version;

    dto:ThrottleEventDTO throttleEventDTO = throttleEventHolderDTO.throttleEventDTO;

    json metaData = [];
    json correlationData = [];
    json payloadData = [throttleEventDTO.messageID, throttleEventDTO.appKey, throttleEventDTO.applicationTier,
                    throttleEventDTO.apiKey, throttleEventDTO.apiTier, throttleEventDTO.subscriptionKey,
                    throttleEventDTO.subscriptionTier, throttleEventDTO.resourceLevelThrottleKey,
                    throttleEventDTO.resourceTier, throttleEventDTO.userId, throttleEventDTO.apiContext,
                    throttleEventDTO.apiVersion, throttleEventDTO.applicationId, throttleEventDTO.apiName,
                        throttleEventDTO.properties];

    event.metaData = metaData;
    event.correlationData = correlationData;
    event.payloadData = payloadData;

    return event;
}

function getThrottleAnalyticsEventPayload (dto:ThrottleEventAnalyticsHolderDTO throttleEventAnalyticsHolderDTO) (json) {
    json event = {};
    event.streamName= throttleEventAnalyticsHolderDTO.streamName;
    event.streamVersion= throttleEventAnalyticsHolderDTO.version;

    dto:ThrottleAnalyticsEventDTO throttleStatDTO = throttleEventAnalyticsHolderDTO.throttleAnalyticsEventDTO;

    json metaData = [];
    json correlationData = [];
    json payloadData = [throttleStatDTO.api, throttleStatDTO.context, throttleStatDTO.version, throttleStatDTO.publisher,
                        throttleStatDTO.subscriptionPolicy, throttleStatDTO.uriTemplate, throttleStatDTO.httpMethod,
                        throttleStatDTO.consumerKey, throttleStatDTO.applicationName, throttleStatDTO.applicationId,
                        throttleStatDTO.applicationOwner, throttleStatDTO.userId, throttleStatDTO.subscriber,
                        throttleStatDTO.throttleCount , throttleStatDTO.throttleTime , throttleStatDTO.gatewayDomain,
                        throttleStatDTO.gatewayIp, throttleStatDTO.throttledReason, throttleStatDTO.throttledPolicy,
                        throttleStatDTO.hostName];

    event.metaData = metaData;
    event.correlationData = correlationData;
    event.payloadData = payloadData;

    return event;
}

function getFaultEventPayload (dto:FaultEventHolderDTO faultEventHolderDTO) (json) {
    json event = {};
    event.streamName= faultEventHolderDTO.streamName;
    event.streamVersion= faultEventHolderDTO.version;

    dto:RequestEventDTO requestEventDTO = faultEventHolderDTO.requestEventDTO;
    dto:FaultEventDTO faultEventDTO = faultEventHolderDTO.faultEventDTO;

    json metaData = [];
    json correlationData = [];
    json payloadData = [requestEventDTO.api, requestEventDTO.context, requestEventDTO.version, requestEventDTO.publisher,
                        requestEventDTO.subscriptionPolicy, requestEventDTO.uriTemplate, requestEventDTO.httpMethod,
                        requestEventDTO.consumerKey, requestEventDTO.applicationName, requestEventDTO.applicationId,
                        requestEventDTO.applicationOwner, requestEventDTO.userId, requestEventDTO.subscriber,
                        requestEventDTO.requestCount , requestEventDTO.requestTime , requestEventDTO.gatewayDomain,
                        requestEventDTO.gatewayIp, requestEventDTO. isThrottled, requestEventDTO.throttledReason,
                        requestEventDTO.throttledPolicy, requestEventDTO.clientIp, requestEventDTO.userAgent,
                        requestEventDTO.hostName, faultEventDTO.faultCount, faultEventDTO.faultTime,
                        faultEventDTO.faultReason];

    event.metaData = metaData;
    event.correlationData = correlationData;
    event.payloadData = payloadData;

    return event;
}

function validate (dto:EventHolderDTO eventHolderDTO) {

}