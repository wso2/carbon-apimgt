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

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.GatewayAgentConfiguration;
import org.wso2.carbon.apimgt.api.model.GatewayDeployer;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.factory.GatewayHolder;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.RuntimeArtifactGeneratorUtil;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;


import java.util.Map;
import java.util.Set;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class ExternalGatewayNotifier extends DeployAPIInGatewayNotifier {
    private static final Log log = LogFactory.getLog(ExternalGatewayNotifier.class);

    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        if (APIUtil.isAnyExternalGateWayProviderExists(event.getTenantDomain())) {
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
        if (deployAPIInGatewayEvent.getGatewayLabels() != null && !deployAPIInGatewayEvent.getGatewayLabels().isEmpty()) {
            if (log.isDebugEnabled()){
                log.debug("Processing external gateway deployment for API: " + deployAPIInGatewayEvent.getUuid());
            }
        }
        if (isExternalGatewayAvailableToDeployment(deployAPIInGatewayEvent)) {
            if (APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name().equals(event.getType())) {
                deployApi(deployAPIInGatewayEvent);
            } else if (APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name().equals(event.getType())) {
                unDeployApi(deployAPIInGatewayEvent);
            }
        }
    }

    /**
     * Deploy APIs to external gateway
     *
     * @param deployAPIInGatewayEvent DeployAPIInGatewayEvent to deploy APIs to external gateway
     * @throws NotifierException if error occurs when deploying APIs to external gateway
     */
    private void deployApi(DeployAPIInGatewayEvent deployAPIInGatewayEvent) throws NotifierException {
        Set<String> gateways = deployAPIInGatewayEvent.getGatewayLabels();
        String apiId = deployAPIInGatewayEvent.getUuid();
        if (log.isDebugEnabled()) {
            log.debug("Deploying API: " + apiId + " to external gateways");
        }
        try {
            Map<String, Environment> environments = APIUtil.getEnvironments(deployAPIInGatewayEvent.getTenantDomain());
            for (String deploymentEnv : gateways) {
                if (environments.containsKey(deploymentEnv)) {
                    GatewayAgentConfiguration gatewayConfiguration = ServiceReferenceHolder.getInstance()
                            .getExternalGatewayConnectorConfiguration(environments.get(deploymentEnv).getGatewayType());
                    GatewayDeployer deployer = null;
                    if (gatewayConfiguration != null &&
                            StringUtils.isNotEmpty(gatewayConfiguration.getGatewayDeployerImplementation())) {
                        deployer = GatewayHolder.getTenantGatewayInstance(deployAPIInGatewayEvent.getTenantDomain(),
                                deploymentEnv);
                    }
                    if (deployer == null) {
                        log.warn("No gateway deployer found for environment: " + deploymentEnv);
                        return;
                    }
                    String referenceArtifact = APIUtil.getApiExternalApiMappingReferenceByApiId(apiId,
                            environments.get(deploymentEnv).getUuid());
                    String encodedGatewayLabel = new String(Base64.encodeBase64(
                            deploymentEnv.getBytes(StandardCharsets.UTF_8)));
                    RuntimeArtifactDto federatedArtifact =
                            RuntimeArtifactGeneratorUtil.generateRuntimeArtifact(apiId, encodedGatewayLabel,
                                    "Federated",
                                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain());
                    if (federatedArtifact == null || federatedArtifact.isFile() ||
                            !(federatedArtifact.getArtifact() instanceof List)) {
                        continue;
                    }
                    List artifactList = (List) federatedArtifact.getArtifact();
                    if (artifactList.isEmpty()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Empty artifact list for API: " + apiId);
                        }
                        continue;
                    }
                    Object artifact = artifactList.get(0);
                    if (artifact instanceof API) {
                        API api = (API) artifact;
                        String updatedReferenceArtifact = deployer.deploy(api, referenceArtifact);
                        if (updatedReferenceArtifact == null) {
                            log.error(
                                    "Failed to deploy API: " + api.getUuid() + " to external gateway in environment: " +
                                            deploymentEnv);
                            throw new APIManagementException("Error while deploying API to the external gateway");
                        }
                        if (referenceArtifact == null) {
                            APIUtil.addApiExternalApiMapping(apiId, environments.get(deploymentEnv).getUuid(),
                                    updatedReferenceArtifact);
                        } else {
                            APIUtil.updateApiExternalApiMapping(apiId, environments.get(deploymentEnv).getUuid(),
                                    updatedReferenceArtifact);
                        }
                        log.info("Successfully deployed API: " + api.getUuid() + " to environment: " + deploymentEnv);
                    }
                }

            }
        } catch (APIManagementException e) {
            log.error("Error while deploying API: " + apiId, e);
            throw new NotifierException(e.getMessage());
        }
    }

    /**
     * Undeploy APIs from external gateway
     *
     * @param deployAPIInGatewayEvent DeployAPIInGatewayEvent to undeploying APIs from external gateway
     * @throws NotifierException if error occurs when undeploying APIs from external gateway
     */
    private void unDeployApi(DeployAPIInGatewayEvent deployAPIInGatewayEvent) throws NotifierException {

        boolean deleted;
        Set<String> gateways = deployAPIInGatewayEvent.getGatewayLabels();
        String apiId = deployAPIInGatewayEvent.getUuid();
        try {
            Map<String, Environment> environments = APIUtil.getEnvironments(deployAPIInGatewayEvent.getTenantDomain());

            for (String deploymentEnv : gateways) {
                if (environments.containsKey(deploymentEnv)) {
                    GatewayDeployer deployer =
                            GatewayHolder.getTenantGatewayInstance(deployAPIInGatewayEvent.getTenantDomain(),
                                    deploymentEnv);
                    if (deployer != null) {
                        String referenceArtifact = APIUtil.getApiExternalApiMappingReferenceByApiId(apiId,
                                environments.get(deploymentEnv).getUuid());
                        if (referenceArtifact == null) {
                            throw new APIManagementException("API is not mapped with an External API");
                        }
                        deleted = deployer.undeploy(referenceArtifact, deployAPIInGatewayEvent.isDeleted());
                        if (log.isDebugEnabled()) {
                            log.debug("Undeploy API with reference artifact: " + referenceArtifact + ", isDeleted: " +
                                    deployAPIInGatewayEvent.isDeleted());
                        }
                        if (!deleted) {
                            throw new NotifierException("Error while deleting externally deployed API");
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
