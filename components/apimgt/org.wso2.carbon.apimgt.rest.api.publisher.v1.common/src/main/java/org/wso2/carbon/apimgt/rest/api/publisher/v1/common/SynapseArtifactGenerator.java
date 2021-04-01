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
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.apimgt.impl.definitions.OAS3Parser;
import org.wso2.carbon.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.GatewayArtifactGenerator;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ImportUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public RuntimeArtifactDto generateGatewayArtifact(List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList)
            throws APIManagementException {

        RuntimeArtifactDto runtimeArtifactDto = new RuntimeArtifactDto();
        List<String> synapseArtifacts = new ArrayList<>();
        for (APIRuntimeArtifactDto runTimeArtifact : apiRuntimeArtifactDtoList) {
            if (runTimeArtifact.isFile()) {
                String tenantDomain = runTimeArtifact.getTenantDomain();
                String label = runTimeArtifact.getLabel();
                Environment environment = APIUtil.getEnvironments().get(label);
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
                                APIDefinitionValidationResponse apiDefinitionValidationResponse =
                                        ImportUtils.retrieveValidatedSwaggerDefinitionFromArchive(extractedFolderPath);
                                apiProduct.setDefinition(apiDefinitionValidationResponse.getContent());
                                gatewayAPIDTO = TemplateBuilderUtil
                                        .retrieveGatewayAPIDto(apiProduct, environment, tenantDomain,
                                                extractedFolderPath
                                        );
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
                                    String graphqlSchema =
                                            ImportUtils.retrieveValidatedGraphqlSchemaFromArchive(extractedFolderPath);
                                    api.setGraphQLSchema(graphqlSchema);
                                    GraphQLSchemaDefinition graphQLSchemaDefinition = new GraphQLSchemaDefinition();
                                    graphqlSchema = graphQLSchemaDefinition
                                            .buildSchemaWithAdditionalInfo(api, graphqlComplexityInfo);
                                    api.setGraphQLSchema(graphqlSchema);
                                    gatewayAPIDTO = TemplateBuilderUtil
                                            .retrieveGatewayAPIDto(api, environment, tenantDomain, apidto,
                                                    extractedFolderPath);
                                } else if (api.getType() != null &&
                                        (APIConstants.APITransportType.HTTP.toString().equals(api.getType())
                                                || APIConstants.API_TYPE_SOAP.equals(api.getType())
                                                || APIConstants.API_TYPE_SOAPTOREST.equals(api.getType()))) {
                                    APIDefinitionValidationResponse apiDefinitionValidationResponse = ImportUtils
                                            .retrieveValidatedSwaggerDefinitionFromArchive(extractedFolderPath);
                                    api.setSwaggerDefinition(apiDefinitionValidationResponse.getContent());
                                    gatewayAPIDTO = TemplateBuilderUtil
                                            .retrieveGatewayAPIDto(api, environment, tenantDomain, apidto,
                                                    extractedFolderPath, apiDefinitionValidationResponse);
                                } else if (api.getType() != null &&
                                        (APIConstants.APITransportType.WS.toString().equals(api.getType()) ||
                                                APIConstants.APITransportType.SSE.toString().equals(api.getType()) ||
                                                APIConstants.APITransportType.WEBSUB.toString()
                                                        .equals(api.getType()))) {
                                    APIDefinitionValidationResponse asyncApiDefinition =
                                            ImportUtils.retrieveValidatedAsyncApiDefinitionFromArchive(
                                                    extractedFolderPath);
                                    api.setAsyncApiDefinition(asyncApiDefinition.getContent());
                                    gatewayAPIDTO = TemplateBuilderUtil
                                            .retrieveGatewayAPIDtoForStreamingAPI(api, environment, tenantDomain,
                                                    apidto, extractedFolderPath);
                                }
                            }
                            if (gatewayAPIDTO != null) {
                                String content = new Gson().toJson(gatewayAPIDTO);
                                synapseArtifacts.add(content);
                            }
                        } finally {
                            FileUtils.deleteQuietly(baseDirectory);
                        }
                    } catch (APIImportExportException | IOException |
                            XMLStreamException | APITemplateException | CertificateManagementException e) {
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

    @Override
    public String getType() {

        return "Synapse";
    }

}
