package org.wso2.carbon.apimgt.rest.api.analytics.impl;

import org.wso2.carbon.apimgt.rest.api.analytics.*;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.*;


import java.util.List;
import org.wso2.carbon.apimgt.rest.api.analytics.NotFoundException;

import java.io.InputStream;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class ApiApiServiceImpl extends ApiApiService {
    @Override
    public Response apiApiUsageGet(String from
, String to
 , Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
