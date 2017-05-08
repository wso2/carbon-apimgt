package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.wso2.carbon.apimgt.rest.api.store.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.store.CompositeApisApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-03T17:36:56.084+05:30")
public class CompositeApisApiServiceImpl extends CompositeApisApiService {
    @Override
    public Response compositeApisApiIdDelete(String apiId
, String ifMatch
, String ifUnmodifiedSince
, Request request
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response compositeApisApiIdGet(String apiId
, String accept
, String ifNoneMatch
, String ifModifiedSince
, Request request
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response compositeApisApiIdPut(String apiId
, APIDTO body
, String contentType
, String ifMatch
, String ifUnmodifiedSince
, Request request
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response compositeApisApiIdSwaggerGet(String apiId
, String accept
, String ifNoneMatch
, String ifModifiedSince
, Request request
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response compositeApisApiIdSwaggerPut(String apiId
, String endpointId
, String contentType
, String ifMatch
, String ifUnmodifiedSince
, Request request
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response compositeApisGet(Integer limit
, Integer offset
, String query
, String accept
, String ifNoneMatch
, Request request
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response compositeApisPost(APIDTO body
, String contentType
, Request request
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
