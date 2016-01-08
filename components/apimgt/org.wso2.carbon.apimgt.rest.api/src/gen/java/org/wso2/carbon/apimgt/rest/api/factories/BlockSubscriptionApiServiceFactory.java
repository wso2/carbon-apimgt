package org.wso2.carbon.apimgt.rest.api.factories;

import org.wso2.carbon.apimgt.rest.api.BlockSubscriptionApiService;
import org.wso2.carbon.apimgt.rest.api.impl.BlockSubscriptionApiServiceImpl;

public class BlockSubscriptionApiServiceFactory {

   private final static BlockSubscriptionApiService service = new BlockSubscriptionApiServiceImpl();

   public static BlockSubscriptionApiService getBlockSubscriptionApi()
   {
      return service;
   }
}
