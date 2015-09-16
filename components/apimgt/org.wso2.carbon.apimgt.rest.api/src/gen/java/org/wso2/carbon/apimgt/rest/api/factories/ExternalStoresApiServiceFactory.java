package org.wso2.carbon.apimgt.rest.api.factories;

import org.wso2.carbon.apimgt.rest.api.ExternalStoresApiService;
import org.wso2.carbon.apimgt.rest.api.impl.ExternalStoresApiServiceImpl;

public class ExternalStoresApiServiceFactory {

   private final static ExternalStoresApiService service = new ExternalStoresApiServiceImpl();

   public static ExternalStoresApiService getExternalStoresApi()
   {
      return service;
   }
}
