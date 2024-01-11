/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway;

import com.google.gson.Gson;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.gateway.GatewayPolicyDTO;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.service.APIGatewayAdmin;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactRetriever;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.List;
import java.util.Set;

/**
 * This class contains the methods used to retrieve artifacts from a storage and deploy and undeploy
 * the gateway policy mappings in gateway.
 */
public class GatewayPolicyDeployer {

    private static final Log log = LogFactory.getLog(GatewayPolicyDeployer.class);
    ArtifactRetriever artifactRetriever;
    private String gatewayPolicyMappingUuid;
    GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties;

    public GatewayPolicyDeployer(String gatewayPolicyMappingUuid) {
        this.artifactRetriever = ServiceReferenceHolder.getInstance().getArtifactRetriever();
        this.gatewayPolicyMappingUuid = gatewayPolicyMappingUuid;
        this.gatewayArtifactSynchronizerProperties = ServiceReferenceHolder
                .getInstance().getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties();
    }

    public GatewayPolicyDeployer() {
        this.artifactRetriever = ServiceReferenceHolder.getInstance().getArtifactRetriever();
        this.gatewayArtifactSynchronizerProperties = ServiceReferenceHolder
                .getInstance().getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties();
    }

    /**
     * Deploy gateway policy in the gateway using the deployGatewayPolicy method in gateway admin.
     *
     * @throws ArtifactSynchronizerException  If an error occurs while retrieving the gateway policy artifact.
     * @throws APIManagementException         If an error occurs while deploying the gateway policy artifact.
     */
    public void deployGatewayPolicyMapping() throws ArtifactSynchronizerException, APIManagementException {
        try {
            GatewayPolicyDTO gatewayPolicyDTO = retrieveGatewayPolicyArtifact(gatewayPolicyMappingUuid);
            if (gatewayPolicyDTO != null) {
                APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdmin();
                MessageContext.setCurrentMessageContext(
                        org.wso2.carbon.apimgt.gateway.utils.GatewayUtils.createAxis2MessageContext());
                apiGatewayAdmin.deployGatewayPolicy(gatewayPolicyDTO);
            } else {
                log.error("Gateway policy artifact is not found for the policy mapping: " + gatewayPolicyMappingUuid);
            }
        } catch (AxisFault e) {
            throw new APIManagementException("Error encountered while deploying gateway policy mapping.", e);
        }
    }

    /**
     * Undeploy gateway policy in the gateway using the unDeployGatewayPolicy method in gateway admin.
     *
     * @throws ArtifactSynchronizerException  If an error occurs while retrieving the gateway policy artifact.
     * @throws APIManagementException         If an error occurs while un-deploying the gateway policy artifact.
     */
    public void undeployGatewayPolicyMapping() throws ArtifactSynchronizerException, APIManagementException {
        try {
            GatewayPolicyDTO gatewayPolicyDTO = retrieveGatewayPolicyArtifact(gatewayPolicyMappingUuid);
            if (gatewayPolicyDTO != null) {
                APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdmin();
                MessageContext.setCurrentMessageContext(
                        org.wso2.carbon.apimgt.gateway.utils.GatewayUtils.createAxis2MessageContext());
                apiGatewayAdmin.unDeployGatewayPolicy(gatewayPolicyDTO);
            }
        } catch (AxisFault e) {
            throw new APIManagementException("Error encountered while undeploying gateway policy mapping.", e);
        }
    }

    /**
     * Deploy gateway policy in the gateway using the deployGatewayPolicy method in gateway admin.
     *
     * @param assignedGatewayLabels  The labels which the gateway subscribed to
     * @param tenantDomain          tenantDomain of API.
     * @return True if all gateway policy artifacts retrieved from the storage and successfully deployed without
     * any error. else false.
     */
    public boolean deployGlobalPoliciesAtGatewayStartup(Set<String> assignedGatewayLabels, String tenantDomain)
            throws ArtifactSynchronizerException {

        boolean result = false;

        if (gatewayArtifactSynchronizerProperties.isRetrieveFromStorageEnabled()) {
            if (artifactRetriever != null) {
                try {
                    String labelString = String.join("|", assignedGatewayLabels);
                    String encodedString = Base64.encodeBase64URLSafeString(labelString.getBytes());
                    APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdmin();
                    MessageContext.setCurrentMessageContext(
                            org.wso2.carbon.apimgt.gateway.utils.GatewayUtils.createAxis2MessageContext());
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                    List<String> gatewayRuntimeArtifacts = ServiceReferenceHolder.getInstance().getArtifactRetriever()
                            .retrieveGatewayPolicyArtifacts(encodedString, tenantDomain);
                    if (gatewayRuntimeArtifacts.size() == 0) {
                        return true;
                    }
                    for (String runtimeArtifact : gatewayRuntimeArtifacts) {
                        try {
                            if (StringUtils.isNotEmpty(runtimeArtifact)) {
                                GatewayPolicyDTO gatewayPolicyDTO = new Gson().fromJson(runtimeArtifact,
                                        GatewayPolicyDTO.class);
                                apiGatewayAdmin.deployGatewayPolicy(gatewayPolicyDTO);
                            }
                        } catch (AxisFault axisFault) {
                            log.error(
                                    "Error in deploying gateway policy artifacts to the Gateway during server startup ",
                                    axisFault);
                        }
                    }
                    result = true;
                } catch (ArtifactSynchronizerException | AxisFault e) {
                    String msg = "Error deploying policy artifacts to the Gateway ";
                    log.error(msg, e);
                    return false;
                } finally {
                    MessageContext.destroyCurrentMessageContext();
                    PrivilegedCarbonContext.endTenantFlow();
                }
            } else {
                String msg = "Artifact retriever not found";
                log.error(msg);
                throw new ArtifactSynchronizerException(msg);
            }
        }
        return result;
    }

    private GatewayPolicyDTO retrieveGatewayPolicyArtifact(String policyMappingUUUID)
            throws ArtifactSynchronizerException {

        GatewayPolicyDTO result;

        if (artifactRetriever != null) {
            try {
                String gatewayRuntimeArtifact = artifactRetriever.retrieveGatewayPolicyArtifacts(policyMappingUUUID);
                if (StringUtils.isNotEmpty(gatewayRuntimeArtifact)) {
                    result = new Gson().fromJson(gatewayRuntimeArtifact, GatewayPolicyDTO.class);
                } else {
                    String msg = "Error retrieving artifacts for policy mapping UUID " + policyMappingUUUID
                            + ". Storage returned null";
                    log.error(msg);
                    throw new ArtifactSynchronizerException(msg);
                }
            } catch (ArtifactSynchronizerException e) {
                String msg = "Error deploying policy mapping " + policyMappingUUUID + " in Gateway";
                log.error(msg, e);
                throw new ArtifactSynchronizerException(msg, e);
            }
        } else {
            String msg = "Artifact retriever not found";
            log.error(msg);
            throw new ArtifactSynchronizerException(msg);
        }
        return result;
    }
}
