package org.wso2.carbon.apimgt.gateway.analytics;
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.gateway.GatewayConstants;
import org.wso2.carbon.apimgt.gateway.analytics.dto.AnalyticsEventStreamDTO;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * This class used to implement some utility function which is used for analytics data processing
 */
public class AnalyticsUtil {
    private static Logger log = LoggerFactory.getLogger(AnalyticsUtil.class);

    /**
     * Setting request related properties
     *
     * @param carbonMessage current properties of the message context
     * @return AnalyticsEventStreamDTO
     */
    public static AnalyticsEventStreamDTO processRequestData(CarbonMessage carbonMessage) {
        log.debug("Processing Request Data");
        AnalyticsEventStreamDTO dto = (AnalyticsEventStreamDTO) carbonMessage
                .getProperty(GatewayConstants.EVENT_DTO_PROPERTY_NAME);
        if (dto == null) {
            dto = initializeStreamDTO(carbonMessage);
        }
        return dto;
    }

    /**
     * Setting response related properties
     *
     * @param carbonMessage current properties of the message context
     * @return AnalyticsEventStreamDTO
     */
    public static AnalyticsEventStreamDTO processResponseData(CarbonMessage carbonMessage) {
        log.debug("Processing Response Data");
        AnalyticsEventStreamDTO dto = (AnalyticsEventStreamDTO) carbonMessage
                .getProperty(GatewayConstants.EVENT_DTO_PROPERTY_NAME);
        if (dto == null) {
            dto = initializeStreamDTO(carbonMessage);
        }
        return dto;
    }

    /**
     * Setting faulty related properties
     *
     * @param carbonMessage current properties of the message context
     * @return AnalyticsEventStreamDTO
     */
    public static AnalyticsEventStreamDTO processFaultData(CarbonMessage carbonMessage) {
        log.debug("Processing Fault Data");
        AnalyticsEventStreamDTO dto = (AnalyticsEventStreamDTO) carbonMessage
                .getProperty(GatewayConstants.EVENT_DTO_PROPERTY_NAME);
        if (dto == null) {
            dto = initializeStreamDTO(carbonMessage);
        }
        return dto;
    }

    /**
     * Setting throttle related properties
     *
     * @param carbonMessage current properties of the message context
     * @return AnalyticsEventStreamDTO
     */
    public static AnalyticsEventStreamDTO processThrottleData(CarbonMessage carbonMessage) {
        log.debug("Processing Throttle Data");
        AnalyticsEventStreamDTO dto = (AnalyticsEventStreamDTO) carbonMessage
                .getProperty(GatewayConstants.EVENT_DTO_PROPERTY_NAME);
        if (dto == null) {
            dto = initializeStreamDTO(carbonMessage);
        }
        return dto;
    }

    /**
     * Initialize DTO with common attributes of the API invocation
     *
     * @param carbonMessage current properties of the message context
     * @return AnalyticsEventStreamDTO
     */
    public static AnalyticsEventStreamDTO initializeStreamDTO(CarbonMessage carbonMessage) {
        log.debug("Initializing AnalyticsEventStreamDTO");
        AnalyticsEventStreamDTO dto = new AnalyticsEventStreamDTO();
        dto.setApiName("sampleAPI");
        dto.setVersion("1.0.0");
        dto.setCreator("admin");
        return dto;
    }

    /**
     * generate the event stream as a object array
     *
     * @param dto AnalyticsEventStreamDTO to be converted
     * @return list of object
     */
    public static Object[] generateStream(AnalyticsEventStreamDTO dto) {
        return new Object[] { dto.getApiName(), dto.getVersion(), dto.getCreator() };
    }
}
