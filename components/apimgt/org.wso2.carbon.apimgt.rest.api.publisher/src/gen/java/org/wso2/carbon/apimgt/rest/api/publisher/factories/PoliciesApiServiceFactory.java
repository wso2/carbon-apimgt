package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.PoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.PoliciesApiServiceImpl;

public class PoliciesApiServiceFactory {

   private final static PoliciesApiService service = new PoliciesApiServiceImpl();

   public static PoliciesApiService getPoliciesApi()
   {
      return service;
   }
}
