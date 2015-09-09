package org.wso2.carbon.apimgt.rest.api.impl;

import org.wso2.carbon.apimgt.rest.api.*;
import org.wso2.carbon.apimgt.rest.api.model.*;


import org.wso2.carbon.apimgt.rest.api.model.Error;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;

public class ExternalStoresApiServiceImpl extends ExternalStoresApiService {
  
      @Override
      public Response externalStoresGet(String limit,String offset,String query,String accept,String ifNoneMatch)
      throws NotFoundException {
          // do some magic!
          return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
      }
  
      @Override
      public Response externalStoresPublishExternalstorePost(String apiId,String externalStoreId)
      throws NotFoundException {
          // do some magic!
          return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
      }
  
}
