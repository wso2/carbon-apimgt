package org.wso2.carbon.apimgt.gateway.event.holder;

import org.wso2.carbon.apimgt.ballerina.publisher;
import ballerina.lang.system;
import org.wso2.carbon.apimgt.gateway.dto;

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
        system:println("Initiating new event publisher for throttleing");
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
    //dto:AnalyticsInfoDTO dto = getAnalyticsConf ();
    map propertiesMap = {
                        "type":"binary",
                        "receiverURLSet":"tcp://localhost:9612",
                        "authURLSet":"ssl://localhost:9712",
                        "username":"admin",
                        "password":"admin",
                        "configPath":"bre/conf/data.agent.config.yaml"
                        };
    return propertiesMap;
}

function getThrottleConfigMap () (map) {
    //dto:ThrottlingInfoDTO dto = getThrottleConf ();
    map propertiesMap = {
                        "type":"binary",
                        "receiverURLSet":"tcp://localhost:9612",
                        "authURLSet":"ssl://localhost:9712",
                        "username":"admin",
                        "password":"admin",
                        "configPath":"bre/conf/data.agent.config.yaml"
                        };
    return propertiesMap;
}

function getAnalyticsConf () (dto:AnalyticsInfoDTO) {
    //todo: set the config from the core config
    dto:AnalyticsInfoDTO dto = {};
    dto.enabled = true;
    return dto;
}

function getThrottleConf () (dto:ThrottlingInfoDTO) {
    //todo: set the config from the core config
    dto:ThrottlingInfoDTO dto = {};
    return dto;
}