package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.SubscriptionsApiService;
import org.wso2.carbon.apimgt.api.factories.SubscriptionsApiServiceFactory;


import org.wso2.carbon.apimgt.model.SubscriptionList;
import org.wso2.carbon.apimgt.model.Error;
import org.wso2.carbon.apimgt.model.Subscription;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

import org.wso2.msf4j.MicroservicesRunner;

@Path("/subscriptions")
@Consumes({ "application/json" })
@Produces({ "application/json" })

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class SubscriptionsApi  {

   private final SubscriptionsApiService delegate = SubscriptionsApiServiceFactory.getSubscriptionsApi();

   public static void main(String[] args) {
       new MicroservicesRunner().deploy(new SubscriptionsApi()).start();
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response subscriptionsGet(@QueryParam("apiId") String apiId




,@QueryParam("limit") Integer limit




,@QueryParam("offset") Integer offset




,

@HeaderParam("Accept") String accept


,

@HeaderParam("If-None-Match") String ifNoneMatch


) {
        return delegate.subscriptionsGet(apiId,limit,offset,accept,ifNoneMatch);
    }
    @POST
    @Path("/block-subscription")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response subscriptionsBlockSubscriptionPost(@QueryParam("subscriptionId") String subscriptionId




,@QueryParam("blockState") String blockState




,

@HeaderParam("If-Match") String ifMatch


,

@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince


) {
        return delegate.subscriptionsBlockSubscriptionPost(subscriptionId,blockState,ifMatch,ifUnmodifiedSince);
    }
    @POST
    @Path("/unblock-subscription")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response subscriptionsUnblockSubscriptionPost(@QueryParam("subscriptionId") String subscriptionId




,

@HeaderParam("If-Match") String ifMatch


,

@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince


) {
        return delegate.subscriptionsUnblockSubscriptionPost(subscriptionId,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/{subscriptionId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response subscriptionsSubscriptionIdGet(
@PathParam("subscriptionId") String subscriptionId



,

@HeaderParam("Accept") String accept


,

@HeaderParam("If-None-Match") String ifNoneMatch


,

@HeaderParam("If-Modified-Since") String ifModifiedSince


) {
        return delegate.subscriptionsSubscriptionIdGet(subscriptionId,accept,ifNoneMatch,ifModifiedSince);
    }
}

