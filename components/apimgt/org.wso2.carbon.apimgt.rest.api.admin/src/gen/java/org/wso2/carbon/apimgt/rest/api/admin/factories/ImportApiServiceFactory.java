package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.ImportApiServiceImpl;

public class ImportApiServiceFactory {

   private final static ImportApiService service = new ImportApiServiceImpl();

   public static ImportApiService getImportApi()
   {
      return service;
   }
}
