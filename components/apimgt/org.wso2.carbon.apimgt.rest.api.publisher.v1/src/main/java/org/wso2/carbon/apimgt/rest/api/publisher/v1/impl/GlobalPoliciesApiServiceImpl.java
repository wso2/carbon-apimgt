package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.GlobalPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.PublisherCommonUtils;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.Map;

import javax.ws.rs.core.Response;

import static org.wso2.carbon.apimgt.impl.APIConstants.UN_AUTHORIZED_ERROR_MESSAGE;

public class GlobalPoliciesApiServiceImpl implements GlobalPoliciesApiService {
    private static final Log log = LogFactory.getLog(GlobalPoliciesApiServiceImpl.class);
    public Response addGlobalPolicy(Map<String, APIOperationPoliciesDTO> requestBody, MessageContext messageContext) {
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            PublisherCommonUtils.applyGatewayGlobalPolicies(requestBody, tenantDomain);
            return Response.ok().build();
        } catch (APIManagementException e) {
            if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("User is not authorized to apply policies", e, log);
            } else {
                String errorMessage = "Error while applying gateway policy";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    private boolean isAuthorizationFailure(Exception e) {
        String errorMessage = e.getMessage();
        return errorMessage != null && errorMessage.contains(UN_AUTHORIZED_ERROR_MESSAGE);
    }
}
