package org.wso2.carbon.apimgt.rest.api.factories;

import org.wso2.carbon.apimgt.rest.api.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.impl.TiersApiServiceImpl;

public class TiersApiServiceFactory {

   private final static TiersApiService service = new TiersApiServiceImpl();

   public static TiersApiService getTiersApi()
   {
      return service;
   }
}
