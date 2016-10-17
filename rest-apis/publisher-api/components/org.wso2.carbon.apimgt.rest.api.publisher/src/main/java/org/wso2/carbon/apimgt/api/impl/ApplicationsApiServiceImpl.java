package org.wso2.carbon.apimgt.api.impl;

import org.wso2.carbon.apimgt.api.*;

import org.wso2.carbon.apimgt.model.Error;
import org.wso2.carbon.apimgt.model.Application;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class ApplicationsApiServiceImpl extends ApplicationsApiService {
      @Override
      public Response applicationsApplicationIdGet(String applicationId,String accept,String ifNoneMatch,String ifModifiedSince){
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
}
