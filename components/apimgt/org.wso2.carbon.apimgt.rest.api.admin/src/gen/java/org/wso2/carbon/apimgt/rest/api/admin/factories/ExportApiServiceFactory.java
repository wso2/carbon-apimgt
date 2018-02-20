package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.ExportApiServiceImpl;

public class ExportApiServiceFactory {

   private final static ExportApiService service = new ExportApiServiceImpl();

   public static ExportApiService getExportApi()
   {
      return service;
   }
}
