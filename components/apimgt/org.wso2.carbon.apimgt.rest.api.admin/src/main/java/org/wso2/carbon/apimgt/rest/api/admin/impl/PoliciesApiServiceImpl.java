package org.wso2.carbon.apimgt.rest.api.admin.impl;

import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.admin.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.admin.PoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.PolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.Request;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date =
        "2017-01-19T14:57:15.952+05:30")
public class PoliciesApiServiceImpl extends PoliciesApiService {
    private static final Logger log = LoggerFactory.getLogger(PoliciesApiService.class);

    @Override
    public Response policiesTierLevelTierLevelTierNameTierNameDelete(String tierName, String tierLevel, String
            ifMatch, String ifUnmodifiedSince, Request request) throws NotFoundException {
        log.info("Received Policy DELETE request with tierName = " + tierName);

        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            apiMgtAdminService.deletePolicy(tierName, tierLevel);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while deleting a Policy [" + tierName + "]";
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.TIER, tierName);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrieve a list of tiers for a particular tier level
     *
     * @param tierLevel   Tier level
     * @param limit       maximum number of tiers to return
     * @param offset      starting position of the pagination
     * @param accept      accept header value
     * @param ifNoneMatch If-Non-Match header value
     * @param request     msf4j request object
     * @return A list of qualifying tiers
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response policiesTierLevelTierLevelGet(String tierLevel, Integer limit, Integer offset, String accept,
                                                  String ifNoneMatch, Request request) throws NotFoundException {
        log.info("Received Policy GET request with tierLevel = " + tierLevel + ", limit = " + limit);

        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            List<Policy> policies = apiMgtAdminService.getAllPoliciesByLevel(tierLevel);
            List<TierDTO> tiers = PolicyMappingUtil.fromPoliciesToDTOs(policies);
            return Response.ok().entity(tiers).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while retrieving Policy";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Add a new policy
     *
     * @param body        Details of the policy to be added
     * @param tierLevel   Tier level
     * @param contentType Content-Type header
     * @param request     msf4j request object
     * @return Newly added policy as the response
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response policiesTierLevelTierLevelPost(TierDTO body, String tierLevel, String contentType, Request request)
            throws NotFoundException {
        log.info("Received Policy POST request " + body + " with tierLevel = " + tierLevel);

        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            Policy policy = PolicyMappingUtil.toPolicy(tierLevel, body);
            apiMgtAdminService.addPolicy(tierLevel, policy);
            return Response.status(Response.Status.CREATED).entity(policy).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while adding Policy ";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    @Override
    public Response policiesTierLevelTierLevelTierNameTierNamePut(String tierName, TierDTO body, String tierLevel,
                                                                  String contentType, String ifMatch, String
                                                                              ifUnmodifiedSince, Request request)
            throws NotFoundException {
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response policiesUpdatePermissionPost(String tierName, String tierLevel, String ifMatch, String
            ifUnmodifiedSince, TierPermissionDTO permissions, Request request) throws NotFoundException {
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
