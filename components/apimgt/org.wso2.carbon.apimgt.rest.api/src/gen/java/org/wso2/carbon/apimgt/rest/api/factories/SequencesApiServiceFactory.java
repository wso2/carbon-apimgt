package org.wso2.carbon.apimgt.rest.api.factories;

import org.wso2.carbon.apimgt.rest.api.SequencesApiService;
import org.wso2.carbon.apimgt.rest.api.impl.SequencesApiServiceImpl;

public class SequencesApiServiceFactory {

   private final static SequencesApiService service = new SequencesApiServiceImpl();

   public static SequencesApiService getSequencesApi()
   {
      return service;
   }
}
