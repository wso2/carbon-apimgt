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
import org.wso2.carbon.apimgt.impl.dto.APIRuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component(
        name = "microgateway.artifact.generator.service",
        immediate = true,
        service = GatewayArtifactGenerator.class
)
public class MicroGatewayArtifactGenerator implements GatewayArtifactGenerator {

    @Override
    public RuntimeArtifactDto generateGatewayArtifact(List<APIRuntimeArtifactDto> apiRuntimeArtifactDtoList)
            throws APIManagementException {

        try {
            File tempDirectory = CommonUtil.createTempDirectory(null);
            for (APIRuntimeArtifactDto apiRuntimeArtifactDto : apiRuntimeArtifactDtoList) {
                if (apiRuntimeArtifactDto.isFile()) {
                    InputStream artifact = (InputStream) apiRuntimeArtifactDto.getArtifact();
                    Path path = Paths.get(tempDirectory.getAbsolutePath(),
                            apiRuntimeArtifactDto.getApiId().concat("-").concat(apiRuntimeArtifactDto.getRevision())
                                    .concat(APIConstants.ZIP_FILE_EXTENSION));
                    FileUtils.copyInputStreamToFile(artifact, path.toFile());
                }
            }
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


    @Override
    public String getType() {

        return "Envoy";
    }
}
