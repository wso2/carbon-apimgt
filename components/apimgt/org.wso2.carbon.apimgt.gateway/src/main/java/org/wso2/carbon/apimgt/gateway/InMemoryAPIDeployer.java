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
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.transport.dynamicconfigurations.DynamicProfileReloaderHolder;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.gateway.GatewayContentDTO;
import org.wso2.carbon.apimgt.api.gateway.GraphQLSchemaDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.service.APIGatewayAdmin;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.dto.GatewayCleanupSkipList;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactRetriever;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.impl.utils.GatewayUtils;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * This class contains the methods used to retrieve artifacts from a storage and deploy and undeploy the API in gateway.
 */
public class InMemoryAPIDeployer {

    private static final Log log = LogFactory.getLog(InMemoryAPIDeployer.class);
    ArtifactRetriever artifactRetriever;
    GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties;

    public InMemoryAPIDeployer() {

        this.artifactRetriever = ServiceReferenceHolder.getInstance().getArtifactRetriever();
        this.gatewayArtifactSynchronizerProperties = ServiceReferenceHolder
                .getInstance().getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties();
    }

    /**
     * Deploy an API in the gateway using the deployAPI method in gateway admin.
     *
     * @param gatewayEvent Gateway Deployment event.
     * @return True if API artifact retrieved from the storage and successfully deployed without any error. else false
     */
    public boolean deployAPI(DeployAPIInGatewayEvent gatewayEvent) throws ArtifactSynchronizerException {

        String apiId = gatewayEvent.getUuid();
        Set<String> gatewayLabels = gatewayEvent.getGatewayLabels();
        try {
            GatewayAPIDTO gatewayAPIDTO = retrieveArtifact(apiId, gatewayLabels);
            if (gatewayAPIDTO != null) {
                APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdmin();
                MessageContext.setCurrentMessageContext(
                        org.wso2.carbon.apimgt.gateway.utils.GatewayUtils.createAxis2MessageContext());
                unDeployAPI(apiGatewayAdmin, gatewayEvent);
                apiGatewayAdmin.deployAPI(gatewayAPIDTO);
                addDeployedCertificatesToAPIAssociation(gatewayAPIDTO);
                addDeployedGraphqlQLToAPI(gatewayAPIDTO);
                DataHolder.getInstance().addKeyManagerToAPIMapping(apiId, gatewayAPIDTO.getKeyManagers());
                DataHolder.getInstance().addAPIMetaData(gatewayEvent);
                DataHolder.getInstance().markAPIAsDeployed(gatewayAPIDTO);
                if (log.isDebugEnabled()) {
                    log.debug("API with " + apiId + " is deployed in gateway with the labels " + String.join(",",
                            gatewayLabels));
                }
                return true;
            }
        } catch (IOException | ArtifactSynchronizerException e) {
            String msg = "Error deploying " + apiId + " in Gateway";
            log.error(msg, e);
            throw new ArtifactSynchronizerException(msg, e);
        } finally {
            MessageContext.destroyCurrentMessageContext();
        }
        return true;
    }

    /**
     * Deploy an API in the gateway using the deployAPI method in gateway admin.
     *
     * @param apiId uuid of API
     * @return True if API artifact retrieved from the storage and successfully deployed without any error. else false
     */
    public boolean deployAPI(String apiId) throws ArtifactSynchronizerException {

        try {
            Set<String> gatewayLabels = gatewayArtifactSynchronizerProperties.getGatewayLabels();
            GatewayAPIDTO gatewayAPIDTO = retrieveArtifact(apiId, gatewayLabels);
            if (gatewayAPIDTO != null) {
                APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdmin();
                MessageContext.setCurrentMessageContext(
                        org.wso2.carbon.apimgt.gateway.utils.GatewayUtils.createAxis2MessageContext());
                apiGatewayAdmin.deployAPI(gatewayAPIDTO);
                addDeployedCertificatesToAPIAssociation(gatewayAPIDTO);
                addDeployedGraphqlQLToAPI(gatewayAPIDTO);
                DataHolder.getInstance().addKeyManagerToAPIMapping(apiId, gatewayAPIDTO.getKeyManagers());
                DataHolder.getInstance().markAPIAsDeployed(gatewayAPIDTO);
                if (log.isDebugEnabled()) {
                    log.debug("API with " + apiId + " is deployed in gateway with the labels " + String.join(",",
                            gatewayLabels));
                }
                return true;
            }
        } catch (IOException | ArtifactSynchronizerException e) {
            String msg = "Error deploying " + apiId + " in Gateway";
            log.error(msg, e);
            throw new ArtifactSynchronizerException(msg, e);
        } finally {
            MessageContext.destroyCurrentMessageContext();
        }
        return true;
    }

    private GatewayAPIDTO retrieveArtifact(String apiId, Set<String> gatewayLabels)
            throws ArtifactSynchronizerException {

        GatewayAPIDTO result;

        String labelString = String.join("|", gatewayLabels);
        String encodedString = Base64.encodeBase64URLSafeString(labelString.getBytes());
        if (artifactRetriever != null) {
            try {
                String gatewayRuntimeArtifact = artifactRetriever.retrieveArtifact(apiId, encodedString);
                if (StringUtils.isNotEmpty(gatewayRuntimeArtifact)) {
                    result = new Gson().fromJson(gatewayRuntimeArtifact, GatewayAPIDTO.class);
                } else {
                    String msg = "Error retrieving artifacts for API " + apiId + ". Storage returned null";
                    log.error(msg);
                    throw new ArtifactSynchronizerException(msg);
                }
            } catch (ArtifactSynchronizerException e) {
                String msg = "Error deploying " + apiId + " in Gateway";
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

    /**
     * Deploy an API in the gateway using the deployAPI method in gateway admin.
     *
     * @param assignedGatewayLabels - The labels which the gateway subscribed to
     * @param tenantDomain          tenantDomain of API.
     * @return True if all API artifacts retrieved from the storage and successfully deployed without any error. else
     * false
     */
    public boolean deployAllAPIsAtGatewayStartup(Set<String> assignedGatewayLabels, String tenantDomain)
            throws ArtifactSynchronizerException {

        boolean result = false;

        if (gatewayArtifactSynchronizerProperties.isRetrieveFromStorageEnabled()) {
            if (artifactRetriever != null) {
                try {
                    int errorCount = 0;
                    String labelString = String.join("|", assignedGatewayLabels);
                    String encodedString = Base64.encodeBase64URLSafeString(labelString.getBytes());
                    APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdmin();
                    MessageContext.setCurrentMessageContext(org.wso2.carbon.apimgt.gateway.utils.GatewayUtils.createAxis2MessageContext());
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                    List<String> gatewayRuntimeArtifacts = ServiceReferenceHolder.getInstance().getArtifactRetriever()
                            .retrieveAllArtifacts(encodedString, tenantDomain);
                    if (gatewayRuntimeArtifacts.size() == 0) {
                        return true;
                    }
                    for (String runtimeArtifact : gatewayRuntimeArtifacts) {
                        GatewayAPIDTO gatewayAPIDTO = null;
                        try {
                            if (StringUtils.isNotEmpty(runtimeArtifact)) {
                                gatewayAPIDTO = new Gson().fromJson(runtimeArtifact, GatewayAPIDTO.class);
                                log.info("Deploying synapse artifacts of " + gatewayAPIDTO.getName());
                                apiGatewayAdmin.deployAPI(gatewayAPIDTO);
                                addDeployedCertificatesToAPIAssociation(gatewayAPIDTO);
                                addDeployedGraphqlQLToAPI(gatewayAPIDTO);
                                DataHolder.getInstance().addKeyManagerToAPIMapping(gatewayAPIDTO.getApiId(),
                                        gatewayAPIDTO.getKeyManagers());
                                DataHolder.getInstance().markAPIAsDeployed(gatewayAPIDTO);
                            }
                        } catch (AxisFault axisFault) {
                            log.error("Error in deploying " + gatewayAPIDTO.getName() + " to the Gateway ", axisFault);
                            errorCount++;
                        }
                    }
                    // reload dynamic profiles to avoid delays in loading certs in mutual ssl enabled APIs upon
                    // server restart
                    DynamicProfileReloaderHolder.getInstance().reloadAllHandlers();
                    if (log.isDebugEnabled()) {
                        log.debug("APIs deployed in gateway with the labels of " + labelString);
                    }
                    result = true;
                    //Setting the result to false only if all the API deployments are failed
                    if (gatewayRuntimeArtifacts.size() == errorCount) {
                        return false;
                    }
                } catch (AxisFault e) {
                    String msg = "Error deploying APIs to the Gateway ";
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

    private void unDeployAPI(APIGatewayAdmin apiGatewayAdmin, DeployAPIInGatewayEvent gatewayEvent)
            throws AxisFault {
            if (gatewayArtifactSynchronizerProperties.isRetrieveFromStorageEnabled()) {
                GatewayAPIDTO gatewayAPIDTO = new GatewayAPIDTO();
                gatewayAPIDTO.setName(gatewayEvent.getName());
                gatewayAPIDTO.setVersion(gatewayEvent.getVersion());
                gatewayAPIDTO.setProvider(gatewayEvent.getProvider());
                gatewayAPIDTO.setTenantDomain(gatewayEvent.getTenantDomain());
                gatewayAPIDTO.setApiId(gatewayEvent.getUuid());
                setClientCertificatesToRemoveIntoGatewayDTO(gatewayAPIDTO);
                if (APIConstants.API_PRODUCT.equals(gatewayEvent.getApiType())) {
                    APIProductIdentifier apiProductIdentifier = new APIProductIdentifier(gatewayEvent.getProvider(),
                            gatewayEvent.getName(), gatewayEvent.getVersion());
                    Set<APIEvent> associatedApis = gatewayEvent.getAssociatedApis();
                    for (APIEvent associatedApi : associatedApis) {
                        GatewayUtils.setCustomSequencesToBeRemoved(apiProductIdentifier, associatedApi.getUuid(),
                                gatewayAPIDTO);
                        GatewayUtils.setEndpointsToBeRemoved(apiProductIdentifier, associatedApi.getUuid(),
                                gatewayAPIDTO);
                    }
                } else {
                    API api = new API(new APIIdentifier(gatewayEvent.getProvider(), gatewayEvent.getName(),
                            gatewayEvent.getVersion()));
                    if (APIConstants.APITransportType.GRAPHQL.toString().equalsIgnoreCase(gatewayEvent.getApiType())) {
                        gatewayAPIDTO.setLocalEntriesToBeRemove(
                                org.wso2.carbon.apimgt.impl.utils.GatewayUtils
                                        .addStringToList(gatewayEvent.getUuid().concat(
                                                "_graphQL"), gatewayAPIDTO.getLocalEntriesToBeRemove()));
                        DataHolder.getInstance().getApiToGraphQLSchemaDTOMap().remove(gatewayEvent.getUuid());
                    }
                    if (APIConstants.APITransportType.WS.toString().equalsIgnoreCase(gatewayEvent.getApiType())) {
                        org.wso2.carbon.apimgt.gateway.utils.GatewayUtils.setWebsocketEndpointsToBeRemoved(
                                gatewayAPIDTO, gatewayEvent.getTenantDomain());
                    } else {
                        GatewayUtils.setEndpointsToBeRemoved(gatewayAPIDTO.getName(), gatewayAPIDTO.getVersion(),
                                gatewayAPIDTO);
                    }

                    GatewayUtils.setCustomSequencesToBeRemoved(api, gatewayAPIDTO);
                }
                gatewayAPIDTO.setLocalEntriesToBeRemove(
                        GatewayUtils
                                .addStringToList(gatewayEvent.getUuid(), gatewayAPIDTO.getLocalEntriesToBeRemove()));
                apiGatewayAdmin.unDeployAPI(gatewayAPIDTO);
                DataHolder.getInstance().getApiToCertificatesMap().remove(gatewayEvent.getUuid());
                DataHolder.getInstance().removeKeyManagerToAPIMapping(gatewayAPIDTO.getApiId());
            }
    }

    public void unDeployAPI(DeployAPIInGatewayEvent gatewayEvent) throws ArtifactSynchronizerException {

        try {
            APIGatewayAdmin apiGatewayAdmin = new APIGatewayAdmin();
            MessageContext.setCurrentMessageContext(org.wso2.carbon.apimgt.gateway.utils.GatewayUtils.createAxis2MessageContext());
            unDeployAPI(apiGatewayAdmin, gatewayEvent);
        } catch (AxisFault axisFault) {
            throw new ArtifactSynchronizerException("Error while unDeploying api ", axisFault);
        } finally {
            MessageContext.destroyCurrentMessageContext();
        }
    }

    public void cleanDeployment(String artifactRepositoryPath) {

        File artifactRepoPath =
                Paths.get(artifactRepositoryPath, SynapseConstants.SYNAPSE_CONFIGS, SynapseConstants.DEFAULT_DIR)
                        .toFile();
        if (artifactRepoPath.exists() && artifactRepoPath.isDirectory()) {
            GatewayCleanupSkipList gatewayCleanupSkipList =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getGatewayCleanupSkipList();
            File apiPath = Paths.get(artifactRepoPath.getAbsolutePath(), "api").toFile();
            if (apiPath.exists() && apiPath.isDirectory()) {
                clean(apiPath, gatewayCleanupSkipList.getApis());
            }
            File localEntryPath = Paths.get(artifactRepoPath.getAbsolutePath(), "local-entries").toFile();
            if (localEntryPath.exists() && localEntryPath.isDirectory()) {
                clean(localEntryPath, gatewayCleanupSkipList.getLocalEntries());
            }
            File endpointPath = Paths.get(artifactRepoPath.getAbsolutePath(), "endpoints").toFile();
            if (endpointPath.exists() && endpointPath.isDirectory()) {
                clean(endpointPath, gatewayCleanupSkipList.getEndpoints());
            }
            File sequencesPath = Paths.get(artifactRepoPath.getAbsolutePath(), "sequences").toFile();
            if (sequencesPath.exists() && sequencesPath.isDirectory()) {
                clean(sequencesPath, gatewayCleanupSkipList.getSequences());
            }
        }
    }

    private void clean(File artifactRepoPath, Set<String> skippedList) {

        if (artifactRepoPath != null && artifactRepoPath.isDirectory()) {
            for (File file : Objects.requireNonNull(artifactRepoPath.listFiles())) {
                if (!skippedList.contains(file.getName())) {
                    if (!file.delete()){
                        if (log.isDebugEnabled()) {
                            log.debug("File " + file.getAbsolutePath() + " not Deleted");
                        }
                    }
                }
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
            DataHolder.getInstance().addApiToAliasList(apiId, aliasList);
        }
    }

    /**
     * Add GraphQLSchemaDTO of deployed GraphQL API to Gateway internal data holder.
     *
     * @param gatewayAPIDTO GatewayAPIDTO
     */
    private void addDeployedGraphqlQLToAPI(GatewayAPIDTO gatewayAPIDTO) {

        if (gatewayAPIDTO != null && gatewayAPIDTO.getGraphQLSchema() != null) {
            String apiId = gatewayAPIDTO.getApiId();
            SchemaParser schemaParser = new SchemaParser();
            TypeDefinitionRegistry registry = schemaParser.parse(gatewayAPIDTO.getGraphQLSchema());
            GraphQLSchema schema = UnExecutableSchemaGenerator.makeUnExecutableSchema(registry);
            GraphQLSchemaDTO schemaDTO = new GraphQLSchemaDTO(schema, registry);
            DataHolder.getInstance().addApiToGraphQLSchemaDTO(apiId, schemaDTO);
        }
    }

    private void setClientCertificatesToRemoveIntoGatewayDTO(GatewayAPIDTO gatewayDTO) {

        if (gatewayDTO != null) {
            if (StringUtils.isNotEmpty(gatewayDTO.getApiId())) {
                List<String> certificateAliasListForAPI =
                        DataHolder.getInstance().getCertificateAliasListForAPI(gatewayDTO.getApiId());
                gatewayDTO.setClientCertificatesToBeRemove(certificateAliasListForAPI.toArray(new String[0]));
            }
        }
    }

    public void reDeployAPI(String apiName, String version, String tenantDomain) throws ArtifactSynchronizerException {

        SubscriptionDataStore tenantSubscriptionStore =
                SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
        Set<String> gatewayLabels = gatewayArtifactSynchronizerProperties.getGatewayLabels();
        if (tenantSubscriptionStore != null) {
            org.wso2.carbon.apimgt.keymgt.model.entity.API retrievedAPI =
                    tenantSubscriptionStore.getApiByNameAndVersion(apiName, version);
            if (retrievedAPI != null) {
                DeployAPIInGatewayEvent deployAPIInGatewayEvent =
                        new DeployAPIInGatewayEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                                APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name(), tenantDomain,
                                retrievedAPI.getApiId(), retrievedAPI.getUuid(), gatewayLabels, apiName, version,
                                retrievedAPI.getApiProvider(),
                                retrievedAPI.getApiType(), retrievedAPI.getContext());
                deployAPI(deployAPIInGatewayEvent);
            }
        }
    }

    public void unDeployAPI(String apiName, String version, String tenantDomain) throws ArtifactSynchronizerException {

        SubscriptionDataStore tenantSubscriptionStore =
                SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
        Set<String> gatewayLabels = gatewayArtifactSynchronizerProperties.getGatewayLabels();
        if (tenantSubscriptionStore != null) {
            org.wso2.carbon.apimgt.keymgt.model.entity.API retrievedAPI =
                    tenantSubscriptionStore.getApiByNameAndVersion(apiName, version);
            if (retrievedAPI != null) {
                DeployAPIInGatewayEvent deployAPIInGatewayEvent =
                        new DeployAPIInGatewayEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                                APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name(), tenantDomain,
                                retrievedAPI.getApiId(), retrievedAPI.getUuid(), gatewayLabels, apiName, version,
                                retrievedAPI.getApiProvider(), retrievedAPI.getApiType(), retrievedAPI.getContext());
                unDeployAPI(deployAPIInGatewayEvent);
            }
        }
    }
}
