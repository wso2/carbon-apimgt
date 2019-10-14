package org.wso2.carbon.throttle.service;

import org.wso2.carbon.throttle.service.dto.*;
import org.wso2.carbon.throttle.service.ConditionGroupsApiService;
import org.wso2.carbon.throttle.service.factories.ConditionGroupsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.throttle.service.dto.ErrorDTO;
import org.wso2.carbon.throttle.service.dto.ConditionGroupListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/conditionGroups")

@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/conditionGroups", description = "the conditionGroups API")
public class ConditionGroupsApi  {

   private final ConditionGroupsApiService delegate = ConditionGroupsApiServiceFactory.getConditionGroupsApi();

    @GET
    
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Condition groups", notes = "This will provide condition groups in database.\n", response = ConditionGroupListDTO.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "An array of condition groups"),
        
        @io.swagger.annotations.ApiResponse(code = 200, message = "Unexpected error") })

    public Response conditionGroupsGet()
    {
    return delegate.conditionGroupsGet();
    }
}

