/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org).
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

package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.RuntimeArtifactGeneratorUtil;
import org.wso2.carbon.apimgt.internal.service.RuntimeMetadataApiService;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.File;
import java.nio.file.Files;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

public class RuntimeMetadataApiServiceImpl implements RuntimeMetadataApiService {

    public Response runtimeMetadataGet(String xWSO2Tenant, String apiId, String gatewayLabel, String name,
                                       String version, MessageContext messageContext)
            throws APIManagementException {

        xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);
        RuntimeArtifactDto runtimeArtifactDto =
                RuntimeArtifactGeneratorUtil.generateMetadataArtifact(apiId, name, version, gatewayLabel, xWSO2Tenant);
        if (runtimeArtifactDto != null) {
            File artifact = (File) runtimeArtifactDto.getArtifact();
            StreamingOutput streamingOutput = (outputStream) -> {
                try {
                    Files.copy(artifact.toPath(), outputStream);
                } finally {
                    Files.delete(artifact.toPath());
                }
            };
            return Response.ok(streamingOutput).header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                    "attachment; filename=deployment.json").header(RestApiConstants.HEADER_CONTENT_TYPE,
                    APIConstants.APPLICATION_JSON_MEDIA_TYPE).build();
        } else {
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(RestApiUtil.getErrorDTO(ExceptionCodes.NO_API_ARTIFACT_FOUND))
                    .build();
        }
    }
}
