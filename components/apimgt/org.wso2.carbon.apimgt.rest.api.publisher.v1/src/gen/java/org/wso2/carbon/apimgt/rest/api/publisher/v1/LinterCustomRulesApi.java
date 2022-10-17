package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.LinterCustomRulesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.LinterCustomRulesApiServiceImpl;
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
@Path("/linter-custom-rules")

@Api(description = "the linter-custom-rules API")




public class LinterCustomRulesApi  {

  @Context MessageContext securityContext;

LinterCustomRulesApiService delegate = new LinterCustomRulesApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get linter custom rules.", notes = "This operation can be used to get linter custom rules from tenant-config. ", response = String.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Linter Custom Rules" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Linter Custom Rules Retrieved Successfully. ", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getLinterCustomRules() throws APIManagementException{
        return delegate.getLinterCustomRules(securityContext);
    }
}
