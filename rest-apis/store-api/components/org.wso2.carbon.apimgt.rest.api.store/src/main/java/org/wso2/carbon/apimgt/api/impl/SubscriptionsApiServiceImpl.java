package org.wso2.carbon.apimgt.api.impl;

import org.wso2.carbon.apimgt.api.*;

import org.wso2.carbon.apimgt.model.SubscriptionList;
import org.wso2.carbon.apimgt.model.Error;
import org.wso2.carbon.apimgt.model.Subscription;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class SubscriptionsApiServiceImpl extends SubscriptionsApiService {
      @Override
      public Response subscriptionsGet(String apiId,Integer limit,Integer offset,String accept,String ifNoneMatch){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response subscriptionsBlockSubscriptionPost(String subscriptionId,String blockState,String ifMatch,String ifUnmodifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response subscriptionsUnblockSubscriptionPost(String subscriptionId,String ifMatch,String ifUnmodifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response subscriptionsSubscriptionIdGet(String subscriptionId,String accept,String ifNoneMatch,String ifModifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
}
