package org.wso2.carbon.apimgt.rest.api.publisher.v1.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.LabelsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.LabelsApiServiceImpl;

public class LabelsApiServiceFactory {

   private final static LabelsApiService service = new LabelsApiServiceImpl();

   public static LabelsApiService getLabelsApi()
   {
      return service;
   }
}
