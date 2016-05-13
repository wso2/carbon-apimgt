package org.wso2.carbon.throttle.service.impl;

import org.wso2.carbon.throttle.service.*;
import org.wso2.carbon.throttle.service.dto.*;


import org.wso2.carbon.throttle.service.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;

public class ThrottleAsStringApiServiceImpl extends ThrottleAsStringApiService {
    @Override
    public Response throttleAsStringGet(String query){
        return Response.ok().entity(ThrottlingDBUtil.getThrottledEventsAsString(query)).build();
    }
}
