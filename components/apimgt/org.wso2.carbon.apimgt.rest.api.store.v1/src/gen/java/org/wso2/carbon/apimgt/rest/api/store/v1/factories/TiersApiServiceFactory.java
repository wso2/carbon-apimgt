package org.wso2.carbon.apimgt.rest.api.store.v1.factories;

import org.wso2.carbon.apimgt.rest.api.store.v1.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.TiersApiServiceImpl;

public class TiersApiServiceFactory {

   private final static TiersApiService service = new TiersApiServiceImpl();

   public static TiersApiService getTiersApi()
   {
      return service;
   }
}
