package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.ResultsApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.ResultsApiServiceImpl;

public class ResultsApiServiceFactory {

   private final static ResultsApiService service = new ResultsApiServiceImpl();

   public static ResultsApiService getResultsApi()
   {
      return service;
   }
}
