package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.EnvironmentsApiService;
import org.wso2.carbon.apimgt.api.factories.EnvironmentsApiServiceFactory;


import org.wso2.carbon.apimgt.model.Error;
import org.wso2.carbon.apimgt.model.EnvironmentList;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

import org.wso2.msf4j.MicroservicesRunner;

@Path("/environments")
@Consumes({ "application/json" })
@Produces({ "application/json" })

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class EnvironmentsApi  {

   private final EnvironmentsApiService delegate = EnvironmentsApiServiceFactory.getEnvironmentsApi();

   public static void main(String[] args) {
       new MicroservicesRunner().deploy(new EnvironmentsApi()).start();
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response environmentsGet(@QueryParam("apiId") String apiId




) {
        return delegate.environmentsGet(apiId);
    }
}

