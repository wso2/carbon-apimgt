package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;


import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;

import java.io.InputStream;

import org.wso2.carbon.apimgt.rest.api.admin.mappings.PolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-19T14:57:15.952+05:30")
public class PoliciesApiServiceImpl extends PoliciesApiService {
    private static final Logger log = LoggerFactory.getLogger(PoliciesApiService.class);

    @Override
    public Response policiesTierLevelDelete(String tierName, String tierLevel, String ifMatch, String ifUnmodifiedSince)
            throws NotFoundException {
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response policiesTierLevelGet(String tierLevel, Integer limit, Integer offset, String accept,
                                         String ifNoneMatch) throws NotFoundException {

        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            Policy policy = apiMgtAdminService.getPolicy(tierLevel, "");
            TierDTO tierDTO = PolicyMappingUtil.fromPolicyToDTO(policy);
            return Response.ok().entity(tierDTO).build();
        } catch (APIManagementException e) {
            String msg = "Error occurred while retrieving Policy";
            RestApiUtil.handleInternalServerError(msg, e, log);
        }

        return null;
    }
    @Override
    public Response policiesTierLevelPost(TierDTO body, String tierLevel, String contentType) throws NotFoundException {
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();

            Policy policy = PolicyMappingUtil.toPolicy(body);

            apiMgtAdminService.addPolicy(tierLevel, policy);


        } catch (APIManagementException e) {
            e.printStackTrace();
        }
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response policiesTierLevelPut(String tierName, TierDTO body, String tierLevel, String contentType,
                                         String ifMatch, String ifUnmodifiedSince) throws NotFoundException {
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response policiesUpdatePermissionPost(String tierName, String tierLevel, String ifMatch,
                                                 String ifUnmodifiedSince, TierPermissionDTO permissions)
            throws NotFoundException {
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
