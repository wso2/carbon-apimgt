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
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
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
import org.wso2.carbon.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.GatewayPolicyArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.GatewayArtifactGenerator;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
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
@Component(
        name = "synapse.artifact.generator.service",
        immediate = true,
        service = GatewayArtifactGenerator.class
)
public class SynapseArtifactGenerator implements GatewayArtifactGenerator {

    private static final Log log = LogFactory.getLog(SynapseArtifactGenerator.class);
    private static final String GATEWAY_EXT_SEQUENCE_PREFIX = "WSO2AMGW--Ext";

    // Thread pool configuration
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 50;
    private static final long KEEP_ALIVE_TIME_MS = 60000L;
    private static final int QUEUE_CAPACITY = 1000;

    private ThreadPoolExecutor artifactThreadPoolExecutor;

    /**
     * OSGi component activation method
     */
    @Activate
    protected void activate(ComponentContext componentContext) {
        log.info("Initializing SynapseArtifactGenerator thread pool executor");

        this.artifactThreadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME_MS,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadFactory() {
                    private final AtomicInteger count = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "SynapseArtifact-" + count.getAndIncrement());
                        t.setDaemon(false);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() { // Handle rejection gracefully
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        log.warn("Thread pool queue full, executing task in caller thread");
                        super.rejectedExecution(r, executor);
                    }
                }
        );

        log.info("SynapseArtifactGenerator thread pool initialized: core=" + CORE_POOL_SIZE +
                ", max=" + MAX_POOL_SIZE + ", queue=" + QUEUE_CAPACITY);
    }

    /**
     * OSGi component deactivation method
     */
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        log.info("Deactivating SynapseArtifactGenerator component");

        if (artifactThreadPoolExecutor != null) {
            log.info("Shutting down SynapseArtifactGenerator thread pool...");

            // Shutdown the executor service
            artifactThreadPoolExecutor.shutdown();

            try {
                // Wait for existing tasks to complete within a timeout
                if (!artifactThreadPoolExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.warn("Thread pool did not terminate gracefully within the timeout, forcing shutdown");
                    artifactThreadPoolExecutor.shutdownNow();

                    // Wait for tasks to respond to being cancelled
                    if (!artifactThreadPoolExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                        log.error("Thread pool did not terminate after forced shutdown");
                    }
                }
                log.info("SynapseArtifactGenerator thread pool shutdown completed");
            } catch (InterruptedException ie) {
                log.error("Thread pool shutdown was interrupted", ie);
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
        Exception exception;
    }

    @Override
    public RuntimeArtifactDto generateGatewayArtifact(List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList)
            throws APIManagementException {

        RuntimeArtifactDto runtimeArtifactDto = new RuntimeArtifactDto();
        List<String> synapseArtifacts = new ArrayList<>();
        List<String> failedApis = new ArrayList<>();

        // Capture the tenant context
        String currentTenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String currentUsername = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        int currentTenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        List<CompletableFuture<ProcessingResult>> futures = apiRuntimeArtifactDtoList.stream()
            .map(runTimeArtifact ->  CompletableFuture.supplyAsync(() -> {
                // Set the Carbon context in the async thread
                PrivilegedCarbonContext.startTenantFlow();
                try {
                    PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    carbonContext.setTenantDomain(currentTenantDomain);
                    carbonContext.setUsername(currentUsername);
                    carbonContext.setTenantId(currentTenantId);

                    ProcessingResult result = new ProcessingResult();
                    result.apiId = runTimeArtifact.getApiId();
                    result.name = runTimeArtifact.getName();

                    if (runTimeArtifact.isFile()) {
                        String tenantDomain = runTimeArtifact.getTenantDomain();
                        String label = runTimeArtifact.getLabel();
                        Environment environment = null;
                        // TODO prevent environment not found scenarios
                        try {
                            environment = APIUtil.getEnvironments(tenantDomain).get(label);
                        } catch (APIManagementException e) {
                            result.success = false;
                            result.errorMessage = "Failed to get environment for tenant: " + tenantDomain +
                                    ", error: " + e.getMessage();
                            result.exception = e;
                            log.error("Error getting environment for API: " + result.name +
                                    " (" + result.apiId + ") in tenant: " + tenantDomain, e);
                            return result;
                        }
                        GatewayAPIDTO gatewayAPIDTO = null;
                        if (environment != null) {
                            try (InputStream artifact = (InputStream) runTimeArtifact.getArtifact()) {
                                File baseDirectory = CommonUtil.createTempDirectory(null);
                                try {
                                    String extractedFolderPath = ImportUtils.getArchivePathOfExtractedDirectory(
                                            baseDirectory.getAbsolutePath(), artifact);
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
                                        APIDTO apidto = APIMappingUtil.fromAPItoDTO(api);
                                        gatewayAPIDTO = TemplateBuilderUtil.retrieveGatewayAPIDto(api, environment,
                                                tenantDomain, apidto, extractedFolderPath, openApiDefinition);
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
                                        result.errorMessage = "GatewayAPIDTO is null";
                                    }
                                } finally {
                                    FileUtils.deleteQuietly(baseDirectory);
                                }
                            } catch (Exception e) {
                                // only do error since we need to continue for other apis
                                result.success = false;
                                result.errorMessage = "Error while creating Synapse configurations: " +
                                        e.getMessage();
                                result.exception = e;
                                log.error("Error while creating Synapse configurations for API: " +
                                        result.name + " (" + result.apiId + ")", e);
                            }
                        } else {
                            result.success = false;
                            result.errorMessage = "Environment not found for tenant: " + tenantDomain +
                                    ", label: " + label;
                        }
                    } else {
                        result.success = false;
                        result.errorMessage = "Runtime artifact is not a file";
                    }
                    return result;
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
                }, artifactThreadPoolExecutor)).collect(Collectors.toList());

        // Wait for all tasks to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));

        try {
            // Collect results of all completed tasks
            allFutures.get();
            for (int i = 0; i < futures.size(); i++) {
                try {
                    ProcessingResult result = futures.get(i).get();
                    if (result.success && result.content != null) {
                        synapseArtifacts.add(result.content);
                    } else {
                        failedApis.add(result.name + " (" + result.apiId + "): " + result.errorMessage);
                        log.warn("Failed to process API " + result.name +
                                " (" + result.apiId + "): " + result.errorMessage);
                    }
                } catch (Exception ex) {
                    APIRuntimeArtifactDto originalArtifact = apiRuntimeArtifactDtoList.get(i);
                    failedApis.add(originalArtifact.getName() + " (" + originalArtifact.getApiId() + "): "
                            + ex.getMessage());
                    log.error("Error collecting result for API: " + originalArtifact.getName() +
                            " (" + originalArtifact.getApiId() + ") ", ex);
                }
            }

        } catch (Exception e) {
            log.error("Unexpected error while waiting for artifact generation", e);
            throw new APIManagementException("Failed to process runtime artifacts", e);
        }

        if (!failedApis.isEmpty()) {
            log.warn("Error while creating Synapse configurations for APIs " +
                    String.join(", ", failedApis));
        }

        runtimeArtifactDto.setFile(false);
        runtimeArtifactDto.setArtifact(synapseArtifacts);
        return runtimeArtifactDto;
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
            List<OperationPolicy> gatewayPolicyList, String flow) throws APIManagementException {
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
