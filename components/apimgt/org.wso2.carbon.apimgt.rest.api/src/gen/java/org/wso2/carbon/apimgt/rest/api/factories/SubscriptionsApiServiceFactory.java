package org.wso2.carbon.apimgt.rest.api.factories;

import org.wso2.carbon.apimgt.rest.api.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.impl.SubscriptionsApiServiceImpl;

public class SubscriptionsApiServiceFactory {

   private final static SubscriptionsApiService service = new SubscriptionsApiServiceImpl();

   public static SubscriptionsApiService getSubscriptionsApi()
   {
      return service;
   }
}
