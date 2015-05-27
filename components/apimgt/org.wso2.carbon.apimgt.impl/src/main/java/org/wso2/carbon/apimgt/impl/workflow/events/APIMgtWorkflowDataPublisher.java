/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.workflow.events;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
* This class will act as data-publisher for workflow events.Reason for not re-using the usage
* publisher bundle is there's a maven cyclic dependency appears if we import it from impl bundle.
*/

public class APIMgtWorkflowDataPublisher {

    private static final Log log = LogFactory.getLog(APIMgtWorkflowDataPublisher.class);
    private LoadBalancingDataPublisher dataPublisher;
    private static Map<String, LoadBalancingDataPublisher> dataPublisherMap;
    static APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
            getAPIManagerConfigurationService().
            getAPIManagerConfiguration();
    static APIManagerAnalyticsConfiguration analyticsConfig = ServiceReferenceHolder.getInstance().
            getAPIManagerConfigurationService().
            getAPIAnalyticsConfiguration();
    boolean enabled = analyticsConfig.isAnalyticsEnabled();
    private static String wfStreamName;
    private static String wfStreamVersion;

    public APIMgtWorkflowDataPublisher() {
        try {
            if (!enabled) {
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Initializing APIMgtUsageDataBridgeDataPublisher");
            }
            dataPublisherMap = new ConcurrentHashMap<String, LoadBalancingDataPublisher>();
            this.dataPublisher = getDataPublisher();
            wfStreamName =
                    config.getFirstProperty(APIConstants.API_WF_STREAM_NAME);
            wfStreamVersion =
                    config.getFirstProperty(APIConstants.API_WF_STREAM_VERSION);
            if (wfStreamName == null || wfStreamVersion == null) {
                log.error("Workflow stream name or version is null. Check api-manager.xml");
            }

            //If Workflow Stream Definition does not exist.
            if (!dataPublisher.isStreamDefinitionAdded(wfStreamName,
                                                       wfStreamVersion)) {

                //Get Workflow Stream Definition
                String wfStreamDefinition = getStreamDefinition();

                //Add Workflow Stream Definition;
                dataPublisher.addStreamDefinition(wfStreamDefinition,
                                                  wfStreamName,
                                                  wfStreamVersion);
            }
        } catch (MalformedURLException e) {
            log.error("Error initializing APIMgtWorkflowDataPublisher." + e.getMessage(), e);
        } catch (AgentException e) {
            log.error("Error initializing APIMgtWorkflowDataPublisher." + e.getMessage(), e);
        } catch (AuthenticationException e) {
            log.error("Error initializing APIMgtWorkflowDataPublisher." + e.getMessage(), e);
        } catch (TransportException e) {
            log.error("Error initializing APIMgtWorkflowDataPublisher." + e.getMessage(), e);
        }
    }

    private static LoadBalancingDataPublisher getDataPublisher()
            throws AgentException, MalformedURLException, AuthenticationException,
                   TransportException {

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        //Get LoadBalancingDataPublisher which has been registered for the tenant.
        LoadBalancingDataPublisher loadBalancingDataPublisher = getDataPublisher(tenantDomain);
        String bamServerURL = analyticsConfig.getBamServerUrlGroups();
        String bamServerUser = analyticsConfig.getBamServerUser();
        String bamServerPassword = analyticsConfig.getBamServerPassword();

        //If a LoadBalancingDataPublisher had not been registered for the tenant.
        if (loadBalancingDataPublisher == null) {

            List<String> receiverGroups = org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil.
                    getReceiverGroups(bamServerURL);

            List<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();

            for (String receiverGroupString : receiverGroups) {
                String[] serverURLs = receiverGroupString.split(",");
                List<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();

                for (String serverURL : serverURLs) {
                    DataPublisherHolder dataPublisherHolder =
                            new DataPublisherHolder(null, serverURL, bamServerUser, bamServerPassword);
                    dataPublisherHolders.add(dataPublisherHolder);
                }

                ReceiverGroup receiverGroup = new ReceiverGroup((ArrayList) dataPublisherHolders);
                allReceiverGroups.add(receiverGroup);
            }

            //Create new LoadBalancingDataPublisher for the tenant.
            loadBalancingDataPublisher = new LoadBalancingDataPublisher((ArrayList) allReceiverGroups);
            try {
                //Add created LoadBalancingDataPublisher.
                addDataPublisher(tenantDomain, loadBalancingDataPublisher);
            } catch (DataPublisherAlreadyExistsException e) {
                log.warn("Attempting to register a data publisher for the tenant " + tenantDomain +
                         " when one already exists. Returning existing data publisher");
                return getDataPublisher(tenantDomain);
            }
        }

        return loadBalancingDataPublisher;
    }

    public static String getStreamDefinition() {

        return "{" +
               "  'name':'" + getWFStreamName() +
               "'," +
               "  'version':'" + getWFStreamVersion() +
               "'," +
               "  'nickName': 'API Manager Workflow Data'," +
               "  'description': 'Workflow Data'," +
               "  'metaData':[" +
               "          {'name':'clientType','type':'STRING'}" +
               "  ]," +
               "  'payloadData':[" +
               "          {'name':'workflowReference','type':'STRING'}," +
               "          {'name':'workflowStatus','type':'STRING'}," +
               "          {'name':'tenantDomain','type':'STRING'}," +
               "          {'name':'workflowType','type':'STRING'}," +
               "          {'name':'createdTime','type':'LONG'}," +
               "          {'name':'updatedTime','type':'LONG'}" +
               "  ]" +

               "}";
    }


    public boolean publishEvent(WorkflowDTO workflowDTO) {
        try {
            if (!enabled) {
                return true;
            }

            if (workflowDTO != null) {
                try {
                    //Publish Workflow data
                    dataPublisher.publish(getWFStreamName(),
                                          getWFStreamVersion(),
                                          System.currentTimeMillis(), new Object[]{"external"},
                                          null,
                                          (Object[]) createPayload(workflowDTO));

                } catch (AgentException e) {
                    log.error("Error while publishing workflow event" +
                              workflowDTO.getWorkflowReference(), e);
                }


            }


        } catch (Throwable e) {
            log.error("Cannot publish workflow event. " + e.getMessage(), e);
        }
        return true;

    }

    public Object createPayload(WorkflowDTO workflowDTO) {
        return new Object[]{workflowDTO.getWorkflowReference(),
                            workflowDTO.getStatus().toString(), workflowDTO.getTenantDomain(),
                            workflowDTO.getWorkflowType(), workflowDTO.getCreatedTime(),
                            workflowDTO.getUpdatedTime()};
    }

    public static String getWFStreamName() {
        return wfStreamName;
    }

    public static String getWFStreamVersion() {
        return wfStreamVersion;
    }

    /**
     * Fetch the data publisher which has been registered under the tenant domain.
     *
     * @param tenantDomain - The tenant domain under which the data publisher is registered
     * @return - Instance of the LoadBalancingDataPublisher which was registered. Null if not registered.
     */
    public static LoadBalancingDataPublisher getDataPublisher(String tenantDomain) {
        if (dataPublisherMap.containsKey(tenantDomain)) {
            return dataPublisherMap.get(tenantDomain);
        }
        return null;
    }

    /**
     * Adds a LoadBalancingDataPublisher to the data publisher map.
     *
     * @param tenantDomain  - The tenant domain under which the data publisher will be registered.
     * @param dataPublisher - Instance of the LoadBalancingDataPublisher
     * @throws org.wso2.carbon.apimgt.impl.workflow.events.DataPublisherAlreadyExistsException
     *          - If a data publisher has already been registered under the
     *          tenant domain
     */
    public static void addDataPublisher(String tenantDomain,
                                        LoadBalancingDataPublisher dataPublisher)
            throws DataPublisherAlreadyExistsException {
        if (dataPublisherMap.containsKey(tenantDomain)) {
            throw new DataPublisherAlreadyExistsException("A DataPublisher has already been created for the tenant " +
                                                          tenantDomain);
        }

        dataPublisherMap.put(tenantDomain, dataPublisher);
    }

}