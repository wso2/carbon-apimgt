/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.usage.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.usage.publisher.dto.*;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.util.ArrayList;
import java.util.List;

public class APIMgtUsageDataBridgeDataPublisher implements APIMgtUsageDataPublisher {

    private static final Log log = LogFactory.getLog(APIMgtUsageDataBridgeDataPublisher.class);

    protected DataPublisher dataPublisher;
    private static DataPublisher dataPublisherStatics;

    public void init() {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing APIMgtUsageDataBridgeDataPublisher");
            }

            this.dataPublisher = getDataPublisher();

        } catch (Exception e) {
            log.error("Error initializing APIMgtUsageDataBridgeDataPublisher", e);
        }
    }

    public void publishEvent(FaultPublisherDTO faultPublisherDTO) {
        DataBridgeFaultPublisherDTO dataBridgeFaultPublisherDTO = new DataBridgeFaultPublisherDTO(faultPublisherDTO);
        List<String> missingMandatoryValues = dataBridgeFaultPublisherDTO.getMissingMandatoryValues();
        if (missingMandatoryValues.isEmpty()) {
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

        } else {
            log.error("Faulty invocation event dropped due to missing mandatory data: "
                    + missingMandatoryValues.toString() + " in event: " + dataBridgeFaultPublisherDTO.toString());
        }
    }

    public void publishEvent(ThrottlePublisherDTO throttPublisherDTO) {
        DataBridgeThrottlePublisherDTO dataBridgeThrottlePublisherDTO = new
                DataBridgeThrottlePublisherDTO(throttPublisherDTO);
        List<String> missingMandatoryValues = dataBridgeThrottlePublisherDTO.getMissingMandatoryValues();
        if (missingMandatoryValues.isEmpty()) {
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
        } else {
            log.error("Throttling event dropped due to missing mandatory data: "
                    + missingMandatoryValues.toString() + " in event: " + dataBridgeThrottlePublisherDTO.toString());
        }
    }

    private static DataPublisher getDataPublisher() {

        //If a DataPublisher had not been registered for the tenant.
        if (dataPublisherStatics == null
                && DataPublisherUtil.getApiManagerAnalyticsConfiguration().getDasReceiverUrlGroups() != null) {

            String serverUser = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getDasReceiverServerUser();
            String serverPassword = DataPublisherUtil.getApiManagerAnalyticsConfiguration()
                    .getDasReceiverServerPassword();
            String serverURL = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getDasReceiverUrlGroups();
            String serverAuthURL = DataPublisherUtil.getApiManagerAnalyticsConfiguration()
                    .getDasReceiverAuthUrlGroups();

            try {
                //Create new DataPublisher for the tenant.
                synchronized (APIMgtUsageDataBridgeDataPublisher.class) {

                    if (dataPublisherStatics == null) {
                        dataPublisherStatics = new DataPublisher(null, serverURL, serverAuthURL, serverUser,
                                serverPassword);
                    }
                }
            } catch (DataEndpointConfigurationException e) {
                log.error("Error while creating data publisher", e);
            } catch (DataEndpointException e) {
                log.error("Error while creating data publisher", e);
            } catch (DataEndpointAgentConfigurationException e) {
                log.error("Error while creating data publisher", e);
            } catch (TransportException e) {
                log.error("Error while creating data publisher", e);
            } catch (DataEndpointAuthenticationException e) {
                log.error("Error while creating data publisher", e);
            }
        }

        return dataPublisherStatics;
    }

    /**
     * This method will publish event for alert types configurations.
     *
     * @param alertTypeDTO DTO object.
     * @throws APIManagementException
     */
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
        List<String> missingMandatoryValues = dataBridgeRequestStreamPublisherDTO.getMissingMandatoryValues();
        if (missingMandatoryValues.isEmpty()) {
            try {
                String streamID = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getRequestStreamName() + ":"
                        + DataPublisherUtil.getApiManagerAnalyticsConfiguration().getRequestStreamVersion();
                //Publish Request Data
                dataPublisher.tryPublish(streamID, System.currentTimeMillis(),
                        dataBridgeRequestStreamPublisherDTO.createMetaData(), null,
                        (Object[]) dataBridgeRequestStreamPublisherDTO.createPayload());
            } catch (Exception e) {
                log.error("Error while publishing Request event", e);
            }
        } else {
            log.error("RequestResponse event dropped due to unavailability of mandatory data: "
                    + missingMandatoryValues.toString() + " in event: " + dataBridgeRequestStreamPublisherDTO.toString());
        }
    }

    @Override
    public void publishEvent(BotDataDTO botDataDTO) {
        DataBridgeBotDataDTO dataBridgeBotDataDTO = new DataBridgeBotDataDTO(botDataDTO);
        List<String> missingMandatoryValues = dataBridgeBotDataDTO.getMissingMandatoryValues();
        if (missingMandatoryValues.isEmpty()) {
            try {
                String streamID = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getBotDataStreamName() + ":"
                        + DataPublisherUtil.getApiManagerAnalyticsConfiguration().getBotStreamVersion();
                dataPublisher.tryPublish(streamID, null, (Object[]) dataBridgeBotDataDTO.createPayload(),
                        null);
            } catch (Exception e) {
                log.error("Error while publishing Request event", e);
            }
        } else {
            log.error("RequestResponse event dropped due to unavailability of mandatory data: "
                    + missingMandatoryValues.toString() + " in event: " + dataBridgeBotDataDTO.toString());
        }
    }
}
