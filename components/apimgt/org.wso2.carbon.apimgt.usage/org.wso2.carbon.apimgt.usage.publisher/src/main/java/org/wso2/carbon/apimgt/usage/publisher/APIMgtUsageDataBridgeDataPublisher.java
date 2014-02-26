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
import org.wso2.carbon.apimgt.usage.publisher.dto.DataBridgeFaultPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.DataBridgeRequestPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.DataBridgeResponsePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.FaultPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.RequestPublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.dto.ResponsePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.internal.DataPublisherAlreadyExistsException;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.apimgt.usage.publisher.service.APIMGTConfigReaderService;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.commons.exception.*;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class APIMgtUsageDataBridgeDataPublisher implements APIMgtUsageDataPublisher{

    private static final Log log   = LogFactory.getLog(APIMgtUsageDataBridgeDataPublisher.class);

    private LoadBalancingDataPublisher dataPublisher;

    public void init(){
        try {
            if(log.isDebugEnabled()){
                log.debug("Initializing APIMgtUsageDataBridgeDataPublisher");
            }

            this.dataPublisher = getDataPublisher();

            //If Request Stream Definition does not exist.
            if(!dataPublisher.isStreamDefinitionAdded(APIMgtUsagePublisherConstants.API_MANAGER_REQUEST_STREAM_NAME,
                    APIMgtUsagePublisherConstants.API_MANAGER_REQUEST_STREAM_VERSION)){

                //Get Request Stream Definition
                String requestStreamDefinition =  DataBridgeRequestPublisherDTO.getStreamDefinition();

                //Add Request Stream Definition.
                dataPublisher.addStreamDefinition(requestStreamDefinition,
                        APIMgtUsagePublisherConstants.API_MANAGER_REQUEST_STREAM_NAME,
                        APIMgtUsagePublisherConstants.API_MANAGER_REQUEST_STREAM_VERSION);
            }

            //If Response Stream Definition does not exist.
            if(!dataPublisher.isStreamDefinitionAdded(APIMgtUsagePublisherConstants.API_MANAGER_RESPONSE_STREAM_NAME,
                     APIMgtUsagePublisherConstants.API_MANAGER_RESPONSE_STREAM_VERSION)){

                //Get Response Stream Definition.
                String responseStreamDefinition = DataBridgeResponsePublisherDTO.getStreamDefinition();

                //Add Response Stream Definition.
                dataPublisher.addStreamDefinition(responseStreamDefinition,
                        APIMgtUsagePublisherConstants.API_MANAGER_RESPONSE_STREAM_NAME,
                        APIMgtUsagePublisherConstants.API_MANAGER_RESPONSE_STREAM_VERSION);

            }

            //If Fault Stream Definition does not exist.
            if(!dataPublisher.isStreamDefinitionAdded(APIMgtUsagePublisherConstants.API_MANAGER_FAULT_STREAM_NAME,
                                                      APIMgtUsagePublisherConstants.API_MANAGER_FAULT_STREAM_VERSION)){

                //Get Fault Stream Definition
                String faultStreamDefinition = DataBridgeFaultPublisherDTO.getStreamDefinition();

                //Add Fault Stream Definition;
                dataPublisher.addStreamDefinition(faultStreamDefinition,
                                                  APIMgtUsagePublisherConstants.API_MANAGER_FAULT_STREAM_NAME,
                                                  APIMgtUsagePublisherConstants.API_MANAGER_FAULT_STREAM_VERSION);
            }
        }catch (Exception e){
            log.error("Error initializing APIMgtUsageDataBridgeDataPublisher", e);
        }
    }

    public void publishEvent(RequestPublisherDTO requestPublisherDTO) {
        DataBridgeRequestPublisherDTO dataBridgeRequestPublisherDTO = new DataBridgeRequestPublisherDTO(requestPublisherDTO);
        try {
            //Publish Request Data
            dataPublisher.publish(APIMgtUsagePublisherConstants.API_MANAGER_REQUEST_STREAM_NAME,
                                  APIMgtUsagePublisherConstants.API_MANAGER_REQUEST_STREAM_VERSION ,
                                  System.currentTimeMillis(), new Object[]{"external"}, null,
                                  (Object[]) dataBridgeRequestPublisherDTO.createPayload());
        } catch(AgentException e){
            log.error("Error while publishing Request event", e);
        }
    }

    public void publishEvent(ResponsePublisherDTO responsePublisherDTO) {
        DataBridgeResponsePublisherDTO dataBridgeResponsePublisherDTO = new DataBridgeResponsePublisherDTO(responsePublisherDTO);
        try {
            //Publish Response Data
            dataPublisher.publish(APIMgtUsagePublisherConstants.API_MANAGER_RESPONSE_STREAM_NAME,
                    APIMgtUsagePublisherConstants.API_MANAGER_RESPONSE_STREAM_VERSION ,
                    System.currentTimeMillis(), new Object[]{"external"}, null,
                    (Object[]) dataBridgeResponsePublisherDTO.createPayload());

        } catch (AgentException e) {
            log.error("Error while publishing Response event", e);
        }
    }

    public void publishEvent(FaultPublisherDTO faultPublisherDTO) {
        DataBridgeFaultPublisherDTO dataBridgeFaultPublisherDTO = new DataBridgeFaultPublisherDTO(faultPublisherDTO);
        try {
            //Publish Fault Data
            dataPublisher.publish(APIMgtUsagePublisherConstants.API_MANAGER_FAULT_STREAM_NAME,
                    APIMgtUsagePublisherConstants.API_MANAGER_FAULT_STREAM_VERSION ,
                    System.currentTimeMillis(), new Object[]{"external"}, null,
                    (Object[]) dataBridgeFaultPublisherDTO.createPayload());

        } catch (AgentException e) {
            log.error("Error while publishing Fault event", e);
        }
    }

    private static LoadBalancingDataPublisher getDataPublisher()
            throws AgentException, MalformedURLException, AuthenticationException,
                   TransportException {

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        //Get LoadBalancingDataPublisher which has been registered for the tenant.
        LoadBalancingDataPublisher loadBalancingDataPublisher = UsageComponent.getDataPublisher(tenantDomain);

        //If a LoadBalancingDataPublisher had not been registered for the tenant.
        if(loadBalancingDataPublisher == null){
            APIMGTConfigReaderService apimgtConfigReaderService = UsageComponent.getApiMgtConfigReaderService();

            List<String> receiverGroups = org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil.
                    getReceiverGroups(apimgtConfigReaderService.getBamServerURL());

            String serverUser = apimgtConfigReaderService.getBamServerUser();
            String serverPassword = apimgtConfigReaderService.getBamServerPassword();
            List<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();

            for(String receiverGroupString : receiverGroups){
                String[] serverURLs = receiverGroupString.split(",");
                List<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();

                for(int i=0; i<serverURLs.length; i++){
                    String serverURL = serverURLs[i];
                    DataPublisherHolder dataPublisherHolder =
                            new DataPublisherHolder(null, serverURL, serverUser, serverPassword);
                    dataPublisherHolders.add(dataPublisherHolder);
                }

                ReceiverGroup receiverGroup = new ReceiverGroup((ArrayList)dataPublisherHolders);
                allReceiverGroups.add(receiverGroup);
            }

            //Create new LoadBalancingDataPublisher for the tenant.
            loadBalancingDataPublisher = new LoadBalancingDataPublisher((ArrayList)allReceiverGroups);
            try {
                //Add created LoadBalancingDataPublisher.
                UsageComponent.addDataPublisher(tenantDomain, loadBalancingDataPublisher);
            } catch (DataPublisherAlreadyExistsException e) {
                log.warn("Attempting to register a data publisher for the tenant " + tenantDomain +
                        " when one already exists. Returning existing data publisher");
                return UsageComponent.getDataPublisher(tenantDomain);
            }
        }

        return loadBalancingDataPublisher;
    }
}
