package org.wso2.carbon.apimgt.rest.api.impl;

import org.wso2.carbon.apimgt.rest.api.*;
import org.wso2.carbon.apimgt.rest.api.model.*;


import org.wso2.carbon.apimgt.rest.api.model.Tier;
import org.wso2.carbon.apimgt.rest.api.model.TierPermission;
import org.wso2.carbon.apimgt.rest.api.model.Error;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;

public class UpdateTierPermissionApiServiceImpl extends UpdateTierPermissionApiService {
    @Override
    public Response updateTierPermissionPost(String tierName,TierPermission permissions,String contentType,String ifMatch,String ifUnmodifiedSince)
    throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
