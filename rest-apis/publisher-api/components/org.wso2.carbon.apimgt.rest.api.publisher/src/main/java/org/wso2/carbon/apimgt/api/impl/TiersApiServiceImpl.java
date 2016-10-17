package org.wso2.carbon.apimgt.api.impl;

import org.wso2.carbon.apimgt.api.*;

import org.wso2.carbon.apimgt.model.Tier;
import org.wso2.carbon.apimgt.model.TierPermission;
import org.wso2.carbon.apimgt.model.Error;
import org.wso2.carbon.apimgt.model.TierList;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class TiersApiServiceImpl extends TiersApiService {
      @Override
      public Response tiersUpdatePermissionPost(String tierName,String tierLevel,String ifMatch,String ifUnmodifiedSince,TierPermission permissions){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response tiersTierLevelGet(String tierLevel,Integer limit,Integer offset,String accept,String ifNoneMatch){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response tiersTierLevelPost(Tier body,String tierLevel,String contentType){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response tiersTierLevelTierNameGet(String tierName,String tierLevel,String accept,String ifNoneMatch,String ifModifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response tiersTierLevelTierNamePut(String tierName,Tier body,String tierLevel,String contentType,String ifMatch,String ifUnmodifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      @Override
      public Response tiersTierLevelTierNameDelete(String tierName,String tierLevel,String ifMatch,String ifUnmodifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
}
