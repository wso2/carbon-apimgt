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
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.deployer.ExternalGatewayDeployer;
import org.wso2.carbon.apimgt.impl.deployer.exceptions.DeployerException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;


import java.util.Map;
import java.util.Set;

public class ExternalGatewayNotifier extends DeployAPIInGatewayNotifier {
    protected ApiMgtDAO apiMgtDAO;
    private static final Log log = LogFactory.getLog(ExternalGatewayNotifier.class);

    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        if (APIUtil.isAnyExternalGateWayProviderExists()) {
            apiMgtDAO = ApiMgtDAO.getInstance();
            process(event);
        }
        return true;
    }

    /**
     * Process gateway notifier events related to External gateway deployments
     *
     * @param event related to deployments
     * @throws NotifierException if error occurs when casting event
     */
    private void process(Event event) throws NotifierException {
        DeployAPIInGatewayEvent deployAPIInGatewayEvent;
        deployAPIInGatewayEvent = (DeployAPIInGatewayEvent) event;
        if (isExternalGatewayAvailableToDeployment(deployAPIInGatewayEvent))
            if (APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name().equals(event.getType())) {
                deployApi(deployAPIInGatewayEvent);
            } else if (APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name().equals(event.getType())) {
                unDeployApi(deployAPIInGatewayEvent);
            }
    }

    /**
     * Deploy APIs to external gateway
     *
     * @param deployAPIInGatewayEvent DeployAPIInGatewayEvent to deploy APIs to external gateway
     * @throws NotifierException if error occurs when deploying APIs to external gateway
     */
    private void deployApi(DeployAPIInGatewayEvent deployAPIInGatewayEvent) throws NotifierException {

        boolean deployed;
        Set<String> gateways = deployAPIInGatewayEvent.getGatewayLabels();
        String apiId = deployAPIInGatewayEvent.getUuid();

        try {
            Map<String, Environment> environments = APIUtil.getEnvironments(deployAPIInGatewayEvent.getTenantDomain());
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(CarbonContext.
                    getThreadLocalCarbonContext().getUsername());
            API api = apiProvider.getAPIbyUUID(apiId, apiMgtDAO.getOrganizationByAPIUUID(apiId));

            for (String deploymentEnv : gateways) {
                if (environments.containsKey(deploymentEnv)) {
                    ExternalGatewayDeployer deployer = ServiceReferenceHolder.getInstance().getExternalGatewayDeployer
                            (environments.get(deploymentEnv).getProvider());
                    if (deployer != null) {
                        try {
                            deployed = deployer.deploy(api, environments.get(deploymentEnv));
                            if (!deployed) {
                                throw new APIManagementException("Error while deploying API product to Solace broker");
                            }
                        } catch (DeployerException e) {
                            throw new APIManagementException(e.getMessage());
                        }
                    }
                }
            }
        } catch (APIManagementException e) {
            throw new NotifierException(e.getMessage());
        }
    }

    /**
     * Undeploy APIs from external gateway
     *
     * @param deployAPIInGatewayEvent DeployAPIInGatewayEvent to undeploy APIs from external gateway
     * @throws NotifierException if error occurs when undeploying APIs from external gateway
     */
    private void unDeployApi(DeployAPIInGatewayEvent deployAPIInGatewayEvent) throws NotifierException {

        boolean deleted;
        Set<String> gateways = deployAPIInGatewayEvent.getGatewayLabels();
        String apiId = deployAPIInGatewayEvent.getUuid();
        try {
            Map<String, Environment> environments = APIUtil.getEnvironments(deployAPIInGatewayEvent.getTenantDomain());

            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(deployAPIInGatewayEvent.getProvider());
            API api = apiProvider.getAPIbyUUID(apiId, apiMgtDAO.getOrganizationByAPIUUID(apiId));

            for (String deploymentEnv : gateways) {
                if (environments.containsKey(deploymentEnv)) {
                    ExternalGatewayDeployer deployer = ServiceReferenceHolder.getInstance().getExternalGatewayDeployer
                            (environments.get(deploymentEnv).getProvider());
                    if (deployer != null) {
                        try {
                            deleted = deployer.undeploy(api.getId().getName(), api.getId().getVersion(),
                                    api.getContext(), environments.get(deploymentEnv));
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

    private boolean isExternalGatewayAvailableToDeployment(DeployAPIInGatewayEvent deployAPIInGatewayEvent)
            throws NotifierException {
        Set<String> gatewayLabels = deployAPIInGatewayEvent.getGatewayLabels();
        try {
            Map<String, Environment> environments = APIUtil.getEnvironments(deployAPIInGatewayEvent.getTenantDomain());
            for (String label : gatewayLabels) {
                Environment environment = environments.get(label);
                if (environment != null && !APIConstants.WSO2_GATEWAY_ENVIRONMENT.equals(environment.getProvider())) {
                    return true;
                }
            }
            return false;
        } catch (APIManagementException e) {
            throw new NotifierException(e);
        }
    }
}
