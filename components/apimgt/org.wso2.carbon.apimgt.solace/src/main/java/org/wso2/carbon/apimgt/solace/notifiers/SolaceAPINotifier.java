/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.solace.notifiers;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.notifier.ApisNotifier;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.solace.utils.SolaceConstants;
import org.wso2.carbon.apimgt.solace.utils.SolaceNotifierUtils;
import org.wso2.carbon.context.CarbonContext;

import java.util.List;
import java.util.Map;

/**
 * This class controls the Solace Broker deployed API update flows
 */
public class SolaceAPINotifier extends ApisNotifier {

    protected ApiMgtDAO apiMgtDAO;

    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        if (SolaceNotifierUtils.isSolaceEnvironmentDefined()) {
            apiMgtDAO = ApiMgtDAO.getInstance();
            process(event);
        }
        return true;
    }

    /**
     * Process Application notifier event related to Solace applications
     *
     * @param event related to API handling
     * @throws NotifierException if error occurs when casting event
     */
    private void process(Event event) throws NotifierException {
        APIEvent apiEvent = (APIEvent) event;

        if (APIConstants.EventType.API_UPDATE.name().equals(event.getType())) {
            updateSolaceAPI(apiEvent);
        }
    }

    /**
     * Update Solace API Definition
     *
     * @param event API event to update Solace API Definition
     * @throws NotifierException if error occurs when updating APIs on the Solace broker
     */
    private void updateSolaceAPI(APIEvent event) throws NotifierException {

        String apiId = event.getUuid();
        Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        boolean isDeployedInSolace = false;
        Environment solaceDeploymentEnvironment = null;
        try {
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            API api = apiProvider.getAPIbyUUID(apiId, apiMgtDAO.getOrganizationByAPIUUID(apiId));

            //Check whether the API is deployed in Solace environment.
            List<APIRevisionDeployment> deployments = apiMgtDAO.getAPIRevisionDeploymentByApiUUID(event.getUuid());
            for (APIRevisionDeployment deployment : deployments) {
                if (gatewayEnvironments.containsKey(deployment.getDeployment())) {
                    if (SolaceConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(gatewayEnvironments.get(deployment.
                            getDeployment()).getProvider())) {
                        isDeployedInSolace = true;
                        solaceDeploymentEnvironment = gatewayEnvironments.get(deployment.getDeployment());
                    }
                }
            }
            // Updating application using Solace Admin APIs
            if (isDeployedInSolace) {
                SolaceNotifierUtils.updateAsyncAPIDefinition(api, solaceDeploymentEnvironment);
            }
        } catch (APIManagementException e) {
            throw new NotifierException(e.getMessage());
        }
    }
}
