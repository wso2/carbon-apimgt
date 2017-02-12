package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.PoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;

import javax.ws.rs.core.Response;
import java.util.List;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-03T20:31:12.997+05:30")
public class PoliciesApiServiceImpl extends PoliciesApiService {

    private static final Logger log = LoggerFactory.getLogger(PoliciesApiService.class);

    @Override
    public Response policiesTierLevelGet(String tierLevel, Integer limit, Integer offset, String accept,
                                         String ifNoneMatch, String minorVersion) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();

        log.info("Received Policy GET request for tierLevel " + tierLevel);

        try {
            List<Policy> policies = RestAPIPublisherUtil.getApiPublisher(username).getAllPoliciesByLevel(tierLevel);
            return Response.ok().entity(policies).build();
        } catch (APIManagementException e) {
            String msg = "Error occurred while retrieving Policy";
            RestApiUtil.handleInternalServerError(msg, e, log);
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(msg, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }

    }

    @Override
    public Response policiesTierLevelTierNameGet(String tierName, String tierLevel, String accept, String ifNoneMatch,
                                                 String ifModifiedSince, String minorVersion) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "Tier Name magic!")).build();
    }

}
