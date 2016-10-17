package org.wso2.carbon.apimgt.api.factories;

import org.wso2.carbon.apimgt.api.SubscriptionsApiService;
import org.wso2.carbon.apimgt.api.impl.SubscriptionsApiServiceImpl;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class SubscriptionsApiServiceFactory {

   private final static SubscriptionsApiService service = new SubscriptionsApiServiceImpl();

   public static SubscriptionsApiService getSubscriptionsApi()
   {
      return service;
   }
}
