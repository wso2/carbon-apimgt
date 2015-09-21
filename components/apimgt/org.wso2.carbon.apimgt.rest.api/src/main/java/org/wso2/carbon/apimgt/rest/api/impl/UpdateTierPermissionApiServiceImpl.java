package org.wso2.carbon.apimgt.rest.api.impl;

import org.wso2.carbon.apimgt.rest.api.*;
import org.wso2.carbon.apimgt.rest.api.dto.*;


import org.wso2.carbon.apimgt.rest.api.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.dto.TierPermissionDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;

public class UpdateTierPermissionApiServiceImpl extends UpdateTierPermissionApiService {
    @Override
    public Response updateTierPermissionPost(String tierName,TierPermissionDTO permissions,String contentType,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
