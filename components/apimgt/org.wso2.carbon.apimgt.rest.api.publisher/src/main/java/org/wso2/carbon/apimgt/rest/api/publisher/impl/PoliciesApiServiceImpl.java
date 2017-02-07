package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-03T20:31:12.997+05:30")
public class PoliciesApiServiceImpl extends PoliciesApiService {
    @Override
    public Response policiesTierLevelGet(String tierLevel, Integer limit, Integer offset, String accept,
                                         String ifNoneMatch) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response policiesTierLevelTierNameGet(String tierName, String tierLevel, String accept, String ifNoneMatch,
                                                 String ifModifiedSince) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response policiesUpdatePermissionPost(String tierName, String tierLevel, String ifMatch,
                                                 String ifUnmodifiedSince, TierPermissionDTO permissions)
            throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
