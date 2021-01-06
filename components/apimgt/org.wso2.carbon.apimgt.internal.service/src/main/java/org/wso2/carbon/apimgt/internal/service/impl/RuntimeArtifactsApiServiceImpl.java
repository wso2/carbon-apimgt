/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.RuntimeArtifactGeneratorUtil;
import org.wso2.carbon.apimgt.internal.service.RuntimeArtifactsApiService;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.ws.rs.core.Response;

/**
 * Runtime Artifact Service implementation.
 */
public class RuntimeArtifactsApiServiceImpl implements RuntimeArtifactsApiService {

    public Response runtimeArtifactsGet(String apiId, String gatewayLabel, String type, MessageContext messageContext)
            throws APIManagementException {

        RuntimeArtifactDto runtimeArtifactDto =
                RuntimeArtifactGeneratorUtil.generateRuntimeArtifact(apiId, gatewayLabel, type);
        if (runtimeArtifactDto != null) {
            if (runtimeArtifactDto.isFile()) {
                File artifact = (File) runtimeArtifactDto.getArtifact();
                try (FileInputStream fileInputStream = new FileInputStream(artifact)) {
                    return Response.ok(fileInputStream).header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                            "attachment; filename=apis.zip").header(RestApiConstants.HEADER_CONTENT_TYPE,
                            APIConstants.APPLICATION_ZIP).build();
                } catch (IOException e) {
                    throw new APIManagementException("Error while sending api achieve", e);
                }
            } else {
                return Response.ok().entity(runtimeArtifactDto.getArtifact()).build();
            }
        }
        return Response.noContent().build();
    }
}
