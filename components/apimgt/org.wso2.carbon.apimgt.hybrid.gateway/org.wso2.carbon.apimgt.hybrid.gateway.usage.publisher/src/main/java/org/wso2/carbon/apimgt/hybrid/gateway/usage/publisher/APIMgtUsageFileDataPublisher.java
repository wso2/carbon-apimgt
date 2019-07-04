/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.UsagePublisherException;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.apimgt.usage.publisher.dto.*;

/**
 * Implementation of APIMgtUsageDataPublisher used for writing events into a File
 */
public class APIMgtUsageFileDataPublisher implements APIMgtUsageDataPublisher {

    private static final Log log = LogFactory.getLog(APIMgtUsageFileDataPublisher.class);

    private static volatile FileDataPublisher dataPublisher = null;

    @Override
    public void init() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing APIMgtUsageFileDataPublisher");
            }
            getDataPublisher();
        } catch (Exception e) {
            log.error("Error occurred while initializing APIMgtUsageFileDataPublisher", e);
        }
    }

    private FileDataPublisher getDataPublisher() throws UsagePublisherException {
        if (dataPublisher == null) {
            //Create new DataPublisher for the tenant.
            synchronized (APIMgtUsageFileDataPublisher.class) {
                if (dataPublisher == null) {
                    dataPublisher = new FileDataPublisher();
                }
            }
        }
        return dataPublisher;
    }

    @Override
    public void publishEvent(FaultPublisherDTO faultPublisherDTO) {
        DataBridgeFaultPublisherDTO dataBridgeFaultPublisherDTO = new DataBridgeFaultPublisherDTO(faultPublisherDTO);
        try {

            String streamID = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getFaultStreamName() + ":"
                    + DataPublisherUtil.getApiManagerAnalyticsConfiguration().getFaultStreamVersion();
            //Publish Fault Data
            dataPublisher.tryPublish(streamID, System.currentTimeMillis(),
                    (Object[]) dataBridgeFaultPublisherDTO.createMetaData(), null,
                    (Object[]) dataBridgeFaultPublisherDTO.createPayload());

        } catch (Exception e) {
            log.error("Error while publishing Fault event", e);
        }
    }

    @Override
    public void publishEvent(ThrottlePublisherDTO throttlePublisherDTO) {
        DataBridgeThrottlePublisherDTO dataBridgeThrottlePublisherDTO = new
                DataBridgeThrottlePublisherDTO(throttlePublisherDTO);

        try {
            String streamID = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getThrottleStreamName() + ":" +
                    DataPublisherUtil.getApiManagerAnalyticsConfiguration().getThrottleStreamVersion();
            //Publish Throttle data
            dataPublisher.tryPublish(streamID, System.currentTimeMillis(),
                    (Object[]) dataBridgeThrottlePublisherDTO.createMetaData(), null,
                    (Object[]) dataBridgeThrottlePublisherDTO.createPayload());

        } catch (Exception e) {
            log.error("Error while publishing Throttle exceed event", e);
        }
    }

    @Override
    public void publishEvent(AlertTypeDTO alertTypeDTO) throws APIManagementException {
        DataBridgeAlertTypesPublisherDTO dataBridgeAlertTypesPublisherDTO = new
                DataBridgeAlertTypesPublisherDTO(alertTypeDTO);
        try {
            String streamID = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getAlertTypeStreamName() + ":" +
                    DataPublisherUtil.getApiManagerAnalyticsConfiguration().getAlertTypeStreamVersion();

            dataPublisher.tryPublish(streamID, System.currentTimeMillis(), null, null,
                    (Object[]) dataBridgeAlertTypesPublisherDTO.createPayload());
        } catch (Exception e) {
            log.error("Error while publishing alert types events.", e);
            throw new APIManagementException("Error while publishing alert types events");
        }
    }

    @Override
    public void publishEvent(RequestResponseStreamDTO requestStream) {
        DataBridgeRequestResponseStreamPublisherDTO dataBridgeRequestStreamPublisherDTO = new DataBridgeRequestResponseStreamPublisherDTO(requestStream);
        try {
            String streamID = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getRequestStreamName() + ":"
                    + DataPublisherUtil.getApiManagerAnalyticsConfiguration().getRequestStreamVersion();
            //Publish Request Data
            dataPublisher.tryPublish(streamID, System.currentTimeMillis(),
                    (Object[]) dataBridgeRequestStreamPublisherDTO.createMetaData(), null,
                    (Object[]) dataBridgeRequestStreamPublisherDTO.createPayload());
        } catch (Exception e) {
            log.error("Error while publishing Request event", e);
        }
    }

    @Override
    public void publishEvent(BotDataDTO botDataDTO) {

    }
}
