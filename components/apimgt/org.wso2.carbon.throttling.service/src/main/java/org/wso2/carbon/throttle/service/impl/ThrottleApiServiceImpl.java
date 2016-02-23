package org.wso2.carbon.throttle.service.impl;

import org.wso2.carbon.throttle.service.*;
import org.wso2.carbon.throttle.service.dto.*;


import org.wso2.carbon.throttle.service.dto.ErrorDTO;
import org.wso2.carbon.throttle.service.dto.ThrottledEventDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;

public class ThrottleApiServiceImpl extends ThrottleApiService {
    @Override
    public Response throttleGet(){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
