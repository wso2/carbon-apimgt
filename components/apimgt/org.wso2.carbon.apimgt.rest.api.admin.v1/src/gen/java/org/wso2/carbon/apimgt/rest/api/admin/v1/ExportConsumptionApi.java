package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.admin.v1.ExportConsumptionApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.ExportConsumptionApiServiceImpl;
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
@Path("/export-consumption")

@Api(description = "the export-consumption API")




public class ExportConsumptionApi  {

  @Context MessageContext securityContext;

ExportConsumptionApiService delegate = new ExportConsumptionApiServiceImpl();


    @GET
    
    
    @Produces({ "application/zip", "application/json" })
    @ApiOperation(value = "Export API Consumption Data ", notes = "This operation provides a ZIP archive containing API consumption/usage data for a given date range. ", response = File.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Consumption" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Consumption data exported successfully as a ZIP file. ", response = File.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class) })
    public Response exportConsumptionData( @NotNull @ApiParam(value = "Start date of the export range (inclusive). Format: YYYY-MM-DD. ",required=true)  @QueryParam("fromDate") String fromDate,  @NotNull @ApiParam(value = "End date of the export range (inclusive). Format: YYYY-MM-DD. ",required=true)  @QueryParam("toDate") String toDate) throws APIManagementException{
        return delegate.exportConsumptionData(fromDate, toDate, securityContext);
    }
}
