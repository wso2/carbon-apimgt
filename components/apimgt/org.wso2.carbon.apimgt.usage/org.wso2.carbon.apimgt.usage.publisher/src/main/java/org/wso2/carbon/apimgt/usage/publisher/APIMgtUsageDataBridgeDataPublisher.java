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
import org.wso2.carbon.apimgt.usage.publisher.internal.DataPublisherAlreadyExistsException;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

public class APIMgtUsageDataBridgeDataPublisher implements APIMgtUsageDataPublisher{

    private static final Log log   = LogFactory.getLog(APIMgtUsageDataBridgeDataPublisher.class);

    private DataPublisher dataPublisher;

    public void init(){
        try {
            if(log.isDebugEnabled()){
                log.debug("Initializing APIMgtUsageDataBridgeDataPublisher");
            }

            this.dataPublisher = getDataPublisher();

        }catch (Exception e){
            log.error("Error initializing APIMgtUsageDataBridgeDataPublisher", e);
        }
    }

    public void publishEvent(RequestPublisherDTO requestPublisherDTO) {
        DataBridgeRequestPublisherDTO dataBridgeRequestPublisherDTO = new DataBridgeRequestPublisherDTO(requestPublisherDTO);
        try {

            String streamID= DataPublisherUtil.getApiManagerAnalyticsConfiguration().getRequestStreamName()+":"
                             +DataPublisherUtil.getApiManagerAnalyticsConfiguration().getRequestStreamVersion();
            //Publish Request Data
            dataPublisher.tryPublish( streamID ,
                                  System.currentTimeMillis(), new Object[]{"external"}, null,
                                  (Object[]) dataBridgeRequestPublisherDTO.createPayload());
        } catch(Exception e){
            log.error("Error while publishing Request event", e);
        }

    }

    public void publishEvent(ResponsePublisherDTO responsePublisherDTO) {
        DataBridgeResponsePublisherDTO dataBridgeResponsePublisherDTO = new DataBridgeResponsePublisherDTO(responsePublisherDTO);
        try {
            String streamID = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getResponseStreamName() + ":"
                              + DataPublisherUtil.getApiManagerAnalyticsConfiguration().getResponseStreamVersion();
            dataBridgeResponsePublisherDTO.createPayload();
            //Publish Response Data
            dataPublisher.tryPublish(streamID,
                                  System.currentTimeMillis(), new Object[]{"external"}, null,
                                  (Object[]) dataBridgeResponsePublisherDTO.createPayload());

        } catch (Exception e) {
            log.error("Error while publishing Response event", e);
        }
    }

    public void publishEvent(FaultPublisherDTO faultPublisherDTO) {
        DataBridgeFaultPublisherDTO dataBridgeFaultPublisherDTO = new DataBridgeFaultPublisherDTO(faultPublisherDTO);
        try {

            String streamID = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getFaultStreamName() + ":"
                              + DataPublisherUtil.getApiManagerAnalyticsConfiguration().getFaultStreamVersion();
            //Publish Fault Data
            dataPublisher.tryPublish(streamID,
                                  System.currentTimeMillis(), new Object[]{"external"}, null,
                                  (Object[]) dataBridgeFaultPublisherDTO.createPayload());

        } catch (Exception e) {
            log.error("Error while publishing Fault event", e);
        }
    }

    public void publishEvent(ThrottlePublisherDTO throttPublisherDTO) {
        DataBridgeThrottlePublisherDTO dataBridgeThrottlePublisherDTO = new
                DataBridgeThrottlePublisherDTO(throttPublisherDTO);

        try {
            String streamID = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getThrottleStreamName() + ":" +
                              DataPublisherUtil.getApiManagerAnalyticsConfiguration().getThrottleStreamVersion();
            //Publish Throttle data
            dataPublisher.tryPublish(streamID,
                                  System.currentTimeMillis(), new Object[]{"external"}, null,
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
            String streamID = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getExecutionTimeStreamName() + ":" +
                    DataPublisherUtil.getApiManagerAnalyticsConfiguration().getExecutionTimeStreamVersion();

            dataPublisher.tryPublish(streamID,System.currentTimeMillis(), new Object[]{"external"}, null,
                    (Object[]) dataBridgeExecutionTimePublisherDTO.createPayload());
        } catch (Exception e) {
            log.error("Error while publishing Execution time events", e);
        }
    }
    private static DataPublisher getDataPublisher() {

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        //Get DataPublisher which has been registered for the tenant.
        DataPublisher dataPublisher = UsageComponent.getDataPublisher(tenantDomain);

        //If a DataPublisher had not been registered for the tenant.
        if (dataPublisher == null
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
                    dataPublisher = UsageComponent.getDataPublisher(tenantDomain);
                    if (dataPublisher == null) {
                        dataPublisher = new DataPublisher(null, serverURL, serverAuthURL, serverUser, serverPassword);
                        //Add created DataPublisher.
                        UsageComponent.addDataPublisher(tenantDomain, dataPublisher);
                    }
                }
            } catch (DataPublisherAlreadyExistsException e) {
                log.warn("Attempting to register a data publisher for the tenant " + tenantDomain +
                         " when one already exists. Returning existing data publisher");
                return UsageComponent.getDataPublisher(tenantDomain);
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

        return dataPublisher;
    }

    /**
     * This method will publish event for alert types configurations.
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

            dataPublisher.tryPublish(streamID,System.currentTimeMillis(), null, null,
                    (Object[]) dataBridgeAlertTypesPublisherDTO.createPayload());
        } catch (Exception e) {
            log.error("Error while publishing alert types events.", e);
            throw new APIManagementException("Error while publishing alert types events");
        }
    }

}
