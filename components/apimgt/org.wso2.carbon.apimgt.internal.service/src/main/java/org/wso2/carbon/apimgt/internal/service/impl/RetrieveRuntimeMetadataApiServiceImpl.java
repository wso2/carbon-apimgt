package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.RuntimeArtifactGeneratorUtil;
import org.wso2.carbon.apimgt.internal.service.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.nio.file.Files;


public class RetrieveRuntimeMetadataApiServiceImpl implements RetrieveRuntimeMetadataApiService {

    public Response retrieveRuntimeMetadataGet(String dataPlaneId,
                                               MessageContext messageContext) throws APIManagementException {
        RuntimeArtifactDto runtimeMetadataDto;
        String organization = RestApiUtil.getOrganization(messageContext);
        if (StringUtils.isEmpty(organization)) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(RestApiUtil.getErrorDTO(ExceptionCodes.ORGANIZATION_NOT_FOUND))
                    .build();
        }
        if (organization.equalsIgnoreCase(APIConstants.ORG_ALL_QUERY_PARAM)) {
            runtimeMetadataDto = RuntimeArtifactGeneratorUtil.generateAllRuntimeMetadata(dataPlaneId);
        } else {
            runtimeMetadataDto
                    = RuntimeArtifactGeneratorUtil.generateAllRuntimeMetadata(organization, dataPlaneId);
        }
        if (runtimeMetadataDto != null) {
            File artifact = (File) runtimeMetadataDto.getArtifact();
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
