package org.wso2.carbon.apimgt.rest.api.store.v1.factories;

import org.wso2.carbon.apimgt.rest.api.store.v1.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.ExportApiServiceImpl;

public class ExportApiServiceFactory {

   private final static ExportApiService service = new ExportApiServiceImpl();

   public static ExportApiService getExportApi()
   {
      return service;
   }
}
