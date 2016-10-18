package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;


import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIProductListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIProductDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public class ProductsApiServiceImpl extends ProductsApiService {
    @Override
    public Response productsChangeProductLifecyclePost(String action,String productId,String lifecycleChecklist,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response productsCopyProductPost(String newVersion,String productId){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response productsGet(Integer limit,Integer offset,String query,String accept,String ifNoneMatch){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response productsPost(APIProductDTO body,String contentType){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response productsProductIdDelete(String productId,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response productsProductIdGet(String productId,String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response productsProductIdPut(String productId,APIProductDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
