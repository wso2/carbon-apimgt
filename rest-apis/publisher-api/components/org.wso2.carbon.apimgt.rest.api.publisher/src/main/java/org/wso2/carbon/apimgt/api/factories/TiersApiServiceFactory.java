package org.wso2.carbon.apimgt.api.factories;

import org.wso2.carbon.apimgt.api.TiersApiService;
import org.wso2.carbon.apimgt.api.impl.TiersApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class TiersApiServiceFactory {

   private final static TiersApiService service = new TiersApiServiceImpl();

   public static TiersApiService getTiersApi()
   {
      return service;
   }
}
