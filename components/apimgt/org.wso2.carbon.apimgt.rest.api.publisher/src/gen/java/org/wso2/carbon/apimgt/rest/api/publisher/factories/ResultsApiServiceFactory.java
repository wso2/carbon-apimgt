package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.ResultsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.ResultsApiServiceImpl;

public class ResultsApiServiceFactory {

   private final static ResultsApiService service = new ResultsApiServiceImpl();

   public static ResultsApiService getResultsApi()
   {
      return service;
   }
}
