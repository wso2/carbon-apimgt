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

import org.apache.commons.io.FileUtils;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.EnvironmentSpecificAPIPropertyDAO;
import org.wso2.carbon.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.GatewayPolicyArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.dto.ApiProjectDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.dto.DeploymentDescriptorDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.dto.EnvironmentDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.environmentspecificproperty.Environment;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
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

    private static final EnvironmentSpecificAPIPropertyDAO environmentSpecificAPIPropertyDao =
            EnvironmentSpecificAPIPropertyDAO.getInstance();

    @Override
    public RuntimeArtifactDto generateGatewayArtifact(List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList)
            throws APIManagementException {

        try {
            if (apiRuntimeArtifactDtoList == null || apiRuntimeArtifactDtoList.isEmpty()) {
                RuntimeArtifactDto runtimeArtifactDto = new RuntimeArtifactDto();
                runtimeArtifactDto.setFile(false);
                runtimeArtifactDto.setArtifact(Collections.emptyList());
                return runtimeArtifactDto;
            }
            DeploymentDescriptorDto descriptorDto = new DeploymentDescriptorDto();
            Map<String, ApiProjectDto> deploymentsMap = new HashMap<>();

            // "tempDirectory" is the root artifact directory
            File tempDirectory = CommonUtil.createTempDirectory(null);
            for (APIRuntimeArtifactDto apiRuntimeArtifactDto : apiRuntimeArtifactDtoList) {
                if (apiRuntimeArtifactDto.isFile()) {
                    InputStream artifact = (InputStream) apiRuntimeArtifactDto.getArtifact();
                    String fileName =
                            apiRuntimeArtifactDto.getApiId().concat("-").concat(apiRuntimeArtifactDto.getRevision())
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
                    Map<String, org.wso2.carbon.apimgt.api.model.Environment> environments =
                            APIUtil.getEnvironments(apiRuntimeArtifactDto.getOrganization());
                    // environment is unique for a revision in a deployment
                    // create new environment
                    EnvironmentDto environment = new EnvironmentDto();
                    environment.setName(apiRuntimeArtifactDto.getLabel());
                    environment.setVhost(apiRuntimeArtifactDto.getVhost());
                    environment.setType(environments.get(apiRuntimeArtifactDto.getLabel()).getType());
                    environment.setDeployedTimeStamp(apiRuntimeArtifactDto.getDeployedTimeStamp());
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
        } catch (APIImportExportException | IOException e) {
            throw new APIManagementException("Error while Generating API artifact", e);
        }
    }

    /**
     * This method is not used in Microgateway.
     */
    @Override
    public RuntimeArtifactDto generateGatewayPolicyArtifact(
            List<GatewayPolicyArtifactDto> gatewayPolicyArtifactDtoList) {

        return null;
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
