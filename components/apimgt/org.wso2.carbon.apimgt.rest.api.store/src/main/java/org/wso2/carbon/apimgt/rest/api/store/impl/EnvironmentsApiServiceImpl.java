package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;


import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;

public class EnvironmentsApiServiceImpl extends EnvironmentsApiService {
    @Override
    public Response environmentsGet(String apiId){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
