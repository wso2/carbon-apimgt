/*
 *
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.GatewayPolicyArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.GatewayArtifactGenerator;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ImportUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.spec.parser.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.apimgt.spec.parser.definitions.OAS3Parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class used to generate gateway artifacts for federated gateway deployments.
 */
@Component(name = "federated.artifact.generator.service", immediate = true, service = GatewayArtifactGenerator.class)
public class FederatedGatewayArtifactGenerator implements GatewayArtifactGenerator {
    private Log log = LogFactory.getLog(FederatedGatewayArtifactGenerator.class);

    @Override
    public RuntimeArtifactDto generateGatewayArtifact(List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList)
            throws APIManagementException {

        RuntimeArtifactDto runtimeArtifactDto = new RuntimeArtifactDto();
        List<API> apiList = new ArrayList<>();
        for (APIRuntimeArtifactDto runTimeArtifact : apiRuntimeArtifactDtoList) {
            String tenantDomain = runTimeArtifact.getTenantDomain();
            String label = runTimeArtifact.getLabel();
            if (log.isDebugEnabled()) {
                log.debug("Processing artifact for API: " + runTimeArtifact.getApiId() +
                        " in tenant: " + tenantDomain + " for environment label: " + label);
            }
            Environment environment = APIUtil.getEnvironments(tenantDomain).get(label);
            if (environment != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found environment: " + environment.getName() + " for label: " + label);
                }
                try (InputStream artifact = (InputStream) runTimeArtifact.getArtifact()) {
                    File baseDirectory = CommonUtil.createTempDirectory(null);
                    try {
                        String extractedFolderPath =
                                ImportUtils.getArchivePathOfExtractedDirectory(baseDirectory.getAbsolutePath(),
                                        artifact);
                        APIDTO apidto = ImportUtils.retrievedAPIDto(extractedFolderPath);
                        API api = APIMappingUtil.fromDTOtoAPI(apidto, apidto.getProvider());
                        api.setUuid(apidto.getId());
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
                        } else if (api.getType() != null &&
                                (APIConstants.APITransportType.HTTP.toString().equals(api.getType()) ||
                                        APIConstants.API_TYPE_SOAP.equals(api.getType()) ||
                                        APIConstants.API_TYPE_SOAPTOREST.equals(api.getType()) ||
                                        APIConstants.APITransportType.WEBHOOK.toString().equals(api.getType()) ||
                                        APIConstants.API_TYPE_MCP.equals(api.getType()))) {
                            String openApiDefinition = ImportUtils.loadSwaggerFile(extractedFolderPath);
                            api.setSwaggerDefinition(openApiDefinition);
                        } else if (api.getType() != null &&
                                (APIConstants.APITransportType.WS.toString().equals(api.getType()) ||
                                        APIConstants.APITransportType.SSE.toString().equals(api.getType()) ||
                                        APIConstants.APITransportType.WEBSUB.toString().equals(api.getType()))) {
                            String asyncApiDefinition =
                                    ImportUtils.loadAsyncApiDefinitionFromFile(extractedFolderPath);
                            api.setAsyncApiDefinition(asyncApiDefinition);
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Successfully processed API: " + api.getId() + " of type: " + api.getType());
                        }
                        apiList.add(api);
                    } finally {
                        FileUtils.deleteQuietly(baseDirectory);
                    }
                } catch (IOException | APIImportExportException e) {
                    throw new APIManagementException(
                            "Error while generating artifact for API: " + runTimeArtifact.getApiId() +
                                    " in environment: " + label, e);
                }
            } else {
                log.warn("Environment not found for label: " + label + " in tenant: " + tenantDomain);
            }

        }
        log.info("Successfully generated gateway artifacts for " + apiList.size() + " API(s)");
        runtimeArtifactDto.setFile(false);
        runtimeArtifactDto.setArtifact(apiList);
        return runtimeArtifactDto;

    }

    @Override
    public RuntimeArtifactDto generateGatewayPolicyArtifact(List<GatewayPolicyArtifactDto> gatewayPolicyArtifactDtoList)
            throws APIManagementException {
        throw new UnsupportedOperationException("Policy artifact generation is not supported in federated mode");
    }

    @Override
    public String getType() {
        return "Federated";
    }
}
