package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ThrottlingPoliciesApiService;

import javax.ws.rs.core.Response;


public class ThrottlingPoliciesApiServiceImpl extends ThrottlingPoliciesApiService {

    @Override
    public Response getAllThrottlingPolicies(String policyLevel, Integer limit, Integer offset,
            String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response getThrottlingPolicyByName(String policyName, String policyLevel, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
