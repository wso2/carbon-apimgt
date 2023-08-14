/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import feign.Feign;
import feign.FeignException;
import feign.Retryer;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.EnvironmentSpecificAPIPropertyDAO;
import org.wso2.carbon.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.client.ChoreoClientErrorDecoder;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.client.ChoreoClientException;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.client.ChoreoHttpClient;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.client.CloudManagerDataDTO;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.client.CloudManagerEnvTemplate;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.dto.ApiProjectDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.dto.DeploymentDescriptorDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.dto.EnvironmentDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.environmentspecificproperty.Environment;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.kmclient.ApacheFeignHttpClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component(
        name = "microgateway.artifact.generator.service",
        immediate = true,
        service = GatewayArtifactGenerator.class
)
public class MicroGatewayArtifactGenerator implements GatewayArtifactGenerator {
    private static final Log log = LogFactory.getLog(MicroGatewayArtifactGenerator.class);
    private static final EnvironmentSpecificAPIPropertyDAO environmentSpecificAPIPropertyDao =
            EnvironmentSpecificAPIPropertyDAO.getInstance();
    // Under the assumption that the choreo environment name won't be edited, once it caches the choreo environment
    // it won't be required to edit.
    // OrganizationUUID -> APIM Environment name -> Choreo Environment
    private static final Map<String, Map<String, String>> ORGANIZATION_ENVIRONMENTS = new HashMap<>();
    private static final Map<String, String> APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP;

    static {
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP = new HashMap<>();
        // Old Development Environments
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("dev-us-east-azure", "Development");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("sandbox-dev", "Development");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("Dev-Internal", "Development");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("dev-eu-north-azure", "Development");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("sandbox-dev-eu-north", "Development");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("Dev-Internal-EU-North", "Development");

        // New Development Environments
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("development-us-east-azure", "Development");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("development-sandbox-us-east-azure", "Development");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("development-internal-us-east-azure", "Development");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("development-eu-north-azure", "Development");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("development-sandbox-eu-north-azure", "Development");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("development-internal-eu-north-azure", "Development");

        // Old Production Environments
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("Production and Sandbox", "Production");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("sandbox-prod", "Production");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("Prod-Internal", "Production");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("prod-eu-north-azure", "Production");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("sandbox-prod-eu-north", "Production");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("Prod-Internal-EU-North", "Production");

        // New Production Environments
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("production-us-east-azure", "Production");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("production-sandbox-us-east-azure", "Production");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("production-internal-us-east-azure", "Production");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("production-eu-north-azure", "Production");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("production-sandbox-eu-north-azure", "Production");
        APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.put("production-internal-eu-north-azure", "Production");
    }

    private static final class CloudManagerHttpClientHolder {
        static ChoreoHttpClient cloudManagerHttpClient;

        static {
            try {
                cloudManagerHttpClient = Feign.builder()
                        .client(new ApacheFeignHttpClient(APIUtil.getHttpClient(
                                "http://localhost:4005")))
                        .encoder(new GsonEncoder())
                        .decoder(new GsonDecoder())
                        .logger(new Slf4jLogger())
                        .errorDecoder(new ChoreoClientErrorDecoder())
                        .retryer(new Retryer.Default())
                        .target(ChoreoHttpClient.class, APIManagerConfiguration.getChoreoCloudManagerEndpointURL());
            } catch (APIManagementException e) {
                cloudManagerHttpClient = null;
                log.error("Error while initializing Cloud Manager HTTP client", e);
            }
        }
    }

    @Override
    public RuntimeArtifactDto generateGatewayArtifact(List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList)
            throws APIManagementException {

        try {
            DeploymentDescriptorDto descriptorDto = new DeploymentDescriptorDto();
            Map<String, ApiProjectDto> deploymentsMap = new HashMap<>();
            Map<String, org.wso2.carbon.apimgt.api.model.Environment> gwEnvironments =
                    APIUtil.getReadOnlyEnvironments();

            // "tempDirectory" is the root artifact directory
            File tempDirectory = CommonUtil.createTempDirectory(null);
            // If cloud manager request failed once due to network issues, there is no point in trying again.
            boolean cloudManagerRequestFailed = false;
            for (APIRuntimeArtifactDto apiRuntimeArtifactDto : apiRuntimeArtifactDtoList) {
                if (apiRuntimeArtifactDto.isFile()) {
                    InputStream artifact = (InputStream) apiRuntimeArtifactDto.getArtifact();
                    String fileName = apiRuntimeArtifactDto.getApiId().concat("-").concat(apiRuntimeArtifactDto.getRevision())
                            .concat(APIConstants.ZIP_FILE_EXTENSION);
                    Path path = Paths.get(tempDirectory.getAbsolutePath(), fileName);
                    FileUtils.copyInputStreamToFile(artifact, path.toFile());

                    ApiProjectDto apiProjectDto = deploymentsMap.get(fileName);
                    if (apiProjectDto == null) {
                        apiProjectDto = new ApiProjectDto();
                        deploymentsMap.put(fileName, apiProjectDto);
                        apiProjectDto.setApiFile(fileName);
                        apiProjectDto.setEnvironments(new HashSet<>());
                        apiProjectDto.setOrganizationId(apiRuntimeArtifactDto.getOrganization());
                    }
                    // environment is unique for a revision in a deployment
                    // create new environment
                    EnvironmentDto environment = new EnvironmentDto();
                    environment.setId(apiRuntimeArtifactDto.getEnvUUID());
                    environment.setName(apiRuntimeArtifactDto.getLabel());
                    environment.setVhost(apiRuntimeArtifactDto.getVhost());
                    environment.setDeployedTimeStamp(apiRuntimeArtifactDto.getDeployedTimeStamp());
                    environment.setDeploymentType(getDeploymentType(gwEnvironments, apiRuntimeArtifactDto.getLabel()));
                    String apimEnv = environment.getName();
                    // Assumption: Already available environments in choreo cannot be deleted or renamed.
                    if (!apiRuntimeArtifactDto.isForPrivateDataPlane()
                            && !apiRuntimeArtifactDto.getOrganization()
                            .equals(APIManagerConfiguration.getChoreoSystemOrganization())) {
                        if (APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.containsKey(apimEnv)
                                && StringUtils.isNotEmpty(APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.get(apimEnv))) {
                            environment.setChoreoEnvironment(APIM_ENV_TO_CHOREO_ENV_MAPPING_FOR_CDP.get(apimEnv));
                            log.debug("Assigning Choreo Environment for the Common Data plane API Deployment " +
                                    apiRuntimeArtifactDto.getApiId() + ":"  + apiRuntimeArtifactDto.getLabel()
                                    + " - " + environment.getChoreoEnvironment());
                        }
                        // If it does not contain the mapping then we need to check for the specific organization again.
                        // This could happen in two occasions.
                        // 1. When the environment is newly added by a shared data plane user
                        // 2. When all the APIs for CDP is pulled, then for the APIs which belongs PDP org the environment
                        // will not be intuitive.
                        // TODO: (VirajSalaka) Improve logic to not to add PDP API artifacts when CDP call happens.
                        // TODO: (VirajSalaka) Handle the scenario when we enable dyanmic environments for CDP
                    } else if (!apiRuntimeArtifactDto.getOrganization()
                            .equals(APIManagerConfiguration.getChoreoSystemOrganization())
                            && StringUtils.isEmpty(environment.getChoreoEnvironment())
                            && StringUtils.isNotEmpty(APIManagerConfiguration.getChoreoCloudManagerEndpointURL())) {
                        if (!ORGANIZATION_ENVIRONMENTS.containsKey(apiRuntimeArtifactDto.getOrganization())
                                || !ORGANIZATION_ENVIRONMENTS.get(apiRuntimeArtifactDto.getOrganization())
                                .containsKey(apimEnv) && !cloudManagerRequestFailed) {
                            CloudManagerDataDTO envTemplateList = null;
                            try {
                                envTemplateList = CloudManagerHttpClientHolder.cloudManagerHttpClient
                                        .getCloudManagerEnvironmentTemplatesForOrganization(
                                                apiRuntimeArtifactDto.getOrganization());
                            } catch (FeignException e) {
                                cloudManagerRequestFailed = true;
                                // TODO: (VirajSalaka) Discuss how to respond to network issues.
                                log.error("Error while getting environment templates", e);
                            } catch (ChoreoClientException e) {
                                // TODO: (VirajSalaka) Log the error.
                                // TODO: (VirajSalaka) Discuss what to do if we get an error response
                                //  from cloud manager.
                                cloudManagerRequestFailed = true;
                                // If the PDP request is for single API artifact APIM will throw the error,
                                // else APIM will swallow the error.
                                // TODO: (VirajSalaka) Exception should not be thrown if the PDP only has Direct KeyManagers
                                log.error("Error while getting environment templates for organization "
                                        + apiRuntimeArtifactDto.getOrganization(), e);
                            }
                            Map<String, String> perOrganizationEnvironments = new HashMap<>();
                            if (envTemplateList != null && envTemplateList.getData() != null
                                    && envTemplateList.getData().size() > 0) {
                                for (CloudManagerEnvTemplate envTemplate: envTemplateList.getData()) {
                                    if (StringUtils.isNotEmpty(envTemplate.getExternalAPIMEnvironment())) {
                                        perOrganizationEnvironments.put(envTemplate.getExternalAPIMEnvironment(),
                                                envTemplate.getName());
                                        log.debug("Choreo Environment for the external APIM environment is added " +
                                                "for the organization " + apiRuntimeArtifactDto.getOrganization() +
                                                " " + envTemplate.getExternalAPIMEnvironment() + " - "
                                                + envTemplate.getName());
                                    }
                                    if (StringUtils.isNotEmpty(envTemplate.getInternalAPIMEnvironment())) {
                                        perOrganizationEnvironments.put(envTemplate.getInternalAPIMEnvironment(),
                                                envTemplate.getName());
                                        log.debug("Choreo Environment for the internal APIM environment is added " +
                                                "for the organization " + apiRuntimeArtifactDto.getOrganization() +
                                                " " + envTemplate.getExternalAPIMEnvironment() + " - "
                                                + envTemplate.getName());
                                    }
                                    if (StringUtils.isNotEmpty(envTemplate.getSandboxAPIMEnvironment())) {
                                        perOrganizationEnvironments.put(envTemplate.getSandboxAPIMEnvironment(),
                                                envTemplate.getName());
                                        log.debug("Choreo Environment for the sandbox APIM environment is added " +
                                                "for the organization " + apiRuntimeArtifactDto.getOrganization() +
                                                " " + envTemplate.getExternalAPIMEnvironment() + " - "
                                                + envTemplate.getName());
                                    }
                                }
                            }
                            ORGANIZATION_ENVIRONMENTS.put(apiRuntimeArtifactDto.getOrganization(),
                                    perOrganizationEnvironments);
                        }
                        if (StringUtils.isNotEmpty(ORGANIZATION_ENVIRONMENTS
                                .get(apiRuntimeArtifactDto.getOrganization()).get(apimEnv))) {
                            environment.setChoreoEnvironment(ORGANIZATION_ENVIRONMENTS
                                    .get(apiRuntimeArtifactDto.getOrganization())
                                    .get(apimEnv));
                        } else {
                            log.error("Choreo Environment name is not resolved for the environment " +
                                    environment.getName() + " for the organization : " +
                                    apiRuntimeArtifactDto.getOrganization());
                        }
                    }
                    apiProjectDto.getEnvironments().add(environment); // ignored if the name of the environment is same
                }
            }
            descriptorDto.setDeployments(new HashSet<>(deploymentsMap.values()));
            String descriptorFile = Paths.get(tempDirectory.getAbsolutePath(),
                    APIConstants.GatewayArtifactConstants.DEPLOYMENT_DESCRIPTOR_FILE).toString();
            CommonUtil.writeDtoToFile(descriptorFile, ExportFormat.JSON,
                    APIConstants.GatewayArtifactConstants.DEPLOYMENT_DESCRIPTOR_FILE_TYPE, descriptorDto);

            // adding env_properties.json
            Map<String, Map<String, Environment>> environmentSpecificAPIProperties =
                    getEnvironmentSpecificAPIProperties(apiRuntimeArtifactDtoList);
            String environmentSpecificAPIPropertyFile = Paths.get(tempDirectory.getAbsolutePath(),
                    APIConstants.GatewayArtifactConstants.ENVIRONMENT_SPECIFIC_API_PROPERTY_FILE).toString();
            CommonUtil.writeDtoToFile(environmentSpecificAPIPropertyFile, ExportFormat.JSON,
                    APIConstants.GatewayArtifactConstants.ENVIRONMENT_SPECIFIC_API_PROPERTY_FILE,
                    APIConstants.GatewayArtifactConstants.ENVIRONMENT_SPECIFIC_API_PROPERTY_KEY_NAME,
                    environmentSpecificAPIProperties);

            CommonUtil.archiveDirectory(tempDirectory.getAbsolutePath());
            FileUtils.deleteQuietly(tempDirectory);
            RuntimeArtifactDto runtimeArtifactDto = new RuntimeArtifactDto();
            runtimeArtifactDto.setArtifact(new File(tempDirectory.getAbsolutePath() + APIConstants.ZIP_FILE_EXTENSION));
            runtimeArtifactDto.setFile(true);
            return runtimeArtifactDto;
        } catch (APIImportExportException |
                 IOException e) {
            throw new APIManagementException("Error while Generating API artifact", e);
        }
    }

    private EnvironmentDto.DeploymentType getDeploymentType(
            Map<String, org.wso2.carbon.apimgt.api.model.Environment> gwEnvironments, String envName) {
        org.wso2.carbon.apimgt.api.model.Environment deployedEnv = gwEnvironments.get(envName);
        if (deployedEnv != null) {
            if (APIConstants.GATEWAY_ENV_TYPE_PRODUCTION.equals(deployedEnv.getType())
                    || APIConstants.GATEWAY_ENV_TYPE_HYBRID.equals(deployedEnv.getType())) {
                return EnvironmentDto.DeploymentType.PRODUCTION;
            } else { // sandbox
                return EnvironmentDto.DeploymentType.SANDBOX;
            }
        } else {
            String sandboxPrefixes = System.getenv(
                    APIConstants.GatewayArtifactConstants.ENV_DEPLOYMENT_TYPE_SANDBOX_PREFIXES);
            String prodPrefixes = System.getenv(
                    APIConstants.GatewayArtifactConstants.ENV_DEPLOYMENT_TYPE_PROD_PREFIXES);
            if (StringUtils.isBlank(sandboxPrefixes)) {
                sandboxPrefixes = APIConstants.GatewayArtifactConstants.DEPLOYMENT_TYPE_SANDBOX_PREFIXES_DEFAULT;
            }
            if (StringUtils.isBlank(prodPrefixes)) {
                prodPrefixes = APIConstants.GatewayArtifactConstants.DEPLOYMENT_TYPE_PROD_PREFIXES_DEFAULT;
            }
            for (String deploymentTypeSandboxPrefix : sandboxPrefixes.split("\\s*,\\s*")) {
                if (envName.toLowerCase().contains(deploymentTypeSandboxPrefix)) {
                    return EnvironmentDto.DeploymentType.SANDBOX;
                }
            }
            for (String deploymentTypeProdPrefix : prodPrefixes.split("\\s*,\\s*")) {
                if (envName.toLowerCase().contains(deploymentTypeProdPrefix)) {
                    return EnvironmentDto.DeploymentType.PRODUCTION;
                }
            }
            return EnvironmentDto.DeploymentType.SANDBOX;
        }
    }

    private Map<String, Map<String, Environment>> getEnvironmentSpecificAPIProperties(
            List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList) throws APIManagementException {
        List<String> apiIds = apiRuntimeArtifactDtoList.stream()
                .map(APIRuntimeArtifactDto::getApiId)
                .collect(Collectors.toList());
        return environmentSpecificAPIPropertyDao.getEnvironmentSpecificAPIPropertiesOfAPIs(apiIds);
    }

    @Override
    public String getType() {

        return "Envoy";
    }
}
