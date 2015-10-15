package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.BlockSubscriptionApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.BlockSubscriptionApiServiceImpl;

public class BlockSubscriptionApiServiceFactory {

   private final static BlockSubscriptionApiService service = new BlockSubscriptionApiServiceImpl();

   public static BlockSubscriptionApiService getBlockSubscriptionApi()
   {
      return service;
   }
}
