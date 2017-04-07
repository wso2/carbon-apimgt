package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import javax.ws.rs.core.Response;
import org.wso2.carbon.apimgt.rest.api.publisher.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.publisher.EnvironmentsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.msf4j.Request;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date =
        "2016-11-01T13:47:43.416+05:30")
public class EnvironmentsApiServiceImpl extends EnvironmentsApiService {
    @Override
    public Response environmentsGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince, Request
            request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
