package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.ApplicationsApiServiceImpl;

public class ApplicationsApiServiceFactory {

   private final static ApplicationsApiService service = new ApplicationsApiServiceImpl();

   public static ApplicationsApiService getApplicationsApi()
   {
      return service;
   }
}
