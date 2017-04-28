package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.admin.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.admin.ThrottlingPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.PolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;

public class ThrottlingPoliciesApiServiceImpl extends ThrottlingPoliciesApiService {

    private static final Logger log = LoggerFactory.getLogger(ThrottlingPoliciesApiServiceImpl.class);

    @Override public Response throttlingPoliciesAdvancedGet(String accept, String ifNoneMatch, String ifModifiedSince,
            Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.API_LEVEL;
        if (log.isDebugEnabled()) {
            log.debug("Received Advance Throttle Policy GET request");
        }
        return getAllThrottlePolicyByTier(tierLevel);
    }

    @Override public Response throttlingPoliciesAdvancedPolicyIdDelete(String policyId, String ifMatch,
            String ifUnmodifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.API_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy DELETE request with uuid: " + policyId);
        }
        return deletePolicy(policyId, tierLevel);
    }

    @Override public Response throttlingPoliciesAdvancedPolicyIdGet(String policyId, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.API_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advanced Policy Get request. Policy uuid: " + policyId);
        }
        return getPolicyByUuid(policyId, tierLevel);
    }

    @Override public Response throttlingPoliciesAdvancedPolicyIdPut(String policyId, TierDTO body, String contentType,
            String ifMatch, String ifUnmodifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.API_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy POST request " + body + " with tierLevel = " + tierLevel);
        }
        return updatePolicy(tierLevel, body);
    }

    @Override public Response throttlingPoliciesAdvancedPost(TierDTO body, String contentType, Request request)
            throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.API_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy POST request " + body + " with tierLevel = " + tierLevel);
        }
        return createPolicy(tierLevel, body);
    }

    @Override public Response throttlingPoliciesApplicationGet(String accept, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {

        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
        if (log.isDebugEnabled()) {
            log.debug("Received all Application Throttle Policy GET request");
        }
        return getAllThrottlePolicyByTier(tierLevel);
    }

    @Override public Response throttlingPoliciesApplicationPolicyIdDelete(String policyId, String ifMatch,
            String ifUnmodifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Application Policy DELETE request with uuid: " + policyId);
        }
        return deletePolicy(policyId, tierLevel);
    }

    @Override public Response throttlingPoliciesApplicationPolicyIdGet(String policyId, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Application Policy Get request. Policy uuid: " + policyId);
        }
        return getPolicyByUuid(policyId, tierLevel);
    }

    @Override public Response throttlingPoliciesApplicationPolicyIdPut(String policyId, TierDTO body,
            String contentType, String ifMatch, String ifUnmodifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Application Policy POST request " + body + " with tierLevel = " + tierLevel);
        }
        return updatePolicy(tierLevel, body);
    }

    @Override public Response throttlingPoliciesApplicationPost(TierDTO body, String contentType, Request request)
            throws NotFoundException {

        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Application Policy POST request " + body + " with tierLevel = " + tierLevel);
        }
        return createPolicy(tierLevel, body);
    }

    @Override public Response throttlingPoliciesCustomGet(String accept, String ifNoneMatch, String ifModifiedSince,
            Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override public Response throttlingPoliciesCustomPost(CustomRuleDTO body, String contentType, Request request)
            throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override public Response throttlingPoliciesCustomRuleIdDelete(String ruleId, String ifMatch,
            String ifUnmodifiedSince, Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override public Response throttlingPoliciesCustomRuleIdGet(String ruleId, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override public Response throttlingPoliciesCustomRuleIdPut(String ruleId, CustomRuleDTO body, String contentType,
            String ifMatch, String ifUnmodifiedSince, Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override public Response throttlingPoliciesSubscriptionGet(String accept, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL;
        if (log.isDebugEnabled()) {
            log.debug("Received all Subscription Throttle Policy GET request");
        }
        return getAllThrottlePolicyByTier(tierLevel);
    }

    @Override public Response throttlingPoliciesSubscriptionPolicyIdDelete(String policyId, String ifMatch,
            String ifUnmodifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Subscription Policy DELETE request with uuid: " + policyId);
        }
        return deletePolicy(policyId, tierLevel);
    }

    @Override public Response throttlingPoliciesSubscriptionPolicyIdGet(String policyId, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Subscription Policy Get request. Policy uuid: " + policyId);
        }
        return getPolicyByUuid(policyId, tierLevel);
    }

    @Override public Response throttlingPoliciesSubscriptionPolicyIdPut(String policyId, TierDTO body,
            String contentType, String ifMatch, String ifUnmodifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Subscription Policy POST request " + body + " with tierLevel = " + tierLevel);
        }
        return updatePolicy(tierLevel, body);
    }

    @Override public Response throttlingPoliciesSubscriptionPost(TierDTO body, String contentType, Request request)
            throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL;

        if (log.isDebugEnabled()) {
            log.info("Received Subscription Policy POST request " + body + " with tierLevel = " + tierLevel);
        }
        return createPolicy(tierLevel, body);
    }

    private Response getPolicyByUuid(String policyId, String tierLevel) {
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            Policy policy = apiMgtAdminService.getPolicyByUuid(policyId, tierLevel);
            return Response.status(Response.Status.OK).entity(policy).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while getting Policy. policy uuid: " + policyId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    private Response getAllThrottlePolicyByTier(String tierLevel) {
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

    private Response createPolicy(String tierLevel, TierDTO body) {
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

    private Response updatePolicy(String tierLevel, TierDTO body) {
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            Policy policy = PolicyMappingUtil.toPolicy(tierLevel, body);
            apiMgtAdminService.updatePolicy(policy);
            return Response.status(Response.Status.CREATED).entity(policy).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while adding Policy ";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    private Response deletePolicy(String policyId, String tierLevel) {
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            apiMgtAdminService.deletePolicyByUuid(policyId, tierLevel);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while deleting a Policy uuid : " + policyId;
            HashMap<String, String> paramList = new HashMap<>();
            paramList.put(APIMgtConstants.ExceptionsConstants.TIER, policyId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
