package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.WorkflowsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.WorkflowsApiServiceImpl;

public class WorkflowsApiServiceFactory {

   private final static WorkflowsApiService service = new WorkflowsApiServiceImpl();

   public static WorkflowsApiService getWorkflowsApi()
   {
      return service;
   }
}
