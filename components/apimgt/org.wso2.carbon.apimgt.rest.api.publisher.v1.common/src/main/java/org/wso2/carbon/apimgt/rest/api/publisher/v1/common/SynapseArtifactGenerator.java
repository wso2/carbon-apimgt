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
import org.osgi.service.component.annotations.Component;
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
import org.wso2.carbon.apimgt.spec.parser.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.apimgt.spec.parser.definitions.OAS3Parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public RuntimeArtifactDto generateGatewayArtifact(List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList)
            throws APIManagementException {

        RuntimeArtifactDto runtimeArtifactDto = new RuntimeArtifactDto();
        List<String> synapseArtifacts = new ArrayList<>();
        for (APIRuntimeArtifactDto runTimeArtifact : apiRuntimeArtifactDtoList) {
            if (runTimeArtifact.isFile()) {
                String tenantDomain = runTimeArtifact.getTenantDomain();
                String label = runTimeArtifact.getLabel();
                Environment environment = APIUtil.getEnvironments(tenantDomain).get(label);
                GatewayAPIDTO gatewayAPIDTO = null;
                if (environment != null) {
                    try (InputStream artifact = (InputStream) runTimeArtifact.getArtifact()) {
                        File baseDirectory = CommonUtil.createTempDirectory(null);
                        try {
                            String extractedFolderPath =
                                    ImportUtils.getArchivePathOfExtractedDirectory(baseDirectory.getAbsolutePath(),
                                            artifact);
                            if (APIConstants.API_PRODUCT.equals(runTimeArtifact.getType())) {
                                APIProductDTO apiProductDTO = ImportUtils.retrieveAPIProductDto(extractedFolderPath);
                                apiProductDTO.setId(runTimeArtifact.getApiId());
                                APIProduct apiProduct = APIMappingUtil.fromDTOtoAPIProduct(apiProductDTO,
                                        apiProductDTO.getProvider());
                                String openApiDefinition = ImportUtils.loadSwaggerFile(extractedFolderPath);
                                apiProduct.setDefinition(openApiDefinition);
                                gatewayAPIDTO = TemplateBuilderUtil.retrieveGatewayAPIDto(apiProduct, environment,
                                        tenantDomain, extractedFolderPath);
                            } else {
                                APIDTO apidto = ImportUtils.retrievedAPIDto(extractedFolderPath);
                                API api = APIMappingUtil.fromDTOtoAPI(apidto, apidto.getProvider());
                                api.setUUID(apidto.getId());
                                if (APIConstants.APITransportType.GRAPHQL.toString().equals(api.getType())) {
                                    APIDefinition parser = new OAS3Parser();
                                    SwaggerData swaggerData = new SwaggerData(api);
                                    String apiDefinition = parser.generateAPIDefinition(swaggerData);
                                    api.setSwaggerDefinition(apiDefinition);
                                    GraphqlComplexityInfo graphqlComplexityInfo = APIUtil.getComplexityDetails(api);
                                    String graphqlSchema = ImportUtils.loadGraphqlSDLFile(extractedFolderPath);
                                    api.setGraphQLSchema(graphqlSchema);
                                    GraphQLSchemaDefinition graphQLSchemaDefinition = new GraphQLSchemaDefinition();
                                    graphqlSchema = graphQLSchemaDefinition.buildSchemaWithAdditionalInfo(api,
                                            graphqlComplexityInfo);
                                    api.setGraphQLSchema(graphqlSchema);
                                    gatewayAPIDTO = TemplateBuilderUtil.retrieveGatewayAPIDto(api, environment,
                                            tenantDomain, apidto, extractedFolderPath);
                                } else if (api.getType() != null &&
                                        (APIConstants.APITransportType.HTTP.toString().equals(api.getType())
                                                || APIConstants.API_TYPE_SOAP.equals(api.getType())
                                                || APIConstants.API_TYPE_SOAPTOREST.equals(api.getType())
                                                || APIConstants.APITransportType.WEBHOOK.toString()
                                                        .equals(api.getType()))) {
                                    String openApiDefinition = ImportUtils.loadSwaggerFile(extractedFolderPath);
                                    api.setSwaggerDefinition(openApiDefinition);
                                    gatewayAPIDTO = TemplateBuilderUtil.retrieveGatewayAPIDto(api, environment,
                                            tenantDomain, apidto, extractedFolderPath, openApiDefinition);
                                } else if (api.getType() != null &&
                                        (APIConstants.APITransportType.WS.toString().equals(api.getType()) ||
                                                APIConstants.APITransportType.SSE.toString().equals(api.getType()) ||
                                                APIConstants.APITransportType.WEBSUB.toString()
                                                        .equals(api.getType()))) {
                                    String asyncApiDefinition =
                                            ImportUtils.loadAsyncApiDefinitionFromFile(extractedFolderPath);
                                    api.setAsyncApiDefinition(asyncApiDefinition);
                                    gatewayAPIDTO = TemplateBuilderUtil.retrieveGatewayAPIDtoForStreamingAPI(api,
                                            environment, tenantDomain, apidto, extractedFolderPath);
                                }
                            }
                            if (gatewayAPIDTO != null) {
                                gatewayAPIDTO.setRevision(runTimeArtifact.getRevision());
                                String content = new Gson().toJson(gatewayAPIDTO);
                                synapseArtifacts.add(content);
                            }
                        } finally {
                            FileUtils.deleteQuietly(baseDirectory);
                        }
                    } catch (Exception e) {
                        // only do error since we need to continue for other apis

                        log.error("Error while creating Synapse configurations", e);
                    }
                }
            }
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
