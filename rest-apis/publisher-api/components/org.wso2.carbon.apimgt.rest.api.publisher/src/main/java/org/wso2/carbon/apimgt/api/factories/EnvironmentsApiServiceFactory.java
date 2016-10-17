package org.wso2.carbon.apimgt.api.factories;

import org.wso2.carbon.apimgt.api.EnvironmentsApiService;
import org.wso2.carbon.apimgt.api.impl.EnvironmentsApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class EnvironmentsApiServiceFactory {

   private final static EnvironmentsApiService service = new EnvironmentsApiServiceImpl();

   public static EnvironmentsApiService getEnvironmentsApi()
   {
      return service;
   }
}
