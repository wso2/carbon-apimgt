package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;


import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.TierDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public class PoliciesApiServiceImpl extends PoliciesApiService {

    @Override
    public Response policiesMediationGet(Integer limit, Integer offset, String query, String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response policiesPolicyLevelGet(String policyLevel, Integer limit, Integer offset,
            String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response policiesPolicyLevelPolicyNameGet(String policyName, String policyLevel,
            String ifNoneMatch) {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
