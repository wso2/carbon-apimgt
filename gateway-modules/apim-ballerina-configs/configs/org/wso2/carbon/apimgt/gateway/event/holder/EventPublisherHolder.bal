package org.wso2.carbon.apimgt.gateway.event.holder;

import org.wso2.carbon.apimgt.ballerina.publisher;
import ballerina.lang.system;
import org.wso2.carbon.apimgt.gateway.dto;
import org.wso2.carbon.apimgt.gateway.holders;

publisher:EventConnector analyticsPublisher;
publisher:EventConnector throttlePublisher;

boolean initAnalytics = false;
boolean initThrottle = false;

function getAnalyticsPublisher () (publisher:EventConnector) {

    if (initAnalytics != false) {
        return analyticsPublisher;
    } else {
        system:println("Initiating new event publisher for analytics");
        map config = getAnalyticsConfigMap();
        publisher:EventConnector pub = getPublisherInstance(config);
        initAnalytics = true;
        analyticsPublisher = pub;
        return pub;
    }
}

function getThrottlingPublisher () (publisher:EventConnector) {
    if (initThrottle != false) {
        return throttlePublisher;
    } else {
        system:println("Initiating new event publisher for throttling");
        map config = getThrottleConfigMap();
        publisher:EventConnector pub = getPublisherInstance(config);
        initThrottle = true;
        throttlePublisher = pub;
        return pub;
    }
}

function getPublisherInstance (map propertiesMap) (publisher:EventConnector) {
    publisher:EventConnector das = create publisher:EventConnector(propertiesMap);
    return das;
}

function getAnalyticsConfigMap () (map) {
    dto:AnalyticsInfoDTO dto = holders:getAnalyticsConf ();
    map propertiesMap = {
                        "type": dto.type,
                        "receiverURLSet": dto.serverURL,
                        "authURLSet": dto.authServerURL,
                        "username": dto.credentials.username,
                        "password": dto.credentials.password,
                        "configPath":"bre/conf/data.agent.config.yaml"
                        };
    return propertiesMap;
}

function getThrottleConfigMap () (map) {
    dto:ThrottlingInfoDTO dto = holders:getThrottleConf ();
    map propertiesMap = {
                        "type": dto.type,
                        "receiverURLSet": dto.serverURL,
                        "authURLSet": dto.authServerURL,
                        "username": dto.credentials.username,
                        "password": dto.credentials.password,
                        "configPath":"bre/conf/data.agent.config.yaml"
                        };
    return propertiesMap;
}

function getAnalyticsConf () (dto:AnalyticsInfoDTO) {
    return holders:getAnalyticsConf ();
}

function getThrottleConf () (dto:ThrottlingInfoDTO) {
    return holders:getThrottleConf ();
}