package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.TenantInfoApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.TenantInfoApiServiceImpl;

public class TenantInfoApiServiceFactory {

   private final static TenantInfoApiService service = new TenantInfoApiServiceImpl();

   public static TenantInfoApiService getTenantInfoApi()
   {
      return service;
   }
}
