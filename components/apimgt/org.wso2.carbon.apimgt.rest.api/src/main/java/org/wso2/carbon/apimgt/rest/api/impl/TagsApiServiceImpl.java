package org.wso2.carbon.apimgt.rest.api.impl;

import org.wso2.carbon.apimgt.rest.api.*;
import org.wso2.carbon.apimgt.rest.api.dto.*;


import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;

public class TagsApiServiceImpl extends TagsApiService {
    @Override
    public Response tagsGet(String accept,String ifNoneMatch,String query){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
