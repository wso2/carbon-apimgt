package org.wso2.carbon.apimgt.rest.api.store;

import io.swagger.annotations.ApiParam;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.rest.api.store.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.rest.api.store.factories.WorkflowsApiServiceFactory;
import org.wso2.msf4j.Microservice;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.store.WorkflowsApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/store/v1//workflows")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the workflows API")
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-28T22:39:18.827+05:30")
public class WorkflowsApi implements Microservice  {
   private final WorkflowsApiService delegate = WorkflowsApiServiceFactory.getWorkflowsApi();

    @POST
    @Path("/update-workflow-status")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update workflow status", notes = "This operation can be used to approve or reject a workflow task. . ", response = WorkflowDTO.class, tags={ "Workflows (Individual)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Workflow request information is returned. ", response = WorkflowDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = WorkflowDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Workflow for the given reference in not found. ", response = WorkflowDTO.class) })
    public Response workflowsUpdateWorkflowStatusPost(@ApiParam(value = "Workflow reference id ",required=true) @QueryParam("workflowReferenceId") String workflowReferenceId
,@ApiParam(value = "Workflow event that need to be updated " ,required=true) WorkflowDTO body
)
    throws NotFoundException {
        return delegate.workflowsUpdateWorkflowStatusPost(workflowReferenceId,body);
    }
}
