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
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
* This class will act as data-publisher for workflow events.Reason for not re-using the usage
* publisher bundle is there's a maven cyclic dependency appears if we import it from impl bundle.
*/

public class APIMgtWorkflowDataPublisher {

    private static final Log log = LogFactory.getLog(APIMgtWorkflowDataPublisher.class);
    private DataPublisher dataPublisher;
    private static Map<String, DataPublisher> dataPublisherMap;
    static APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
            getAPIManagerConfigurationService().
            getAPIManagerConfiguration();
    static APIManagerAnalyticsConfiguration analyticsConfig = ServiceReferenceHolder.getInstance().
            getAPIManagerConfigurationService().
            getAPIAnalyticsConfiguration();
    boolean enabled = APIUtil.isAnalyticsEnabled();
    private static String wfStreamName;
    private static String wfStreamVersion;
    private static DataPublisher dataPublisherStatics;

    public APIMgtWorkflowDataPublisher() {
        if (!enabled) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Initializing APIMgtUsageDataBridgeDataPublisher");
        }

        wfStreamName = config.getFirstProperty(APIConstants.API_WF_STREAM_NAME);
        wfStreamVersion = config.getFirstProperty(APIConstants.API_WF_STREAM_VERSION);
        if (wfStreamName == null || wfStreamVersion == null) {
            log.error("Workflow stream name or version is null. Check api-manager.xml");
        }

        dataPublisherMap = new ConcurrentHashMap<String, DataPublisher>();
        this.dataPublisher = getDataPublisher();
    }

    private static DataPublisher getDataPublisher() {

        //If a DataPublisher had not been registered for the tenant.
        if (dataPublisherStatics == null) {

            //Get DataPublisher which has been registered for the tenant.
            String serverURL = analyticsConfig.getDasReceiverUrlGroups();
            String serverAuthURL = analyticsConfig.getDasReceiverAuthUrlGroups();
            String serverUser = analyticsConfig.getDasReceiverServerUser();
            String serverPassword = analyticsConfig.getDasReceiverServerPassword();

            try {
                //Create new DataPublisher for the tenant.
                synchronized (APIMgtWorkflowDataPublisher.class) {

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

    public boolean publishEvent(WorkflowDTO workflowDTO) {
        try {
            if (!enabled) {
                return true;
            }

            if (workflowDTO != null) {
                try {

                    dataPublisher.publish(getStreamID(), System.currentTimeMillis(), new Object[]{"external"},
                            null, (Object[]) createPayload(workflowDTO));
                } catch (Exception e) {
                    log.error("Error while publishing workflow event" +
                              workflowDTO.getWorkflowReference(), e);
                }
            }
        } catch (Exception e) {
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

    public static String getStreamID() {
        return getWFStreamName() + ":"+ getWFStreamVersion();
    }

    public static String getWFStreamVersion() {
        return wfStreamVersion;
    }

    /**
     * Fetch the data publisher which has been registered under the tenant domain.
     *
     * @param tenantDomain - The tenant domain under which the data publisher is registered
     * @return - Instance of the DataPublisher which was registered. Null if not registered.
     */
    public static DataPublisher getDataPublisher(String tenantDomain) {
        if (dataPublisherMap.containsKey(tenantDomain)) {
            return dataPublisherMap.get(tenantDomain);
        }
        return null;
    }

    /**
     * Adds a DataPublisher to the data publisher map.
     *
     * @param tenantDomain  - The tenant domain under which the data publisher will be registered.
     * @param dataPublisher - Instance of the DataPublisher
     * @throws org.wso2.carbon.apimgt.impl.workflow.events.DataPublisherAlreadyExistsException
     *          - If a data publisher has already been registered under the
     *          tenant domain
     */
    public static void addDataPublisher(String tenantDomain,
                                        DataPublisher dataPublisher)
            throws DataPublisherAlreadyExistsException {
        if (dataPublisherMap.containsKey(tenantDomain)) {
            throw new DataPublisherAlreadyExistsException("A DataPublisher has already been created for the tenant " +
                                                          tenantDomain);
        }

        dataPublisherMap.put(tenantDomain, dataPublisher);
    }

}
