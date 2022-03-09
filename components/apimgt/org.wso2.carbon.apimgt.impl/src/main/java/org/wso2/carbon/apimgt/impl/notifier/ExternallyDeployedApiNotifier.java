/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.notifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.deployer.ExternalGatewayDeployer;
import org.wso2.carbon.apimgt.impl.deployer.exceptions.DeployerException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;

import java.util.List;
import java.util.Map;

public class ExternallyDeployedApiNotifier extends ApisNotifier{
    protected ApiMgtDAO apiMgtDAO;
    private static final Log log = LogFactory.getLog(ExternalGatewayNotifier.class);

    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        apiMgtDAO = ApiMgtDAO.getInstance();
        process(event);
        return true;
    }

    /**
     * Process API lifecycle notifier events related to APIs deployed in external gateway
     *
     * @param event related to deployments
     * @throws NotifierException if error occurs when casting event
     */
    private void process (Event event) throws NotifierException {
        APIEvent apiEvent;
        apiEvent = (APIEvent) event;

        if (APIConstants.EventType.API_LIFECYCLE_CHANGE.name().equals(event.getType())) {
            // Handle API retiring life cycle change in external gateway
            undeployApiWhenRetiring(apiEvent);
        } else if (APIConstants.EventType.API_DELETE.name().equals(event.getType())) {
            // Handle API deletion in external gateway
            undeployWhenDeleting(apiEvent);
        }
    }

    /**
     * Undeploy APIs from external gateway when life cycle state changed to retire
     *
     * @param apiEvent APIEvent to undeploy APIs from external gateway
     * @throws NotifierException if error occurs when undeploying APIs from external gateway
     */
    private void undeployApiWhenRetiring(APIEvent apiEvent) throws NotifierException {

        apiMgtDAO = ApiMgtDAO.getInstance();
        Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        boolean deleted;
        String apiId = apiEvent.getUuid();

        if (!APIConstants.RETIRED.equals(apiEvent.getApiStatus())) {
            return;
        }

        try {
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            API api = apiProvider.getAPIbyUUID(apiId, apiMgtDAO.getOrganizationByAPIUUID(apiId));
            List<APIRevisionDeployment> test = apiMgtDAO.getAPIRevisionDeploymentsByApiUUID(apiId);

            for (APIRevisionDeployment deployment : test) {
                String deploymentEnv = deployment.getDeployment();
                if (gatewayEnvironments.containsKey(deploymentEnv)) {
                    ExternalGatewayDeployer deployer = ServiceReferenceHolder.getInstance().getExternalGatewayDeployer
                            (gatewayEnvironments.get(deploymentEnv).getProvider());
                    if (deployer != null) {
                        try {
                            deleted = deployer.undeployWhenRetire(api, gatewayEnvironments.get(deploymentEnv));
                            if (!deleted) {
                                throw new NotifierException("Error while deleting API product from Solace broker");
                            }
                        } catch (DeployerException e) {
                            throw new NotifierException(e.getMessage());
                        }
                    }
                }
            }
        } catch (APIManagementException e) {
            throw new NotifierException(e.getMessage());
        }

    }

    /**
     * Undeploy APIs from external gateway when API is deleted
     *
     * @param apiEvent APIEvent to undeploy APIs from external gateway
     * @throws NotifierException if error occurs when undeploying APIs from external gateway
     */
    private void undeployWhenDeleting(APIEvent apiEvent) throws NotifierException {

        Map<String, Environment> gatewayEnvironments = APIUtil.getReadOnlyGatewayEnvironments();
        boolean deleted;
        String apiId = apiEvent.getUuid();

        try {
            List<APIRevisionDeployment> test = apiMgtDAO.getAPIRevisionDeploymentsByApiUUID(apiId);
            for (APIRevisionDeployment deployment : test) {
                String deploymentEnv = deployment.getDeployment();
                if (gatewayEnvironments.containsKey(deploymentEnv)) {
                    ExternalGatewayDeployer deployer = ServiceReferenceHolder.getInstance().getExternalGatewayDeployer
                            (gatewayEnvironments.get(deploymentEnv).getProvider());
                    if (deployer != null) {
                        try {
                            deleted = deployer.undeploy(apiEvent.getApiName(), apiEvent.getApiVersion(),
                                    apiEvent.getApiContext(), gatewayEnvironments.get(deploymentEnv));
                            if (!deleted) {
                                throw new NotifierException("Error while deleting API product from Solace broker");
                            }
                        } catch (DeployerException e) {
                            throw new NotifierException(e.getMessage());
                        }
                    }
                }
            }
        } catch (APIManagementException e) {
            throw new NotifierException(e.getMessage());
        }

    }
}
