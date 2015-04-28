/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.impl.executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.registry.extensions.interfaces.Execution;
import org.wso2.carbon.governance.registry.extensions.internal.GovernanceRegistryExtensionsComponent;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is an implementation of the interface {@link org.wso2.carbon.governance.registry.extensions.interfaces.Execution}
 * This class consists methods that will publish an API to API Manager.
 *
 * This class gets initiated when a REST Service lifecycle is added to a REST Service. In initialization following
 * static configuration parameters should be defined in the lifecycle.
 *  Eg:- <execution forEvent="Publish" class="org.wso2.carbon.governance.registry.extensions.executors.APIPublishExecutor">
 *           <parameter name="apim.endpoint" value="http://localhost:9763/"/>
 *           <parameter name="apim.username" value="admin"/>
 *           <parameter name="apim.password" value="admin"/>
 *           <parameter name="default.tier" value="Unlimited"/>
 *           <parameter name="throttlingTier" value="Unlimited,Unlimited,Unlimited,Unlimited,Unlimited"/>
 *       </execution>
 *
 * Once the REST Service is is set to publish to API Manager, the execute method will get called. The execute method
 * contains the logic to publish an API to API Manager through a http POST request to API Publisher. The method will
 * return true if the API published successfully and false if fails to publish the API.
 *
 * @see org.wso2.carbon.governance.registry.extensions.interfaces.Execution
 */
public class ApiPublisherExecutor implements Execution {

    Log log = LogFactory.getLog(ApiPublisherExecutor.class);

    /**
     * This method is called when the execution class is initialized.
     * All the execution classes are initialized only once.
     *
     * @param parameterMap Static parameter map given by the user.
     *                     These are the parameters that have been given in the
     *                     lifecycle configuration as the parameters of the executor.
     */
    public void init(Map parameterMap) {
    }

    /**
     * @param context      The request context that was generated from the registry core.
     *                     The request context contains the resource, resource path and other
     *                     variables generated during the initial call.
     * @param currentState The current lifecycle state.
     * @param targetState  The target lifecycle state.
     * @return Returns whether the execution was successful or not.
     */
    public boolean execute(RequestContext context, String currentState, String targetState) {
        boolean executed = false;
        String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
        Map<String, List<String>> failedGateways = new ConcurrentHashMap<String, List<String>>();
        try {
            GenericArtifactManager artifactManager = APIUtil
                    .getArtifactManager(context.getSystemRegistry(), APIConstants.API_KEY);
            Resource apiResource = context.getResource();
            String artifactId = apiResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException(
                        "artifact id is null for : " + context.getResourcePath().getCompletePath());
            }
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
            API api = APIUtil.getAPI(apiArtifact);
            APIStatus newStatus = getApiStatus(targetState);
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(user);
            failedGateways = apiProvider.changeAPIStatus(api, newStatus, user, true);
            if (failedGateways == null || failedGateways.size() == 0) {
                executed = true;
            }
        } catch (RegistryException e) {
            log.error("Failed to get the generic artifact ", e);
            return false;
        } catch (APIManagementException e) {
            log.error("Failed to publish service to API store ", e);
            return false;
        }
        return executed;
    }

    private static APIStatus getApiStatus(String status) {
        APIStatus apiStatus = null;
        for (APIStatus aStatus : APIStatus.values()) {
            if (aStatus.getStatus().equalsIgnoreCase(status)) {
                apiStatus = aStatus;
            }
        }
        return apiStatus;
    }
}
