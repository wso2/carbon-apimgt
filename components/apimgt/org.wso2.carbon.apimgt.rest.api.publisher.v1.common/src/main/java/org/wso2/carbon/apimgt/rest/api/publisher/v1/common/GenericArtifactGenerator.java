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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.GatewayArtifactGenerator;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ImportUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class used to generate Synapse Artifact.
 */
@Component(
        name = "generic.artifact.generator.service",
        immediate = true,
        service = GatewayArtifactGenerator.class
)
public class GenericArtifactGenerator implements GatewayArtifactGenerator {

    private static final Log log = LogFactory.getLog(SynapseArtifactGenerator.class);

    @Override
    public RuntimeArtifactDto generateGatewayArtifact(List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList)
            throws APIManagementException {

        RuntimeArtifactDto runtimeArtifactDto = new RuntimeArtifactDto();
        List<String> artifacts = new ArrayList<>();
        for (APIRuntimeArtifactDto runTimeArtifact : apiRuntimeArtifactDtoList) {
            if (runTimeArtifact.isFile()) {
                String label = runTimeArtifact.getLabel();
                Environment environment = APIUtil.getEnvironments().get(label);
                if (environment != null) {
                    try (InputStream artifact = (InputStream) runTimeArtifact.getArtifact()) {
                        File baseDirectory = CommonUtil.createTempDirectory(null);
                        try {
                            String extractedFolderPath =
                                    ImportUtils.getArchivePathOfExtractedDirectory(baseDirectory.getAbsolutePath(),
                                            artifact);

                            String jsonContent = null;
                            String pathToArchive = extractedFolderPath + ImportExportConstants.API_FILE_LOCATION;
                            String pathToYamlFile = pathToArchive + ImportExportConstants.YAML_EXTENSION;
                            String pathToJsonFile = pathToArchive + ImportExportConstants.JSON_EXTENSION;

                            // Load yaml representation first if it is present
                            if (CommonUtil.checkFileExistence(pathToYamlFile)) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Found api definition file " + pathToYamlFile);
                                }
                                String yamlContent = FileUtils.readFileToString(new File(pathToYamlFile));
                                jsonContent = CommonUtil.yamlToJson(yamlContent);
                            } else if (CommonUtil.checkFileExistence(pathToJsonFile)) {
                                // load as a json fallback
                                if (log.isDebugEnabled()) {
                                    log.debug("Found api definition file " + pathToJsonFile);
                                }
                                jsonContent = FileUtils.readFileToString(new File(pathToJsonFile));
                            }

                            artifacts.add(jsonContent);

                        } finally {
                            FileUtils.deleteQuietly(baseDirectory);
                        }
                    } catch (Exception e) {
                        // only do error since we need to continue for other apis

                        log.error("Error while creating External gateway artifact", e);
                    }
                }
            }
        }
        runtimeArtifactDto.setFile(false);
        runtimeArtifactDto.setArtifact(artifacts);
        return runtimeArtifactDto;
    }

    @Override
    public String getType() {

        return "ExternalGWArtifact";
    }

}
