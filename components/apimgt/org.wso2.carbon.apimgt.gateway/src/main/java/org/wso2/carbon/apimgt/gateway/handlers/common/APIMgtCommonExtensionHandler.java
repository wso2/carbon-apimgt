/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.handlers.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.RESTUtils;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.dto.ExecutionTimePublisherDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashMap;
import java.util.Map;

public class APIMgtCommonExtensionHandler extends AbstractHandler implements ManagedLifecycle {
    private static final Log log = LogFactory.getLog(APIMgtCommonExtensionHandler.class);


    public boolean handleRequest(MessageContext messageContext) {
        if (messageContext.getProperty(APIMgtGatewayConstants.REQUEST_EXECUTION_START_TIME) == null) {
            messageContext.setProperty(APIMgtGatewayConstants.REQUEST_EXECUTION_START_TIME, Long.toString(System.currentTimeMillis()));
        }
        return true;
    }

    public boolean handleResponse(MessageContext messageContext) {
        if (APIUtil.isStatsEnabled()) {
            if (messageContext.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_END_TIME) == null) {

                long executionStartTime = Long.parseLong((String) messageContext.getProperty(APIMgtGatewayConstants
                        .BACKEND_REQUEST_START_TIME));
                messageContext.setProperty(APIMgtGatewayConstants.BACKEND_REQUEST_END_TIME, Long.toString(System
                        .currentTimeMillis
                                ()));
                publishExecutionTime(messageContext, executionStartTime, "BackEnd");
            }
        }
        return true;
    }

    public void init(SynapseEnvironment synapseEnvironment) {
    }

    public void destroy() {
    }

    public static void publishExecutionTime(MessageContext messageContext, long executionStartTime, String
            mediationType) {
        long executionTime = System.currentTimeMillis() - executionStartTime;
        ExecutionTimePublisherDTO executionTimePublisherDTO = new ExecutionTimePublisherDTO();
        String apiName = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
        String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String apiContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String tenantDomain = MultitenantUtils.getTenantDomain(RESTUtils.getFullRequestPath(messageContext));
        executionTimePublisherDTO.setApiName(APIUtil.getAPINamefromRESTAPI(apiName));
            if (executionStartTime == 0) {
                executionTimePublisherDTO.setExecutionTime(0);
            }
        executionTimePublisherDTO.setExecutionTime(executionTime);
        executionTimePublisherDTO.setMediationType(mediationType);
        executionTimePublisherDTO.setVersion(apiVersion);
        executionTimePublisherDTO.setContext(apiContext);
        String provider = APIUtil.getAPIProviderFromRESTAPI(apiName, tenantDomain);
        executionTimePublisherDTO.setProvider(provider);
        executionTimePublisherDTO.setTenantDomain(tenantDomain);
        executionTimePublisherDTO.setTenantId(APIUtil.getTenantId(provider));
        Map executionTimeMap;
        Object apiExecutionObject = messageContext.getProperty("api.execution.time");
        if (apiExecutionObject != null && apiExecutionObject instanceof Map) {
            executionTimeMap = (Map) apiExecutionObject;
        } else {
            executionTimeMap = new HashMap();
        }
        executionTimeMap.put(mediationType, executionTimePublisherDTO);
        messageContext.setProperty("api.execution.time", executionTimeMap);
    }

}
