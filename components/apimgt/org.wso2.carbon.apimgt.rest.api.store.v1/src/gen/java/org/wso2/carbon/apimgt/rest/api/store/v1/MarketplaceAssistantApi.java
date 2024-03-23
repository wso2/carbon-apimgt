package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.MarketplaceAssistantApiCountResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.MarketplaceAssistantRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.MarketplaceAssistantResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.MarketplaceAssistantApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.MarketplaceAssistantApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.inject.Inject;

import io.swagger.annotations.*;
import java.io.InputStream;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
@Path("/marketplace-assistant")

@Api(description = "the marketplace-assistant API")




public class MarketplaceAssistantApi  {

  @Context MessageContext securityContext;

MarketplaceAssistantApiService delegate = new MarketplaceAssistantApiServiceImpl();


    @GET
    @Path("/api-count")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "API Count endpoint.", notes = "Get the api count of Marketplace Assistant AI service. ", response = MarketplaceAssistantApiCountResponseDTO.class, tags={ "Marketplace Assistant",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Api Count is returned. ", response = MarketplaceAssistantApiCountResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error. An error occurred while retrieving the API count. ", response = Void.class) })
    public Response getMarketplaceAssistantApiCount() throws APIManagementException{
        return delegate.getMarketplaceAssistantApiCount(securityContext);
    }

    @POST
    @Path("/chat")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Marketplace Assistant Chat Endpoint.", notes = "Send a single query to the service and get the response. ", response = MarketplaceAssistantResponseDTO.class, tags={ "Marketplace Assistant" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Marketplace Assistant chat response payload. ", response = MarketplaceAssistantResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 429, message = "Too many requests.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error. An error occurred while executing test using Marketplace Chat service. ", response = Void.class) })
    public Response marketplaceAssistantExecute(@ApiParam(value = "Marketplace Assistant chat request payload. " ) MarketplaceAssistantRequestDTO marketplaceAssistantRequestDTO) throws APIManagementException{
        return delegate.marketplaceAssistantExecute(marketplaceAssistantRequestDTO, securityContext);
    }
}
