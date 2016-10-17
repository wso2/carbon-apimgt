package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.TiersApiService;
import org.wso2.carbon.apimgt.api.factories.TiersApiServiceFactory;


import org.wso2.carbon.apimgt.model.Tier;
import org.wso2.carbon.apimgt.model.TierPermission;
import org.wso2.carbon.apimgt.model.Error;
import org.wso2.carbon.apimgt.model.TierList;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

import org.wso2.msf4j.MicroservicesRunner;

@Path("/tiers")
@Consumes({ "application/json" })
@Produces({ "application/json" })

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class TiersApi  {

   private final TiersApiService delegate = TiersApiServiceFactory.getTiersApi();

   public static void main(String[] args) {
       new MicroservicesRunner().deploy(new TiersApi()).start();
    }

    @POST
    @Path("/update-permission")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response tiersUpdatePermissionPost(@QueryParam("tierName") String tierName




,@QueryParam("tierLevel") String tierLevel




,

@HeaderParam("If-Match") String ifMatch


,

@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince


,


@BodyParam("permissions") TierPermission permissions

) {
        return delegate.tiersUpdatePermissionPost(tierName,tierLevel,ifMatch,ifUnmodifiedSince,permissions);
    }
    @GET
    @Path("/{tierLevel}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response tiersTierLevelGet(
@PathParam("tierLevel") String tierLevel



,@QueryParam("limit") Integer limit




,@QueryParam("offset") Integer offset




,

@HeaderParam("Accept") String accept


,

@HeaderParam("If-None-Match") String ifNoneMatch


) {
        return delegate.tiersTierLevelGet(tierLevel,limit,offset,accept,ifNoneMatch);
    }
    @POST
    @Path("/{tierLevel}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response tiersTierLevelPost(


@BodyParam("body") Tier body

,
@PathParam("tierLevel") String tierLevel



,

@HeaderParam("Content-Type") String contentType


) {
        return delegate.tiersTierLevelPost(body,tierLevel,contentType);
    }
    @GET
    @Path("/{tierLevel}/{tierName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response tiersTierLevelTierNameGet(
@PathParam("tierName") String tierName



,
@PathParam("tierLevel") String tierLevel



,

@HeaderParam("Accept") String accept


,

@HeaderParam("If-None-Match") String ifNoneMatch


,

@HeaderParam("If-Modified-Since") String ifModifiedSince


) {
        return delegate.tiersTierLevelTierNameGet(tierName,tierLevel,accept,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{tierLevel}/{tierName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response tiersTierLevelTierNamePut(
@PathParam("tierName") String tierName



,


@BodyParam("body") Tier body

,
@PathParam("tierLevel") String tierLevel



,

@HeaderParam("Content-Type") String contentType


,

@HeaderParam("If-Match") String ifMatch


,

@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince


) {
        return delegate.tiersTierLevelTierNamePut(tierName,body,tierLevel,contentType,ifMatch,ifUnmodifiedSince);
    }
    @DELETE
    @Path("/{tierLevel}/{tierName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response tiersTierLevelTierNameDelete(
@PathParam("tierName") String tierName



,
@PathParam("tierLevel") String tierLevel



,

@HeaderParam("If-Match") String ifMatch


,

@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince


) {
        return delegate.tiersTierLevelTierNameDelete(tierName,tierLevel,ifMatch,ifUnmodifiedSince);
    }
}

