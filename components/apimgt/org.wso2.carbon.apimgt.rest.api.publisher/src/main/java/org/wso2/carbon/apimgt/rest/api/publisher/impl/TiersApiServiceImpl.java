package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;


import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierPermissionDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public class TiersApiServiceImpl extends TiersApiService {
    @Override
    public Response tiersTierLevelGet(String tierLevel,Integer limit,Integer offset,String accept,String ifNoneMatch){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response tiersTierLevelPost(TierDTO body,String tierLevel,String contentType){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response tiersTierLevelTierNameDelete(String tierName,String tierLevel,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response tiersTierLevelTierNameGet(String tierName,String tierLevel,String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response tiersTierLevelTierNamePut(String tierName,TierDTO body,String tierLevel,String contentType,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response tiersUpdatePermissionPost(String tierName,String tierLevel,String ifMatch,String ifUnmodifiedSince,TierPermissionDTO permissions){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
