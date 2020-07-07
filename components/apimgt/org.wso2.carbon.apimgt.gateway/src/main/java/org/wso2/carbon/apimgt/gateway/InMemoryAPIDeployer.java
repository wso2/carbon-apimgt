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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.gateway.service.APIGatewayAdmin;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactRetriever;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This class contains the methods used to retrieve artifacts from a storage and deploy and undeploy the API in gateway
 */
public class InMemoryAPIDeployer {

    private static Log log = LogFactory.getLog(InMemoryAPIDeployer.class);
    APIGatewayAdmin apiGatewayAdmin;
    ArtifactRetriever artifactRetriever;
    GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties;

    public InMemoryAPIDeployer() {

        this.artifactRetriever = ServiceReferenceHolder.getInstance().getArtifactRetriever();
        this.apiGatewayAdmin = new APIGatewayAdmin();
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
    public boolean deployAPI(String apiId, String gatewayLabel) {

        if (gatewayArtifactSynchronizerProperties.isRetrieveFromStorageEnabled() &&
                gatewayArtifactSynchronizerProperties.getGatewayLabels().contains(gatewayLabel)) {
            if (artifactRetriever != null) {
                try {
                    String gatewayRuntimeArtifact = artifactRetriever.retrieveArtifact(apiId, gatewayLabel,
                            APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_PUBLISH);
                    if (gatewayRuntimeArtifact != null) {
                        GatewayAPIDTO gatewayAPIDTO = new Gson().fromJson(gatewayRuntimeArtifact, GatewayAPIDTO.class);
                        apiGatewayAdmin.deployAPI(gatewayAPIDTO);
                        return true;
                    } else {
                        log.error("Error retrieving artifacts for API " + apiId + ". Storage returned null");
                    }
                } catch (IOException | ArtifactSynchronizerException e) {
                    log.error("Error deploying " + apiId + " in Gateway", e);
                }
            } else {
                log.error("Artifact retriever not found");
            }
        }
        return false;
    }

    /**
     * Deploy an API in the gateway using the deployAPI method in gateway admin
     *
     * @param assignedGatewayLabels - The labels which the gateway subscribed to
     * @return True if all API artifacts retrieved from the storage and successfully deployed without any error. else
     * false
     */
    public boolean deployAllAPIsAtGatewayStartup (Set<String> assignedGatewayLabels) {

        if (gatewayArtifactSynchronizerProperties.isRetrieveFromStorageEnabled()) {
            if (artifactRetriever != null) {
                try {
                    Iterator<String> it = assignedGatewayLabels.iterator();
                    while (it.hasNext()) {
                        String label = it.next();
                        List<String> gatewayRuntimeArtifacts = ServiceReferenceHolder
                                .getInstance().getArtifactRetriever().retrieveAllArtifacts(label);
                        for (String APIruntimeArtifact :gatewayRuntimeArtifacts){
                            GatewayAPIDTO gatewayAPIDTO = null;
                            try {
                                if (APIruntimeArtifact != null) {
                                    gatewayAPIDTO = new Gson().fromJson(APIruntimeArtifact, GatewayAPIDTO.class);
                                    log.info("Deploying synapse artifacts of " + gatewayAPIDTO.getName());
                                    apiGatewayAdmin.deployAPI(gatewayAPIDTO);
                                }
                            } catch (AxisFault axisFault) {
                                log.error("Error in deploying" + gatewayAPIDTO.getName()+ " to the Gateway ");
                                continue;
                            }
                        }
                    }
                    return true;
                } catch (ArtifactSynchronizerException | IOException e ) {
                    log.error("Error  deploying APIs to the Gateway " + e );
                }
            } else {
                log.error("Artifact retriever not found");
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
    public boolean unDeployAPI(String apiId, String gatewayLabel) {

        if (gatewayArtifactSynchronizerProperties.isRetrieveFromStorageEnabled() &&
                gatewayArtifactSynchronizerProperties.getGatewayLabels().contains(gatewayLabel)) {
            if (artifactRetriever != null) {
                try {
                    String gatewayRuntimeArtifact = artifactRetriever
                            .retrieveArtifact(apiId, gatewayLabel,
                                    APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_REMOVE);
                    if (gatewayRuntimeArtifact != null) {
                        GatewayAPIDTO gatewayAPIDTO = new Gson().fromJson(gatewayRuntimeArtifact, GatewayAPIDTO.class);
                        apiGatewayAdmin.unDeployAPI(gatewayAPIDTO);
                        return true;
                    } else {
                        log.error("Error retrieving artifacts for API " + apiId + ". Storage returned null");
                    }
                } catch (ArtifactSynchronizerException | IOException e) {
                    log.error("Error undeploying " + apiId + " in Gateway", e);
                }
            } else {
                log.error("Artifact retriever not found");
            }
        }
        return false;
    }

    /**
     * Retrieve artifacts from the storage
     *
     * @param apiId        - UUID of the API
     * @param gatewayLabel - Label of the Gateway
     * @return DTO Object that contains the information and artifacts of the API for the given label
     */
    public GatewayAPIDTO getAPIArtifact(String apiId, String gatewayLabel) {

        GatewayAPIDTO gatewayAPIDTO = null;
        if (gatewayArtifactSynchronizerProperties.getGatewayLabels().contains(gatewayLabel)) {
            if (artifactRetriever != null) {
                try {
                    String gatewayRuntimeArtifact = artifactRetriever.retrieveArtifact(apiId, gatewayLabel,
                            APIConstants.GatewayArtifactSynchronizer.GATEWAY_INSTRUCTION_PUBLISH);
                    if (gatewayRuntimeArtifact != null) {
                        gatewayAPIDTO = new Gson().fromJson(gatewayRuntimeArtifact, GatewayAPIDTO.class);
                    } else {
                        log.error("Error retrieving artifacts for API " + apiId + ". Storage returned null");
                    }
                } catch (ArtifactSynchronizerException | IOException e) {
                    log.error("Error retrieving artifacts of " + apiId + " from storage", e);
                }
            } else {
                log.error("Artifact retriever not found");
            }
        }
        return gatewayAPIDTO;
    }
}
