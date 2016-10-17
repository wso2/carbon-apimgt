package org.wso2.carbon.apimgt.api.factories;

import org.wso2.carbon.apimgt.api.ApisApiService;
import org.wso2.carbon.apimgt.api.impl.ApisApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class ApisApiServiceFactory {

   private final static ApisApiService service = new ApisApiServiceImpl();

   public static ApisApiService getApisApi()
   {
      return service;
   }
}
