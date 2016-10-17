package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.ApplicationsApiService;
import org.wso2.carbon.apimgt.api.factories.ApplicationsApiServiceFactory;


import org.wso2.carbon.apimgt.model.Error;
import org.wso2.carbon.apimgt.model.Application;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

import org.wso2.msf4j.MicroservicesRunner;

@Path("/applications")
@Consumes({ "application/json" })
@Produces({ "application/json" })

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class ApplicationsApi  {

   private final ApplicationsApiService delegate = ApplicationsApiServiceFactory.getApplicationsApi();

   public static void main(String[] args) {
       new MicroservicesRunner().deploy(new ApplicationsApi()).start();
    }

    @GET
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response applicationsApplicationIdGet(
@PathParam("applicationId") String applicationId



,

@HeaderParam("Accept") String accept


,

@HeaderParam("If-None-Match") String ifNoneMatch


,

@HeaderParam("If-Modified-Since") String ifModifiedSince


) {
        return delegate.applicationsApplicationIdGet(applicationId,accept,ifNoneMatch,ifModifiedSince);
    }
}

