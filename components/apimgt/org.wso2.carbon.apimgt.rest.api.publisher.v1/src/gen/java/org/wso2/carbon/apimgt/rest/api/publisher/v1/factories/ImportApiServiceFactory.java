package org.wso2.carbon.apimgt.rest.api.publisher.v1.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ImportApiServiceImpl;

public class ImportApiServiceFactory {

   private final static ImportApiService service = new ImportApiServiceImpl();

   public static ImportApiService getImportApi()
   {
      return service;
   }
}
