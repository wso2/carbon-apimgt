package org.wso2.carbon.apimgt.rest.api.store.v1.factories;

import org.wso2.carbon.apimgt.rest.api.store.v1.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.SubscriptionsApiServiceImpl;

public class SubscriptionsApiServiceFactory {

   private final static SubscriptionsApiService service = new SubscriptionsApiServiceImpl();

   public static SubscriptionsApiService getSubscriptionsApi()
   {
      return service;
   }
}
