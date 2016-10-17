package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.ProductsApiService;
import org.wso2.carbon.apimgt.api.factories.ProductsApiServiceFactory;


import org.wso2.carbon.apimgt.model.APIProductList;
import org.wso2.carbon.apimgt.model.Error;
import org.wso2.carbon.apimgt.model.APIProduct;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

import org.wso2.msf4j.MicroservicesRunner;

@Path("/products")
@Consumes({ "application/json" })
@Produces({ "application/json" })

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class ProductsApi  {

   private final ProductsApiService delegate = ProductsApiServiceFactory.getProductsApi();

   public static void main(String[] args) {
       new MicroservicesRunner().deploy(new ProductsApi()).start();
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response productsGet(@QueryParam("limit") Integer limit




,@QueryParam("offset") Integer offset




,@QueryParam("query") String query




,

@HeaderParam("Accept") String accept


,

@HeaderParam("If-None-Match") String ifNoneMatch


) {
        return delegate.productsGet(limit,offset,query,accept,ifNoneMatch);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response productsPost(


@BodyParam("body") APIProduct body

,

@HeaderParam("Content-Type") String contentType


) {
        return delegate.productsPost(body,contentType);
    }
    @POST
    @Path("/change-product-lifecycle")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response productsChangeProductLifecyclePost(@QueryParam("action") String action




,@QueryParam("productId") String productId




,@QueryParam("lifecycleChecklist") String lifecycleChecklist




,

@HeaderParam("If-Match") String ifMatch


,

@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince


) {
        return delegate.productsChangeProductLifecyclePost(action,productId,lifecycleChecklist,ifMatch,ifUnmodifiedSince);
    }
    @POST
    @Path("/copy-product")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response productsCopyProductPost(@QueryParam("newVersion") String newVersion




,@QueryParam("productId") String productId




) {
        return delegate.productsCopyProductPost(newVersion,productId);
    }
    @GET
    @Path("/{productId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response productsProductIdGet(
@PathParam("productId") String productId



,

@HeaderParam("Accept") String accept


,

@HeaderParam("If-None-Match") String ifNoneMatch


,

@HeaderParam("If-Modified-Since") String ifModifiedSince


) {
        return delegate.productsProductIdGet(productId,accept,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{productId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response productsProductIdPut(
@PathParam("productId") String productId



,


@BodyParam("body") APIProduct body

,

@HeaderParam("Content-Type") String contentType


,

@HeaderParam("If-Match") String ifMatch


,

@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince


) {
        return delegate.productsProductIdPut(productId,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @DELETE
    @Path("/{productId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response productsProductIdDelete(
@PathParam("productId") String productId



,

@HeaderParam("If-Match") String ifMatch


,

@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince


) {
        return delegate.productsProductIdDelete(productId,ifMatch,ifUnmodifiedSince);
    }
}

