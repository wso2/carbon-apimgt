package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.GatewayPolicyArtifactGeneratorUtil;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.RuntimeArtifactGeneratorUtil;
import org.wso2.carbon.apimgt.internal.service.GatewayPolicyArtifactsApiService;
import org.wso2.carbon.apimgt.internal.service.dto.SynapseArtifactListDTO;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.nio.file.Files;
import java.util.List;


public class GatewayPolicyArtifactsApiServiceImpl implements GatewayPolicyArtifactsApiService {

    public Response gatewayPolicyArtifactsGet(String xWSO2Tenant, String policyMappingUuid, String type,
            String gatewayLabel, MessageContext messageContext) throws APIManagementException {
        RuntimeArtifactDto runtimeArtifactDto = null;
        xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);
        String organization = RestApiUtil.getOrganization(messageContext);
        // Need to further discuss on this
//        if (StringUtils.isNotEmpty(organization) && !organization.equalsIgnoreCase(APIConstants.ORG_ALL_QUERY_PARAM)) {
//            xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(organization, messageContext);
//        }
//        if (StringUtils.isNotEmpty(organization) && organization.equalsIgnoreCase(APIConstants.ORG_ALL_QUERY_PARAM) &&
//                xWSO2Tenant.equalsIgnoreCase(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
//            runtimeArtifactDto = GatewayPolicyArtifactGeneratorUtil.generateRuntimeArtifact(policyMappingUuid, type, xWSO2Tenant);
//        }
        if (StringUtils.isNotEmpty(organization) && !organization.equalsIgnoreCase(APIConstants.ORG_ALL_QUERY_PARAM)) {
            runtimeArtifactDto = GatewayPolicyArtifactGeneratorUtil.generateRuntimeArtifact(policyMappingUuid, type,
                    xWSO2Tenant, gatewayLabel);
        }
        if (runtimeArtifactDto != null) {
            if (runtimeArtifactDto.isFile()) {
                File artifact = (File) runtimeArtifactDto.getArtifact();
                StreamingOutput streamingOutput = (outputStream) -> {
                    try {
                        Files.copy(artifact.toPath(), outputStream);
                    } finally {
                        Files.delete(artifact.toPath());
                    }
                };
                return Response.ok(streamingOutput)
                        .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=policy.xml")
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, APIConstants.APPLICATION_XML_MEDIA_TYPE).build();
            } else {
                SynapseArtifactListDTO synapseArtifactListDTO = new SynapseArtifactListDTO();
                if (runtimeArtifactDto.getArtifact() instanceof List) {
                    synapseArtifactListDTO.setList((List<String>) runtimeArtifactDto.getArtifact());
                    synapseArtifactListDTO.setCount(((List<String>) runtimeArtifactDto.getArtifact()).size());
                }
                return Response.ok().entity(synapseArtifactListDTO)
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, RestApiConstants.APPLICATION_JSON).build();
            }
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
