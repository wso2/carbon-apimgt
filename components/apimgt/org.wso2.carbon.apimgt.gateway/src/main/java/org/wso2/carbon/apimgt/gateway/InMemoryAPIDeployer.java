/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway;

import com.google.gson.Gson;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.gateway.GatewayContentDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.gateway.internal.CertificateDataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactRetriever;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.impl.utils.APIGatewayAdminClient;
import org.wso2.carbon.apimgt.impl.utils.GatewayUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class contains the methods used to retrieve artifacts from a storage and deploy and undeploy the API in gateway
 */
public class InMemoryAPIDeployer {

    private static Log log = LogFactory.getLog(InMemoryAPIDeployer.class);
    private boolean debugEnabled = log.isDebugEnabled();
    ArtifactRetriever artifactRetriever;
    GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties;

    public InMemoryAPIDeployer() {

        this.artifactRetriever = ServiceReferenceHolder.getInstance().getArtifactRetriever();
        this.gatewayArtifactSynchronizerProperties = ServiceReferenceHolder
                .getInstance().getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties();
    }

    /**
     * Deploy an API in the gateway using the deployAPI method in gateway admin
     *
     * @param apiId        - UUID of the API
     * @param gatewayLabel - Label of the Gateway
     * @return True if API artifact retrieved from the storage and successfully deployed without any error. else false
     */
    public boolean deployAPI(String apiId, String gatewayLabel) throws ArtifactSynchronizerException {

        if (gatewayArtifactSynchronizerProperties.isRetrieveFromStorageEnabled() &&
                gatewayArtifactSynchronizerProperties.getGatewayLabels().contains(gatewayLabel)) {
            if (artifactRetriever != null) {
                try {
                    String gatewayRuntimeArtifact = artifactRetriever.retrieveArtifact(apiId, gatewayLabel,
                            APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_PUBLISH);
                    if (StringUtils.isNotEmpty(gatewayRuntimeArtifact)) {
                        GatewayAPIDTO gatewayAPIDTO = new Gson().fromJson(gatewayRuntimeArtifact, GatewayAPIDTO.class);
                        APIGatewayAdminClient apiGatewayAdminClient = new APIGatewayAdminClient();
                        apiGatewayAdminClient.deployAPI(gatewayAPIDTO);
                        addDeployedCertificatesToAPIAssociation(gatewayAPIDTO);
                        if (debugEnabled) {
                            log.debug(
                                    "API with " + apiId + " is deployed in gateway with the label of " + gatewayLabel);
                        }
                        return true;
                    } else {
                        String msg = "Error retrieving artifacts for API " + apiId + ". Storage returned null";
                        log.error(msg);
                        throw new ArtifactSynchronizerException(msg);
                    }
                } catch (IOException | ArtifactSynchronizerException e) {
                    String msg = "Error deploying " + apiId + " in Gateway";
                    log.error(msg, e);
                    throw new ArtifactSynchronizerException(msg, e);
                }
            } else {
                String msg = "Artifact retriever not found";
                log.error(msg);
                throw new ArtifactSynchronizerException(msg);
            }
        }
        return false;
    }

    /**
     * Deploy an API in the gateway using the deployAPI method in gateway admin
     *
     * @param assignedGatewayLabels - The labels which the gateway subscribed to
     * @param tenantDomain
     * @return True if all API artifacts retrieved from the storage and successfully deployed without any error. else
     * false
     */
    public boolean deployAllAPIsAtGatewayStartup(Set<String> assignedGatewayLabels, String tenantDomain) throws
            ArtifactSynchronizerException {

        if (gatewayArtifactSynchronizerProperties.isRetrieveFromStorageEnabled()) {
            if (artifactRetriever != null) {
                try {
                    Iterator<String> it = assignedGatewayLabels.iterator();
                    APIGatewayAdminClient apiGatewayAdminClient = new APIGatewayAdminClient();
                    while (it.hasNext()) {
                        String label = it.next();
                        List<String> gatewayRuntimeArtifacts = ServiceReferenceHolder
                                .getInstance().getArtifactRetriever().retrieveAllArtifacts(label, tenantDomain);
                        for (String runtimeArtifact : gatewayRuntimeArtifacts) {
                            GatewayAPIDTO gatewayAPIDTO = null;
                            try {
                                if (StringUtils.isNotEmpty(runtimeArtifact)) {
                                    gatewayAPIDTO = new Gson().fromJson(runtimeArtifact, GatewayAPIDTO.class);
                                    log.info("Deploying synapse artifacts of " + gatewayAPIDTO.getName());
                                    apiGatewayAdminClient.deployAPI(gatewayAPIDTO);
                                    addDeployedCertificatesToAPIAssociation(gatewayAPIDTO);
                                }
                            } catch (AxisFault axisFault) {
                                log.error("Error in deploying " + gatewayAPIDTO.getName() + " to the Gateway ");
                            }
                        }

                        if (debugEnabled) {
                            log.debug("APIs deployed in gateway with the label of " + label);
                        }
                    }
                    return true;
                } catch (ArtifactSynchronizerException | IOException e) {
                    String msg = "Error  deploying APIs to the Gateway ";
                    log.error(msg, e);
                    throw new ArtifactSynchronizerException(msg, e);
                }
            } else {
                String msg = "Artifact retriever not found";
                log.error(msg);
                throw new ArtifactSynchronizerException(msg);
            }
        }
        return false;
    }

    /**
     * UnDeploy an API in the gateway using the uneployAPI method in gateway admin
     *
     * @param apiId        - UUID of the API
     * @param gatewayLabel - Label of the Gateway
     * @return True if API artifact retrieved from the storage and successfully undeployed without any error. else false
     */
    public boolean unDeployAPI(String apiId, String gatewayLabel) throws ArtifactSynchronizerException {

        return false;
    }

    /**
     * Retrieve artifacts from the storage
     *
     * @param apiId        - UUID of the API
     * @param gatewayLabel - Label of the Gateway
     * @return DTO Object that contains the information and artifacts of the API for the given label
     */
    public GatewayAPIDTO getAPIArtifact(String apiId, String gatewayLabel) throws ArtifactSynchronizerException {

        GatewayAPIDTO gatewayAPIDTO = null;
        if (gatewayArtifactSynchronizerProperties.getGatewayLabels().contains(gatewayLabel)) {
            if (artifactRetriever != null) {
                try {
                    String gatewayRuntimeArtifact = artifactRetriever.retrieveArtifact(apiId, gatewayLabel,
                            APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_PUBLISH);
                    if (StringUtils.isNotEmpty(gatewayRuntimeArtifact)) {
                        gatewayAPIDTO = new Gson().fromJson(gatewayRuntimeArtifact, GatewayAPIDTO.class);
                        if (debugEnabled) {
                            log.debug("Retrieved artifacts for API  " + apiId + " retrieved from eventhub");
                        }
                    } else {
                        String msg = "Error retrieving artifacts for API " + apiId + ". Storage returned null";
                        log.error(msg);
                        throw new ArtifactSynchronizerException(msg);
                    }
                } catch (ArtifactSynchronizerException | IOException e) {
                    String msg = "Error in retrieving artifacts for API " + apiId;
                    log.error(msg);
                    throw new ArtifactSynchronizerException(msg);
                }
            } else {
                String msg = "Artifact retriever not found";
                log.error(msg);
                throw new ArtifactSynchronizerException(msg);
            }
        }
        return gatewayAPIDTO;
    }

    /**
     * Retrieve artifacts from the storage
     *
     *@param apiName - Name of the API
     *@param version - version of the API
     *@param tenantDomain - Tenant Domain of the API
     * @return Map that contains the UUID and label of the API
     */
    public Map<String, String> getGatewayAPIAttributes(String apiName, String version, String tenantDomain)
            throws ArtifactSynchronizerException {

        Map<String, String> apiAttributes = null;
        if (artifactRetriever != null) {
            try {
                apiAttributes = artifactRetriever.retrieveAttributes(apiName, version, tenantDomain);
                log.debug("API Attributes retrieved for " + apiName + "  from storage");
            } catch (ArtifactSynchronizerException e) {
                String msg = "Error retrieving artifacts of " + apiName + " from storage";
                log.error(msg, e);
                throw new ArtifactSynchronizerException(msg, e);
            }
        } else {
            String msg = "Artifact retriever not found";
            log.error(msg);
            throw new ArtifactSynchronizerException(msg);
        }
        return apiAttributes;
    }

    public void unDeployAPI(DeployAPIInGatewayEvent gatewayEvent) throws ArtifactSynchronizerException {

        try {
            if (gatewayArtifactSynchronizerProperties.isRetrieveFromStorageEnabled()) {
                APIGatewayAdminClient apiGatewayAdmin = new APIGatewayAdminClient();

                API api = new API(new APIIdentifier(gatewayEvent.getProvider(), gatewayEvent.getName(),
                        gatewayEvent.getVersion()));
                GatewayAPIDTO gatewayAPIDTO = new GatewayAPIDTO();
                gatewayAPIDTO.setName(gatewayEvent.getName());
                gatewayAPIDTO.setVersion(gatewayEvent.getVersion());
                gatewayAPIDTO.setProvider(gatewayEvent.getProvider());
                gatewayAPIDTO.setTenantDomain(gatewayEvent.getTenantDomain());
                gatewayAPIDTO.setOverride(true);
                gatewayAPIDTO.setApiId(gatewayEvent.getApiId());
                setClientCertificatesToRemoveIntoGatewayDTO(gatewayAPIDTO);
                if (APIConstants.API_PRODUCT.equals(gatewayEvent.getApiType())) {
                    gatewayAPIDTO.setOverride(false);
                    Set<APIEvent> associatedApis = gatewayEvent.getAssociatedApis();
                    for (APIEvent associatedApi : associatedApis) {
                        if (!APIStatus.PUBLISHED.getStatus().equals(associatedApi.getApiStatus())) {
                            GatewayUtils.setCustomSequencesToBeRemoved(
                                    new API(new APIIdentifier(associatedApi.getApiProvider(),
                                            associatedApi.getApiName(),
                                            associatedApi.getApiVersion())), gatewayAPIDTO);
                            GatewayUtils
                                    .setEndpointsToBeRemoved(associatedApi.getApiName(), associatedApi.getApiVersion(),
                                            gatewayAPIDTO);
                        }
                    }
                } else {
                    if (APIConstants.APITransportType.GRAPHQL.toString().equalsIgnoreCase(gatewayEvent.getApiType())) {
                        gatewayAPIDTO.setLocalEntriesToBeRemove(
                                org.wso2.carbon.apimgt.impl.utils.GatewayUtils
                                        .addStringToList(gatewayEvent.getApiId().concat(
                                                "_graphQL"), gatewayAPIDTO.getLocalEntriesToBeRemove()));
                    }
                    GatewayUtils.setEndpointsToBeRemoved(gatewayAPIDTO.getName(), gatewayAPIDTO.getVersion(),
                            gatewayAPIDTO);
                    GatewayUtils.setCustomSequencesToBeRemoved(api, gatewayAPIDTO);
                }
                gatewayAPIDTO.setLocalEntriesToBeRemove(
                        GatewayUtils
                                .addStringToList(gatewayEvent.getApiId(), gatewayAPIDTO.getLocalEntriesToBeRemove()));
                apiGatewayAdmin.unDeployAPI(gatewayAPIDTO);
            }
        } catch (AxisFault axisFault) {
            throw new ArtifactSynchronizerException("Error while unDeploying api ", axisFault);
        }

    }

    public void cleanDeployment(String tenantDomain) throws ArtifactSynchronizerException {

        if (gatewayArtifactSynchronizerProperties.isRetrieveFromStorageEnabled()) {
            try {
                APIGatewayAdminClient apiGatewayAdmin = new APIGatewayAdminClient();
                apiGatewayAdmin.cleanDeployment(tenantDomain);
            } catch (AxisFault axisFault) {
                throw new ArtifactSynchronizerException("Error while running cleanup", axisFault);
            }
        }
    }

    private void addDeployedCertificatesToAPIAssociation(GatewayAPIDTO gatewayAPIDTO) {

        if (gatewayAPIDTO != null) {
            String apiId = gatewayAPIDTO.getApiId();
            List<String> aliasList = new ArrayList<>();
            if (gatewayAPIDTO.getClientCertificatesToBeAdd() != null) {
                for (GatewayContentDTO gatewayContentDTO : gatewayAPIDTO.getClientCertificatesToBeAdd()) {
                    aliasList.add(gatewayContentDTO.getName());
                }
            }
            CertificateDataHolder.getInstance().addApiToAliasList(apiId, aliasList);
        }
    }

    private void setClientCertificatesToRemoveIntoGatewayDTO(GatewayAPIDTO gatewayDTO) {

        if (gatewayDTO != null) {
            if (StringUtils.isNotEmpty(gatewayDTO.getApiId())) {
                List<String> certificateAliasListForAPI =
                        CertificateDataHolder.getInstance().getCertificateAliasListForAPI(gatewayDTO.getApiId());
                gatewayDTO.setClientCertificatesToBeRemove(certificateAliasListForAPI.toArray(new String[0]));
            }
        }
    }
}
