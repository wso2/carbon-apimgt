package org.wso2.carbon.apimgt.rest.api.factories;

import org.wso2.carbon.apimgt.rest.api.UpdateTierPermissionApiService;
import org.wso2.carbon.apimgt.rest.api.impl.UpdateTierPermissionApiServiceImpl;

public class UpdateTierPermissionApiServiceFactory {

   private final static UpdateTierPermissionApiService service = new UpdateTierPermissionApiServiceImpl();

   public static UpdateTierPermissionApiService getUpdateTierPermissionApi()
   {
      return service;
   }
}
