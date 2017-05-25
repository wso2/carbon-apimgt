package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.admin.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.admin.PoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.PolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;

public class PoliciesApiServiceImpl extends PoliciesApiService {

    private static final Logger log = LoggerFactory.getLogger(PoliciesApiServiceImpl.class);

    /**
     *
     * @param accept            Accept header value
     * @param ifNoneMatch       If-None-Match header value
     * @param ifModifiedSince   If-Modified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingAdvancedGet(String accept, String ifNoneMatch, String ifModifiedSince,
            Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.API_LEVEL;
        if (log.isDebugEnabled()) {
            log.debug("Received Advance Throttle Policy GET request");
        }
        return getAllThrottlePolicyByTier(tierLevel);
    }

    /**
     *
     * @param policyId          Uuid of the Advanced policy.
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingAdvancedPolicyIdDelete(String policyId, String ifMatch,
            String ifUnmodifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.API_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy DELETE request with uuid: " + policyId);
        }
        return deletePolicy(policyId, tierLevel);
    }

    /**
     *
     * @param policyId          Uuid of the Advanced policy.
     * @param ifNoneMatch       If-None-Match header value
     * @param ifModifiedSince   If-Modified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingAdvancedPolicyIdGet(String policyId, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.API_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advanced Policy Get request. Policy uuid: " + policyId);
        }
        return getPolicyByUuid(policyId, tierLevel);
    }

    /**
     *
     * @param policyId          Uuid of the Advanced policy.
     * @param body              DTO object including the Policy meta information
     * @param contentType       Content-Type header value
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingAdvancedPolicyIdPut(String policyId, TierDTO body, String contentType,
            String ifMatch, String ifUnmodifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.API_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy POST request " + body + " with tierLevel = " + tierLevel);
        }
        return updatePolicy(policyId, tierLevel, body);
    }

    /**
     *
     * @param body              DTO object including the Policy meta information
     * @param contentType       Content-Type header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingAdvancedPost(TierDTO body, String contentType, Request request)
            throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.API_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy POST request " + body + " with tierLevel = " + tierLevel);
        }
        return createPolicy(tierLevel, body);
    }

    /**
     * @param accept            Accept header value
     * @param ifNoneMatch       If-None-Match header value
     * @param ifModifiedSince   If-Modified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingApplicationGet(String accept, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
        if (log.isDebugEnabled()) {
            log.debug("Received Advance Throttle Policy GET request");
        }
        return getAllThrottlePolicyByTier(tierLevel);
    }

    /**
     *
     * @param policyId          Uuid of the Application policy.
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingApplicationPolicyIdDelete(String policyId, String ifMatch,
            String ifUnmodifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy DELETE request with uuid: " + policyId);
        }
        return deletePolicy(policyId, tierLevel);
    }

    /**
     *
     * @param policyId          Uuid of the Application policy
     * @param ifNoneMatch       If-None-Match header value
     * @param ifModifiedSince   If-Modified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingApplicationPolicyIdGet(String policyId, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advanced Policy Get request. Policy uuid: " + policyId);
        }
        return getPolicyByUuid(policyId, tierLevel);
    }

    /**
     *
     * @param policyId          Uuid of the policy.
     * @param body              DTO object including the Policy meta information
     * @param contentType       Content-Type header value
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingApplicationPolicyIdPut(String policyId, TierDTO body,
            String contentType, String ifMatch, String ifUnmodifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy POST request " + body + " with tierLevel = " + tierLevel);
        }
        return updatePolicy(policyId, tierLevel, body);
    }

    /**
     *
     * @param body              DTO object including the Policy meta information
     * @param contentType       Content-Type header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingApplicationPost(TierDTO body, String contentType, Request request)
            throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy POST request " + body + " with tierLevel = " + tierLevel);
        }
        return createPolicy(tierLevel, body);
    }

    /**
     *
     * @param accept            Accept header value
     * @param ifNoneMatch       If-None-Match header value
     * @param ifModifiedSince   If-Modified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingCustomGet(String accept, String ifNoneMatch, String ifModifiedSince,
            Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    /**
     *
     * @param body              DTO object including the Policy meta information
     * @param contentType       Content-Type header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingCustomPost(CustomRuleDTO body, String contentType, Request request)
            throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    /**
     *
     * @param ruleId            Uuid of the custom rule.
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingCustomRuleIdDelete(String ruleId, String ifMatch,
            String ifUnmodifiedSince, Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    /**
     *
     * @param ruleId            Uuid of the custom rule
     * @param ifNoneMatch       If-None-Match header value
     * @param ifModifiedSince   If-Modified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingCustomRuleIdGet(String ruleId, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    /**
     *
     * @param ruleId            Uuid of the custom rule
     * @param body              DTO object including the Policy meta information
     * @param contentType       Content-Type header value
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingCustomRuleIdPut(String ruleId, CustomRuleDTO body, String contentType,
            String ifMatch, String ifUnmodifiedSince, Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    /**
     *
     * @param accept            Accept header value
     * @param ifNoneMatch       If-None-Match header value
     * @param ifModifiedSince   If-Modified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingSubscriptionGet(String accept, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL;
        if (log.isDebugEnabled()) {
            log.debug("Received Advance Throttle Policy GET request");
        }
        return getAllThrottlePolicyByTier(tierLevel);
    }

    /**
     *
     * @param policyId          Uuid of the Subscription policy.
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingSubscriptionPolicyIdDelete(String policyId, String ifMatch,
            String ifUnmodifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy DELETE request with uuid: " + policyId);
        }
        return deletePolicy(policyId, tierLevel);
    }

    /**
     *
     * @param policyId          Uuid of the Subscription policy
     * @param ifNoneMatch       If-None-Match header value
     * @param ifModifiedSince   If-Modified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingSubscriptionPolicyIdGet(String policyId, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advanced Policy Get request. Policy uuid: " + policyId);
        }
        return getPolicyByUuid(policyId, tierLevel);
    }

    /**
     *
     * @param policyId          Uuid of the Subscription policy.
     * @param body              DTO object including the Policy meta information
     * @param contentType       Content-Type header value
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingSubscriptionPolicyIdPut(String policyId, TierDTO body,
            String contentType, String ifMatch, String ifUnmodifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy POST request " + body + " with tierLevel = " + tierLevel);
        }
        return updatePolicy(policyId, tierLevel, body);
    }

    /**
     *
     * @param body              DTO object including the Policy meta information
     * @param contentType       Content-Type header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingSubscriptionPost(TierDTO body, String contentType, Request request)
            throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy POST request " + body + " with tierLevel = " + tierLevel);
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
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
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
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
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
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    private Response updatePolicy(String policyId, String tierLevel, TierDTO body) {
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            Policy policy = PolicyMappingUtil.toPolicy(tierLevel, body);
            policy.setUuid(policyId);
            apiMgtAdminService.updatePolicy(policy);
            return Response.status(Response.Status.CREATED).entity(policy).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while adding Policy ";
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
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
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil
                    .getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
