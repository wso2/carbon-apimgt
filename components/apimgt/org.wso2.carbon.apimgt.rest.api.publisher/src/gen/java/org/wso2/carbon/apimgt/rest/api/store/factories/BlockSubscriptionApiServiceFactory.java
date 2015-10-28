package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.BlockSubscriptionApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.BlockSubscriptionApiServiceImpl;

public class BlockSubscriptionApiServiceFactory {

   private final static BlockSubscriptionApiService service = new BlockSubscriptionApiServiceImpl();

   public static BlockSubscriptionApiService getBlockSubscriptionApi()
   {
      return service;
   }
}
