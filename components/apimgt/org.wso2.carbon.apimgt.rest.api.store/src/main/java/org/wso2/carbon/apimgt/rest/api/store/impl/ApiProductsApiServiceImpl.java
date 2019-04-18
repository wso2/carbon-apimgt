package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;


import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIProductListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public class ApiProductsApiServiceImpl extends ApiProductsApiService {
    @Override
    public Response apiProductsApiProductIdDocumentsDocumentIdContentGet(String apiProductId,String documentId,String xWSO2Tenant,String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apiProductsApiProductIdDocumentsDocumentIdGet(String apiProductId,String documentId,String xWSO2Tenant,String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apiProductsApiProductIdDocumentsGet(String apiProductId,Integer limit,Integer offset,String xWSO2Tenant,String accept,String ifNoneMatch){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apiProductsApiProductIdGet(String apiProductId,String accept,String ifNoneMatch,String ifModifiedSince,String xWSO2Tenant){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apiProductsApiProductIdSwaggerGet(String apiProductId,String accept,String ifNoneMatch,String ifModifiedSince,String xWSO2Tenant){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apiProductsApiProductIdThumbnailGet(String apiProductId,String xWSO2Tenant,String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apiProductsGet(Integer limit,Integer offset,String xWSO2Tenant,String query,String accept,String ifNoneMatch){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
