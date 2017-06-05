package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.admin.PoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.AdvancedThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.ApplicationThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.SubscriptionThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.Request;
import sun.misc.UUDecoder;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        if (log.isDebugEnabled()) {
            log.debug("Received Advance Throttle Policy GET request");
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            List<APIPolicy> policies = apiMgtAdminService.getAllAdvancePolicies();
            AdvancedThrottlePolicyListDTO advancedThrottlePolicyListDTO = AdvancedThrottlePolicyMappingUtil
                    .fromAPIPolicyArrayToListDTO(policies);
            return Response.ok().entity(advancedThrottlePolicyListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while retrieving Advance Policies";
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
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
        if (log.isDebugEnabled()) {
            log.info("Received Advanced Policy Get request. Policy uuid: " + policyId);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            APIPolicy apiPolicy = apiMgtAdminService.getAPIPolicyByUuid(policyId);
            AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyToDTO(apiPolicy);
            return Response.status(Response.Status.OK).entity(apiPolicy).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while getting Advanced Policy. policy uuid: " + policyId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
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
    @Override
    public Response policiesThrottlingAdvancedPolicyIdPut(String policyId, AdvancedThrottlePolicyDTO body,
            String contentType, String ifMatch, String ifUnmodifiedSince, Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.API_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy PUT request " + body + " with tierLevel = " + tierLevel);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            APIPolicy apiPolicy = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyDTOToPolicy(body);
            apiPolicy.setUuid(policyId);
            apiMgtAdminService.updateAPIPolicy(apiPolicy);
            return Response.status(Response.Status.CREATED).entity(apiPolicy).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while updating Advanced Policy. policy uuid: " + policyId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();

        }
    }

    /**
     *
     * @param body              DTO object including the Policy meta information
     * @param contentType       Content-Type header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingAdvancedPost(AdvancedThrottlePolicyDTO body, String contentType,
            Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.API_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy POST request " + body + " with tierLevel = " + tierLevel);
        }
//        return createPolicy(tierLevel, body);
        return null;
    }

    @Override public Response policiesThrottlingApplicationGet(String accept, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {

        if (log.isDebugEnabled()) {
            log.debug("Received Application Throttle Policy GET request");
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            List<ApplicationPolicy> policies = apiMgtAdminService.getAllApplicationPolicies();
            ApplicationThrottlePolicyListDTO applicationThrottlePolicyListDTO = ApplicationThrottlePolicyMappingUtil
                    .fromApplicationPolicyArrayToListDTO(policies);
            return Response.ok().entity(applicationThrottlePolicyListDTO).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while retrieving Application Policies";
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    @Override public Response policiesThrottlingApplicationPolicyIdDelete(String policyId, String ifMatch,
            String ifUnmodifiedSince, Request request) throws NotFoundException {

        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy DELETE request with uuid: " + policyId);
        }
        return deletePolicy(policyId, tierLevel);
    }

    @Override public Response policiesThrottlingApplicationPolicyIdGet(String policyId, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {

        if (log.isDebugEnabled()) {
            log.info("Received Application Policy Get request. Policy uuid: " + policyId);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            ApplicationPolicy applicationPolicy = apiMgtAdminService.getApplicationPolicyByUuid(policyId);
            ApplicationThrottlePolicyMappingUtil.fromApplicationThrottlePolicyToDTO(applicationPolicy);
            return Response.status(Response.Status.OK).entity(applicationPolicy).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while getting Application Policy. policy uuid: " + policyId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    @Override public Response policiesThrottlingApplicationPolicyIdPut(String policyId,
            ApplicationThrottlePolicyDTO body, String contentType, String ifMatch, String ifUnmodifiedSince,
            Request request) throws NotFoundException {

        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Application Policy PUT request " + body + " with tierLevel = " + tierLevel);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            ApplicationPolicy applicationPolicy = ApplicationThrottlePolicyMappingUtil
                    .fromApplicationThrottlePolicyDTOToModel(body);
            applicationPolicy.setUuid(policyId);
            apiMgtAdminService.updateApplicationPolicy(applicationPolicy);
            return Response.status(Response.Status.OK).entity(applicationPolicy).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while updating Application Policy. policy uuid: " + policyId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    @Override public Response policiesThrottlingApplicationPost(ApplicationThrottlePolicyDTO body, String contentType,
            Request request) throws NotFoundException {

        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Application Policy PUT request " + body + " with tierLevel = " + tierLevel);
        }

        String policyId = UUID.randomUUID().toString();
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            ApplicationPolicy applicationPolicy = ApplicationThrottlePolicyMappingUtil.
                    fromApplicationThrottlePolicyDTOToModel(body);
            applicationPolicy.setUuid(policyId);
            apiMgtAdminService.addPolicy(tierLevel, applicationPolicy);
            return Response.status(Response.Status.CREATED).entity(applicationPolicy).build();

        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while adding Application Bandwidth Policy. policy uuid: " + policyId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

//    @Override public Response policiesThrottlingApplicationGet(String accept, String ifNoneMatch,
//            String ifModifiedSince, Request request) throws NotFoundException {
//
//        if (log.isDebugEnabled()) {
//            log.debug("Received Application Throttle Policy GET request");
//        }
//        try {
//            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
//            List<ApplicationPolicy> policies = apiMgtAdminService.getAllApplicationPolicies();
//            ApplicationThrottlePolicyListDTO applicationThrottlePolicyListDTO = ApplicationThrottlePolicyMappingUtil
//                    .fromApplicationPolicyArrayToListDTO(policies);
//            return Response.ok().entity(applicationThrottlePolicyListDTO).build();
//
//        } catch (APIManagementException e) {
//            String errorMessage = "Error occurred while retrieving Application Policies";
//            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
//            log.error(errorMessage, e);
//            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
//        }
//    }

//    @Override public Response policiesThrottlingApplicationBandwidthPolicyIdDelete(String policyId, String ifMatch,
//            String ifUnmodifiedSince, Request request) throws NotFoundException {
//
//        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
//        if (log.isDebugEnabled()) {
//            log.info("Received Advance Policy DELETE request with uuid: " + policyId);
//        }
//        return deletePolicy(policyId, tierLevel);
//    }

//    @Override public Response policiesThrottlingApplicationBandwidthPolicyIdGet(String policyId, String ifNoneMatch,
//            String ifModifiedSince, Request request) throws NotFoundException {
//
//        if (log.isDebugEnabled()) {
//            log.info("Received Application Policy Get request. Policy uuid: " + policyId);
//        }
//        try {
//            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
//            ApplicationPolicy applicationPolicy = apiMgtAdminService.getApplicationPolicyByUuid(policyId);
//            ApplicationThrottlePolicyMappingUtil.fromApplicationThrottlePolicyToDTO(applicationPolicy);
//            return Response.status(Response.Status.OK).entity(applicationPolicy).build();
//
//        } catch (APIManagementException e) {
//            String errorMessage = "Error occurred while getting Application Policy. policy uuid: " + policyId;
//            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
//            log.error(errorMessage, e);
//            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
//        }
//    }

//    @Override public Response policiesThrottlingApplicationBandwidthPolicyIdPut(String policyId,
//            ApplicationBandwidthThrottlePolicyDTO body, String contentType, String ifMatch, String ifUnmodifiedSince,
//            Request request) throws NotFoundException {
//
//        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
//        if (log.isDebugEnabled()) {
//            log.info("Received Application Policy PUT request " + body + " with tierLevel = " + tierLevel);
//        }
//        try {
//            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
//            ApplicationPolicy applicationPolicy = ApplicationThrottlePolicyMappingUtil
//                    .fromApplicationThrottlePolicyDTOToModel(body);
//            applicationPolicy.setUuid(policyId);
//            apiMgtAdminService.updateApplicationPolicy(applicationPolicy);
//            return Response.status(Response.Status.OK).entity(applicationPolicy).build();
//
//        } catch (APIManagementException e) {
//            String errorMessage = "Error occurred while updating Application Policy. policy uuid: " + policyId;
//            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
//            log.error(errorMessage, e);
//            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
//        }
//    }

//    @Override public Response policiesThrottlingApplicationBandwidthPost(ApplicationBandwidthThrottlePolicyDTO body,
//            String contentType, Request request) throws NotFoundException {
//
//        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
//        if (log.isDebugEnabled()) {
//            log.info("Received Application Policy PUT request " + body + " with tierLevel = " + tierLevel);
//        }
//
//        String policyId = UUID.randomUUID().toString();
//        try {
//            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
//            ApplicationPolicy applicationPolicy = ApplicationThrottlePolicyMappingUtil.
//                    fromApplicationThrottlePolicyDTOToModel(body);
//            applicationPolicy.setUuid(policyId);
//            apiMgtAdminService.addPolicy(tierLevel, applicationPolicy);
//            return Response.status(Response.Status.CREATED).entity(applicationPolicy).build();
//
//        } catch (APIManagementException e) {
//            String errorMessage = "Error occurred while adding Application Bandwidth Policy. policy uuid: " + policyId;
//            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
//            log.error(errorMessage, e);
//            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
//        }
//    }

    //    /**
//     * @param accept            Accept header value
//     * @param ifNoneMatch       If-None-Match header value
//     * @param ifModifiedSince   If-Modified-Since header value
//     * @param request           msf4j request object
//     * @return Response object
//     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
//     */
//    @Override public Response policiesThrottlingApplicationGet(String accept, String ifNoneMatch,
//            String ifModifiedSince, Request request) throws NotFoundException {
//        if (log.isDebugEnabled()) {
//            log.debug("Received Application Throttle Policy GET request");
//        }
//        try {
//            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
//            List<ApplicationPolicy> policies = apiMgtAdminService.getAllApplicationPolicies();
//            ApplicationThrottlePolicyListDTO applicationThrottlePolicyListDTO = ApplicationThrottlePolicyMappingUtil
//                    .fromApplicationPolicyArrayToListDTO(policies);
//            return Response.ok().entity(applicationThrottlePolicyListDTO).build();
//        } catch (APIManagementException e) {
//            String errorMessage = "Error occurred while retrieving Application Policies";
//            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
//            log.error(errorMessage, e);
//            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
//        }
//    }

//    /**
//     *
//     * @param policyId          Uuid of the Application policy.
//     * @param ifMatch           If-Match header value
//     * @param ifUnmodifiedSince If-Unmodified-Since header value
//     * @param request           msf4j request object
//     * @return Response object
//     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
//     */
//    @Override public Response policiesThrottlingApplicationPolicyIdDelete(String policyId, String ifMatch,
//            String ifUnmodifiedSince, Request request) throws NotFoundException {
//        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
//        if (log.isDebugEnabled()) {
//            log.info("Received Advance Policy DELETE request with uuid: " + policyId);
//        }
//        return deletePolicy(policyId, tierLevel);
//    }

//    /**
//     *
//     * @param policyId          Uuid of the Application policy
//     * @param ifNoneMatch       If-None-Match header value
//     * @param ifModifiedSince   If-Modified-Since header value
//     * @param request           msf4j request object
//     * @return Response object
//     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
//     */
//    @Override public Response policiesThrottlingApplicationPolicyIdGet(String policyId, String ifNoneMatch,
//            String ifModifiedSince, Request request) throws NotFoundException {
//        if (log.isDebugEnabled()) {
//            log.info("Received Application Policy Get request. Policy uuid: " + policyId);
//        }
//        try {
//            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
//            ApplicationPolicy applicationPolicy = apiMgtAdminService.getApplicationPolicyByUuid(policyId);
//            ApplicationThrottlePolicyMappingUtil.fromApplicationThrottlePolicyToDTO(applicationPolicy);
//            return Response.status(Response.Status.OK).entity(applicationPolicy).build();
//        } catch (APIManagementException e) {
//            String errorMessage = "Error occurred while getting Application Policy. policy uuid: " + policyId;
//            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
//            log.error(errorMessage, e);
//            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
//        }
//    }

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
//    @Override
//    public Response policiesThrottlingApplicationPolicyIdPut(String policyId,
//            ApplicationThrottlePolicyDTO body, String contentType, String ifMatch, String ifUnmodifiedSince,
//            Request request) throws NotFoundException {
//        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
//        if (log.isDebugEnabled()) {
//            log.info("Received Application Policy PUT request " + body + " with tierLevel = " + tierLevel);
//        }
//        try {
//            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
//            ApplicationPolicy applicationPolicy = ApplicationThrottlePolicyMappingUtil
//                    .fromApplicationThrottlePolicyDTOToModel(body);
//            applicationPolicy.setUuid(policyId);
//            apiMgtAdminService.updateApplicationPolicy(applicationPolicy);
//            return Response.status(Response.Status.CREATED).entity(applicationPolicy).build();
//        } catch (APIManagementException e) {
//            String errorMessage = "Error occurred while updating Application Policy. policy uuid: " + policyId;
//            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
//            log.error(errorMessage, e);
//            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
//
//        }
//    }

    /**
     *
     * @param body              DTO object including the Policy meta information
     * @param contentType       Content-Type header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
//    @Override public Response policiesThrottlingApplicationPost(ApplicationThrottlePolicyDTO body, String contentType,
//            Request request) throws NotFoundException {
//        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL;
//        if (log.isDebugEnabled()) {
//            log.info("Received Advance Policy POST request " + body + " with tierLevel = " + tierLevel);
//        }
////        return createPolicy(tierLevel, body);
//        return null;
//    }

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
        if (log.isDebugEnabled()) {
            log.debug("Received Application Throttle Policy GET request");
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            List<SubscriptionPolicy> policies = apiMgtAdminService.getAllSubscriptionPolicies();
            SubscriptionThrottlePolicyListDTO subscriptionThrottlePolicyListDTO = SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionPolicyArrayToListDTO(policies);
            return Response.ok().entity(subscriptionThrottlePolicyListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while retrieving Application Policies";
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
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
        if (log.isDebugEnabled()) {
            log.info("Received Subscription Policy Get request. Policy uuid: " + policyId);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            SubscriptionPolicy subscriptionPolicy = apiMgtAdminService.getSubscriptionPolicyByUuid(policyId);
            SubscriptionThrottlePolicyMappingUtil.fromSubscriptionThrottlePolicyToDTO(subscriptionPolicy);
            return Response.status(Response.Status.OK).entity(subscriptionPolicy).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while getting Subscription Policy. policy uuid: " + policyId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
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
    @Override
    public Response policiesThrottlingSubscriptionPolicyIdPut(String policyId,
            SubscriptionThrottlePolicyDTO body, String contentType, String ifMatch, String ifUnmodifiedSince,
            Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Subscription Policy PUT request " + body + " with tierLevel = " + tierLevel);
        }
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            SubscriptionPolicy subscriptionPolicy = SubscriptionThrottlePolicyMappingUtil
                    .fromSubscriptionThrottlePolicyDTOToModel(body);
            subscriptionPolicy.setUuid(policyId);
            apiMgtAdminService.updateSubscriptionPolicy(subscriptionPolicy);
            return Response.status(Response.Status.CREATED).entity(subscriptionPolicy).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while updating Application Policy. policy uuid: " + policyId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();

        }
    }

    /**
     *
     * @param body              DTO object including the Policy meta information
     * @param contentType       Content-Type header value
     * @param request           msf4j request object
     * @return Response object
     * @throws NotFoundException if an error occurred when particular resource does not exits in the system.
     */
    @Override public Response policiesThrottlingSubscriptionPost(SubscriptionThrottlePolicyDTO body, String contentType,
            Request request) throws NotFoundException {
        String tierLevel = APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL;
        if (log.isDebugEnabled()) {
            log.info("Received Advance Policy POST request " + body + " with tierLevel = " + tierLevel);
        }
//        return createPolicy(tierLevel, body);
        return null;
    }

//    private Response getPolicyByUuid(String policyId, String tierLevel) {
//        try {
//            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
//            Policy policy = apiMgtAdminService.getPolicyByUuid(policyId, tierLevel);
//            return Response.status(Response.Status.OK).entity(policy).build();
//        } catch (APIManagementException e) {
//            String errorMessage = "Error occurred while getting Policy. policy uuid: " + policyId;
//            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
//            log.error(errorMessage, e);
//            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
//        }
//    }
//
//    private Response getAllThrottlePolicyByTier(String tierLevel) {
//        try {
//            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
//            List<Policy> policies = apiMgtAdminService.getAllPoliciesByLevel(tierLevel);
//            List<TierDTO> tiers = PolicyMappingUtil.fromPoliciesToDTOs(policies);
//            return Response.ok().entity(tiers).build();
//        } catch (APIManagementException e) {
//            String errorMessage = "Error occurred while retrieving Policy";
//            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
//            log.error(errorMessage, e);
//            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
//        }
//    }
//
//    private Response createPolicy(String tierLevel, Thro body) {
//        try {
//            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
//            Policy policy = PolicyMappingUtil.toPolicy(tierLevel, body);
//            apiMgtAdminService.addPolicy(tierLevel, policy);
//            return Response.status(Response.Status.CREATED).entity(policy).build();
//        } catch (APIManagementException e) {
//            String errorMessage = "Error occurred while adding Policy ";
//            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
//            log.error(errorMessage, e);
//            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
//        }
//    }
//
//    private Response updatePolicy(String policyId, String tierLevel, TierDTO body) {
//        try {
//            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
//            Policy policy = PolicyMappingUtil.toPolicy(tierLevel, body);
//            policy.setUuid(policyId);
//            apiMgtAdminService.updatePolicy(policy);
//            return Response.status(Response.Status.CREATED).entity(policy).build();
//        } catch (APIManagementException e) {
//            String errorMessage = "Error occurred while adding Policy ";
//            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
//            log.error(errorMessage, e);
//            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
//        }
//    }
//
    private Response deletePolicy(String policyId, String tierLevel) {
        try {
            APIMgtAdminService apiMgtAdminService = RestApiUtil.getAPIMgtAdminService();
            apiMgtAdminService.deletePolicyByUuid(policyId, tierLevel);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while deleting a Policy uuid : " + policyId;
            Map<String, String> paramList = new HashMap<>();
            paramList.put(APIMgtConstants.ExceptionsConstants.TIER, policyId);
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil
                    .getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

}
