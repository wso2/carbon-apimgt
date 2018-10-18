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

package org.wso2.carbon.apimgt.micro.gateway.usage.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.micro.gateway.usage.publisher.util.UsagePublisherException;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.apimgt.usage.publisher.dto.AlertTypeDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.DataBridgeAlertTypesPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.DataBridgeExecutionTimePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.DataBridgeFaultPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.DataBridgeRequestPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.DataBridgeRequestStreamPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.DataBridgeResponsePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.DataBridgeThrottlePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.ExecutionTimePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.FaultPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.RequestPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.RequestStreamDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.ResponsePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.ThrottlePublisherDTO;

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
    public void publishEvent(RequestPublisherDTO requestPublisherDTO) {
        DataBridgeRequestPublisherDTO dataBridgeRequestPublisherDTO =
                new DataBridgeRequestPublisherDTO(requestPublisherDTO);

        try {
            String streamID = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getRequestStreamName() + ":"
                    + DataPublisherUtil.getApiManagerAnalyticsConfiguration().getRequestStreamVersion();
            //Publish Request Data
            dataPublisher.tryPublish(streamID, System.currentTimeMillis(),
                    (Object[]) dataBridgeRequestPublisherDTO.createMetaData(), null,
                    (Object[]) dataBridgeRequestPublisherDTO.createPayload());
        } catch (Exception e) {
            log.error("Error while publishing Request event", e);
        }
    }

    @Override
    public void publishEvent(ResponsePublisherDTO responsePublisherDTO) {
        DataBridgeResponsePublisherDTO dataBridgeResponsePublisherDTO =
                new DataBridgeResponsePublisherDTO(responsePublisherDTO);
        try {
            String streamID = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getResponseStreamName() + ":"
                    + DataPublisherUtil.getApiManagerAnalyticsConfiguration().getResponseStreamVersion();
            //Publish Response Data
            dataPublisher.tryPublish(streamID, System.currentTimeMillis(),
                    (Object[]) dataBridgeResponsePublisherDTO.createMetaData(), null,
                    (Object[]) dataBridgeResponsePublisherDTO.createPayload());

        } catch (Exception e) {
            log.error("Error while publishing Response event", e);
        }
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
    public void publishEvent(ExecutionTimePublisherDTO executionTimePublisherDTO) {
        DataBridgeExecutionTimePublisherDTO dataBridgeExecutionTimePublisherDTO = new
                DataBridgeExecutionTimePublisherDTO(executionTimePublisherDTO);
        try {
            String streamID = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getExecutionTimeStreamName()
                    + ":" + DataPublisherUtil.getApiManagerAnalyticsConfiguration().getExecutionTimeStreamVersion();

            dataPublisher.tryPublish(streamID, System.currentTimeMillis(),
                    (Object[]) dataBridgeExecutionTimePublisherDTO.createMetaData(), null,
                    (Object[]) dataBridgeExecutionTimePublisherDTO.createPayload());
        } catch (Exception e) {
            log.error("Error while publishing Execution time events", e);
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
    public void publishEvent(RequestStreamDTO requestStreamDTO) {
        DataBridgeRequestStreamPublisherDTO dataBridgeRequestStreamPublisherDTO =
                new DataBridgeRequestStreamPublisherDTO(requestStreamDTO);
        try {
            String streamID = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getResponseStreamName() + ":"
                    + DataPublisherUtil.getApiManagerAnalyticsConfiguration().getResponseStreamVersion();
            //Publish Request Data
            dataPublisher.tryPublish(streamID, System.currentTimeMillis(),
                    (Object[]) null, null,
                    (Object[]) dataBridgeRequestStreamPublisherDTO.createPayload());

        } catch (Exception e) {
            log.error("Error while publishing Request event", e);
        }
        
    }
}
