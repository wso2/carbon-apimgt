package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.util.ETagUtils;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.PoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.msf4j.Request;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-03T20:31:12.997+05:30")
public class PoliciesApiServiceImpl extends PoliciesApiService {

    private static final Logger log = LoggerFactory.getLogger(PoliciesApiService.class);

    /**
     * Retrieve tiers that corresponding to the level specified
     * 
     * @param tierLevel Tier Level
     * @param limit Maximum tiers to return in a single response
     * @param offset Starting position of the pagination
     * @param ifNoneMatch If-None-Match header value
     * @param request ms4j request object
     * @return A list of tiers qualifying
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response policiesTierLevelGet(String tierLevel, Integer limit, Integer offset, String ifNoneMatch,
            Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);

        log.info("Received Policy GET request for tierLevel " + tierLevel);

        try {
            List<Policy> policies = RestAPIPublisherUtil.getApiPublisher(username).getAllPoliciesByLevel
                    (RestApiUtil.mapRestApiPolicyLevelToPolicyLevelEnum(tierLevel));
            return Response.ok().entity(policies).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while retrieving Policies";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }

    }

    /**
     * Retrieves a single tier
     * 
     * @param tierName Name of the tier
     * @param tierLevel Tier Level
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since value
     * @param request ms4j request object
     * @return Requested tier as the response
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response policiesTierLevelTierNameGet(String tierName, String tierLevel, String ifNoneMatch,
            String ifModifiedSince, Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        String existingFingerprint = policiesTierLevelTierNameGetFingerprint(tierName, tierLevel, ifNoneMatch,
                ifModifiedSince, request);
        if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                .contains(existingFingerprint)) {
            return Response.notModified().build();
        }

        try {
            Policy policy = RestAPIPublisherUtil.getApiPublisher(username).getPolicyByName
                    (RestApiUtil.mapRestApiPolicyLevelToPolicyLevelEnum(tierLevel), tierName);
            return Response.ok().header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"").entity(policy).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while retrieving Policy";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrieves the fingerprint of an existing tier
     * 
     * @param tierName Name of the tier
     * @param tierLevel Tier Level
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since value
     * @param request ms4j request object
     * @return fingerprint of an existing tier
     */
    public String policiesTierLevelTierNameGetFingerprint(String tierName, String tierLevel, String ifNoneMatch,
            String ifModifiedSince, Request request) {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            String lastUpdatedTime = RestAPIPublisherUtil.getApiPublisher(username)
                    .getLastUpdatedTimeOfThrottlingPolicy(RestApiUtil.mapRestApiPolicyLevelToPolicyLevelEnum
                            (tierLevel), tierName);
            return ETagUtils.generateETag(lastUpdatedTime);
        } catch (APIManagementException e) {
            //gives a warning and let it continue the execution
            String errorMessage = "Error while retrieving last updated time of policy :" + tierLevel + "/" + tierName;
            log.error(errorMessage, e);
            return null;
        }
    }
}
