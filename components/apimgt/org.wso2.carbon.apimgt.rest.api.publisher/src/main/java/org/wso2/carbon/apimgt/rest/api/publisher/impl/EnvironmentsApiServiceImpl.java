package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.wso2.carbon.apimgt.rest.api.publisher.*;


import javax.ws.rs.core.Response;

public class EnvironmentsApiServiceImpl extends EnvironmentsApiService {
    @Override
    public Response environmentsGet(String apiId){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
