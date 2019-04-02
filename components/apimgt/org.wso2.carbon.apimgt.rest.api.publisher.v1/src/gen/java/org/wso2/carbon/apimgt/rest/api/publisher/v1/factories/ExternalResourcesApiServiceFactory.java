package org.wso2.carbon.apimgt.rest.api.publisher.v1.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.ExternalResourcesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ExternalResourcesApiServiceImpl;

public class ExternalResourcesApiServiceFactory {

   private final static ExternalResourcesApiService service = new ExternalResourcesApiServiceImpl();

   public static ExternalResourcesApiService getExternalResourcesApi()
   {
      return service;
   }
}
