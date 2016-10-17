package org.wso2.carbon.apimgt.api.factories;

import org.wso2.carbon.apimgt.api.ApplicationsApiService;
import org.wso2.carbon.apimgt.api.impl.ApplicationsApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class ApplicationsApiServiceFactory {

   private final static ApplicationsApiService service = new ApplicationsApiServiceImpl();

   public static ApplicationsApiService getApplicationsApi()
   {
      return service;
   }
}
