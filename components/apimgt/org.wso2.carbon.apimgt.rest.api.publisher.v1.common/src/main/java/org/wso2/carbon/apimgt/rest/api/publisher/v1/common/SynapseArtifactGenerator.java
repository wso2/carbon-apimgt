/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common;

import com.google.gson.Gson;
import org.apache.axiom.om.OMElement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.gateway.GatewayContentDTO;
import org.wso2.carbon.apimgt.api.gateway.GatewayPolicyDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.GatewayPolicyArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.GatewayArtifactGenerator;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ImportUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerDTO;
import org.wso2.carbon.apimgt.spec.parser.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.apimgt.spec.parser.definitions.OAS3Parser;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class used to generate Synapse Artifact.
 */
public class SynapseArtifactGenerator implements GatewayArtifactGenerator {

    private static final Log log = LogFactory.getLog(SynapseArtifactGenerator.class);
    private static final String GATEWAY_EXT_SEQUENCE_PREFIX = "WSO2AMGW--Ext";
    private ThreadPoolExecutor artifactThreadPoolExecutor;
    private int corePoolSize;
    private int maxPoolSize;
    private long keepAliveTime;
    private int queueCapacity;

    public void initialize() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        // Thread pool configuration
        this.corePoolSize = Integer.parseInt(
                config.getFirstProperty(APIConstants.SynapseArtifactGenerator.CORE_POOL_SIZE));
        this.maxPoolSize = Integer.parseInt(
                config.getFirstProperty(APIConstants.SynapseArtifactGenerator.MAX_POOL_SIZE));
        this.keepAliveTime = Long.parseLong(
                config.getFirstProperty(APIConstants.SynapseArtifactGenerator.KEEP_ALIVE_TIME_MS));
        this.queueCapacity = Integer.parseInt(
                config.getFirstProperty(APIConstants.SynapseArtifactGenerator.QUEUE_CAPACITY));
        this.artifactThreadPoolExecutor = createArtifactThreadPool();
    }

    private ThreadPoolExecutor createArtifactThreadPool() {

        log.debug("Initializing SynapseArtifactGenerator thread pool : core=" + corePoolSize +
                ", max=" + maxPoolSize + ", keepAliveMs=" + keepAliveTime + ", queue=" + queueCapacity);
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                new ThreadFactory() {
                    private final AtomicInteger count = new AtomicInteger(1);

                    @Override
                    public Thread newThread(@NotNull Runnable r) {
                        Thread t = new Thread(r, "SynapseArtifact-" + count.getAndIncrement());
                        t.setDaemon(false);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() { // Handle rejection gracefully
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        log.warn("Thread pool queue full, executing task in caller thread. Active threads: "
                                + executor.getActiveCount() + ", Queue size: " + executor.getQueue().size());
                        super.rejectedExecution(r, executor);
                    }
                }
        );
    }

    public void shutdown() {

        if (artifactThreadPoolExecutor != null) {
            log.debug("Shutting down SynapseArtifactGenerator thread pool...");
            // Shutdown the executor service
            artifactThreadPoolExecutor.shutdown();
            try {
                // Wait for existing tasks to complete within a timeout
                if (artifactThreadPoolExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.debug("SynapseArtifactGenerator thread pool shutdown completed gracefully.");
                } else {
                    log.debug("Thread pool did not terminate gracefully, forcing shutdown...");
                    artifactThreadPoolExecutor.shutdownNow();
                    // Wait again for tasks to respond to cancellation
                    if (artifactThreadPoolExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                        log.debug("SynapseArtifactGenerator thread pool shutdown completed after forcing.");
                    } else {
                        log.error("Thread pool did not terminate even after forced shutdown.");
                    }
                }
            } catch (InterruptedException ie) {
                log.error("Thread pool shutdown was interrupted, forcing immediate shutdown.", ie);
                artifactThreadPoolExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    private static class ProcessingResult {
        String apiId;
        String name;
        String content;
        boolean success = false;
        String errorMessage;
    }

    @Override
    public RuntimeArtifactDto generateGatewayArtifact(List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList)
            throws APIManagementException {

        RuntimeArtifactDto runtimeArtifactDto = new RuntimeArtifactDto();
        List<String> synapseArtifacts = new ArrayList<>();
        List<String> failedApis = new ArrayList<>();
        Map<String, Environment> environments = APIUtil.getEnvironments();

        // Capture the tenant context
        String currentTenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String currentUsername = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        int currentTenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        List<CompletableFuture<ProcessingResult>> futures = apiRuntimeArtifactDtoList.stream()
                .map(runTimeArtifact -> CompletableFuture.supplyAsync(() -> {
                    // Set the Carbon context in the async thread
                    PrivilegedCarbonContext.startTenantFlow();
                    try {
                        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                        carbonContext.setTenantDomain(currentTenantDomain);
                        carbonContext.setUsername(currentUsername);
                        carbonContext.setTenantId(currentTenantId);
                        return processSingleArtifact(runTimeArtifact, environments);
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }, artifactThreadPoolExecutor)).collect(Collectors.toList());
        // Wait for all tasks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        for (CompletableFuture<ProcessingResult> future : futures) {
            try {
                ProcessingResult result = future.get();
                if (result.success && result.content != null) {
                    synapseArtifacts.add(result.content);
                } else {
                    failedApis.add(result.name + " (" + result.apiId + "): " + result.errorMessage);
                }
            } catch (Exception e) {
                log.error("Unexpected error collecting future result.", e);
            }
        }
        if (!failedApis.isEmpty()) {
            log.warn("Failed to create Synapse configurations for the following APIs: " + String.join(", ",
                    failedApis));
        }
        runtimeArtifactDto.setFile(false);
        runtimeArtifactDto.setArtifact(synapseArtifacts);
        return runtimeArtifactDto;
    }

    private ProcessingResult processSingleArtifact(APIRuntimeArtifactDto runTimeArtifact,
                                                   Map<String, Environment> environments) {

        ProcessingResult result = new ProcessingResult();
        result.apiId = runTimeArtifact.getApiId();
        result.name = runTimeArtifact.getName();
        if (!runTimeArtifact.isFile()) {
            result.success = false;
            result.errorMessage = "Runtime artifact is not a file";
            return result;
        }
        String label = runTimeArtifact.getLabel();
        Environment environment = environments.get(label);
        if (environment == null) {
            result.success = false;
            result.errorMessage = "Environment not found for label: " + label;
            return result;
        }
        try (InputStream artifact = (InputStream) runTimeArtifact.getArtifact()) {
            GatewayAPIDTO gatewayAPIDTO = null;
            File baseDirectory = CommonUtil.createTempDirectory(null);
            try {
                String extractedFolderPath = ImportUtils.getArchivePathOfExtractedDirectory(
                        baseDirectory.getAbsolutePath(), artifact);
                String tenantDomain = runTimeArtifact.getTenantDomain();
                if (APIConstants.API_PRODUCT.equals(runTimeArtifact.getType())) {
                    APIProductDTO apiProductDTO = ImportUtils.retrieveAPIProductDto(
                            extractedFolderPath);
                    apiProductDTO.setId(runTimeArtifact.getApiId());
                    APIProduct apiProduct = APIMappingUtil.fromDTOtoAPIProduct(apiProductDTO,
                            apiProductDTO.getProvider());
                    String openApiDefinition = ImportUtils.loadSwaggerFile(extractedFolderPath);
                    apiProduct.setDefinition(openApiDefinition);
                    gatewayAPIDTO = TemplateBuilderUtil.retrieveGatewayAPIDto(apiProduct,
                            environment, tenantDomain, extractedFolderPath, openApiDefinition);
                } else if (APIConstants.API_TYPE_MCP.equals(runTimeArtifact.getType())) {
                    MCPServerDTO mcpServerDTO = ImportUtils.retrievedMCPDto(
                            extractedFolderPath);
                    API api = APIMappingUtil.fromMCPServerDTOtoAPI(mcpServerDTO,
                            mcpServerDTO.getProvider());
                    String openApiDefinition = ImportUtils.loadSwaggerFile(extractedFolderPath);
                    api.setSwaggerDefinition(openApiDefinition);
                    gatewayAPIDTO = TemplateBuilderUtil.retrieveGatewayAPIDto(api, environment,
                            tenantDomain, null, extractedFolderPath, openApiDefinition);
                } else {
                    APIDTO apidto = ImportUtils.retrievedAPIDto(extractedFolderPath);
                    API api = APIMappingUtil.fromDTOtoAPI(apidto, apidto.getProvider());
                    api.setUUID(apidto.getId());
                    if (APIConstants.APITransportType.GRAPHQL.toString()
                            .equals(api.getType())) {
                        APIDefinition parser = new OAS3Parser();
                        SwaggerData swaggerData = new SwaggerData(api);
                        String apiDefinition = parser.generateAPIDefinition(swaggerData);
                        api.setSwaggerDefinition(apiDefinition);
                        GraphqlComplexityInfo graphqlComplexityInfo = APIUtil
                                .getComplexityDetails(api);
                        String graphqlSchema = ImportUtils.loadGraphqlSDLFile(
                                extractedFolderPath);
                        api.setGraphQLSchema(graphqlSchema);
                        GraphQLSchemaDefinition graphQLSchemaDefinition = new
                                GraphQLSchemaDefinition();
                        graphqlSchema = graphQLSchemaDefinition.buildSchemaWithAdditionalInfo(
                                api, graphqlComplexityInfo);
                        api.setGraphQLSchema(graphqlSchema);
                        gatewayAPIDTO = TemplateBuilderUtil.retrieveGatewayAPIDto(api,
                                environment, tenantDomain, apidto, extractedFolderPath);
                    } else if (api.getType() != null && (APIConstants.APITransportType.HTTP
                            .toString().equals(api.getType()) || APIConstants.API_TYPE_SOAP
                            .equals(api.getType()) || APIConstants.API_TYPE_SOAPTOREST
                            .equals(api.getType()) || APIConstants.APITransportType.WEBHOOK
                            .toString().equals(api.getType()) || APIConstants.API_TYPE_MCP
                            .equals(api.getType()))) {
                        String openApiDefinition = ImportUtils.loadSwaggerFile(
                                extractedFolderPath);
                        api.setSwaggerDefinition(openApiDefinition);
                        gatewayAPIDTO = TemplateBuilderUtil.retrieveGatewayAPIDto(api,
                                environment, tenantDomain, apidto, extractedFolderPath,
                                openApiDefinition);
                    } else if (api.getType() != null
                            && (APIConstants.APITransportType.WS.toString().equals(
                            api.getType())
                            || APIConstants.APITransportType.SSE.toString().equals(
                            api.getType())
                            || APIConstants.APITransportType.WEBSUB.toString().equals(
                            api.getType()))) {
                        String asyncApiDefinition = ImportUtils.loadAsyncApiDefinitionFromFile(
                                extractedFolderPath);
                        api.setAsyncApiDefinition(asyncApiDefinition);
                        gatewayAPIDTO = TemplateBuilderUtil
                                .retrieveGatewayAPIDtoForStreamingAPI(api, environment,
                                        tenantDomain, apidto, extractedFolderPath);
                    }
                }
                if (gatewayAPIDTO != null) {
                    gatewayAPIDTO.setRevision(runTimeArtifact.getRevision());
                    result.content = new Gson().toJson(gatewayAPIDTO);
                    result.success = true;
                } else {
                    result.success = false;
                    result.errorMessage = "Generated GatewayAPIDTO was null";
                }
            } finally {
                FileUtils.deleteQuietly(baseDirectory);
            }
        } catch (Exception e) {
            // only do error since we need to continue for other apis
            // only do error since we need to continue for other apis.
            log.error("Error creating Synapse configurations for API: " + result.name + " ("
                    + result.apiId + ")", e);
            result.success = false;
            result.errorMessage = e.getMessage();
        }
        return result;
    }


    /**
     * Generate gateway policy artifact.
     *
     * @param gatewayPolicyArtifactDtoList list of gateway policy artifacts
     * @return RuntimeArtifactDto runtime artifact
     * @throws APIManagementException
     */
    @Override
    public RuntimeArtifactDto generateGatewayPolicyArtifact(
            List<GatewayPolicyArtifactDto> gatewayPolicyArtifactDtoList) throws APIManagementException {

        RuntimeArtifactDto runtimeArtifactDto = new RuntimeArtifactDto();
        List<String> synapseArtifacts = new ArrayList<>();
        for (GatewayPolicyArtifactDto gatewayPolicyArtifactDto : gatewayPolicyArtifactDtoList) {
            GatewayPolicyDTO gatewayPolicyDTO = new GatewayPolicyDTO();
            gatewayPolicyDTO.setTenantDomain(gatewayPolicyArtifactDto.getTenantDomain());
            GatewayContentDTO gatewayInContentDTO = retrieveGatewayPolicySequence(
                    gatewayPolicyArtifactDto.getGatewayPolicyDataList(),
                    gatewayPolicyArtifactDto.getGatewayPolicyList(), APIConstants.OPERATION_SEQUENCE_TYPE_REQUEST);
            if (gatewayInContentDTO != null) {
                gatewayPolicyDTO.setGatewayPolicySequenceToBeAdded(
                        TemplateBuilderUtil.addGatewayContentToList(gatewayInContentDTO,
                                gatewayPolicyDTO.getGatewayPolicySequenceToBeAdded()));
            }
            GatewayContentDTO gatewayOutContentDTO = retrieveGatewayPolicySequence(
                    gatewayPolicyArtifactDto.getGatewayPolicyDataList(),
                    gatewayPolicyArtifactDto.getGatewayPolicyList(), APIConstants.OPERATION_SEQUENCE_TYPE_RESPONSE);
            if (gatewayOutContentDTO != null) {
                gatewayPolicyDTO.setGatewayPolicySequenceToBeAdded(
                        TemplateBuilderUtil.addGatewayContentToList(gatewayOutContentDTO,
                                gatewayPolicyDTO.getGatewayPolicySequenceToBeAdded()));
            }
            GatewayContentDTO gatewayFaultContentDTO = retrieveGatewayPolicySequence(
                    gatewayPolicyArtifactDto.getGatewayPolicyDataList(),
                    gatewayPolicyArtifactDto.getGatewayPolicyList(), APIConstants.OPERATION_SEQUENCE_TYPE_FAULT);
            if (gatewayFaultContentDTO != null) {
                gatewayPolicyDTO.setGatewayPolicySequenceToBeAdded(
                        TemplateBuilderUtil.addGatewayContentToList(gatewayFaultContentDTO,
                                gatewayPolicyDTO.getGatewayPolicySequenceToBeAdded()));
            }
            String content = new Gson().toJson(gatewayPolicyDTO);
            synapseArtifacts.add(content);
        }
        runtimeArtifactDto.setFile(false);
        runtimeArtifactDto.setArtifact(synapseArtifacts);
        return runtimeArtifactDto;
    }

    private static GatewayContentDTO retrieveGatewayPolicySequence(List<OperationPolicyData> gatewayPolicyDataList,
                                                                   List<OperationPolicy> gatewayPolicyList, String flow)
            throws APIManagementException {

        GatewayContentDTO gatewayPolicySequenceContentDto = new GatewayContentDTO();

        String policySequence;
        String seqExt = GATEWAY_EXT_SEQUENCE_PREFIX + SynapsePolicyAggregator.getSequenceExtensionFlow(flow);
        try {
            policySequence = SynapsePolicyAggregator.generateGatewayPolicySequenceForPolicyMapping(
                    gatewayPolicyDataList, gatewayPolicyList, flow, seqExt);
        } catch (IOException e) {
            throw new APIManagementException(e);
        }

        if (StringUtils.isNotEmpty(policySequence)) {
            try {
                OMElement omElement = APIUtil.buildOMElement(new ByteArrayInputStream(policySequence.getBytes()));
                if (omElement != null) {
                    if (omElement.getAttribute(new QName("name")) != null) {
                        omElement.getAttribute(new QName("name")).setAttributeValue(seqExt);
                    }
                    gatewayPolicySequenceContentDto.setName(seqExt);
                    gatewayPolicySequenceContentDto.setContent(APIUtil.convertOMtoString(omElement));
                    return gatewayPolicySequenceContentDto;
                }
            } catch (APIManagementException | XMLStreamException e) {
                throw new APIManagementException(e);
            }
        }
        return null;
    }

    @Override
    public String getType() {

        return "Synapse";
    }

}
