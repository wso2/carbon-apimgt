package org.wso2.carbon.apimgt.rest.api.store.v1.factories;

import org.wso2.carbon.apimgt.rest.api.store.v1.CompositeApisApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.CompositeApisApiServiceImpl;

public class CompositeApisApiServiceFactory {

   private final static CompositeApisApiService service = new CompositeApisApiServiceImpl();

   public static CompositeApisApiService getCompositeApisApi()
   {
      return service;
   }
}
