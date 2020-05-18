package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.service.APIKeyRevokeService;
import org.wso2.carbon.apimgt.keymgt.service.APIKeyRevokeServiceImpl;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.apimgt.internal.service.ApikeyApiService;
import org.wso2.carbon.apimgt.internal.service.dto.RevokeAPIKeyDTO;

import javax.ws.rs.core.Response;

public class ApikeyApiServiceImpl implements ApikeyApiService {
    private static final Log log = LogFactory.getLog(ApikeyApiServiceImpl.class);

    @Override
    public Response apikeyRevokePost(RevokeAPIKeyDTO body, MessageContext messageContext)
            throws APIManagementException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            boolean hasPermission = APIUtil.hasPermission(username, APIConstants.Permissions.APIM_ADMIN);
            if(hasPermission) {
                APIKeyRevokeService apiKeyRevokeService = APIKeyRevokeServiceImpl.getInstance();
                apiKeyRevokeService.revokeAPIKey(body.getApiKey(), body.getExpiryTime().longValue(),
                        body.getTenantId().intValue());
            } else {
                RestApiUtil.handleAuthorizationFailure("User doesn't have sufficient permissions", username,log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while checking permissions", e, log);
        }
        return Response.ok().build();
    }
}
