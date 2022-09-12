package org.wso2.carbon.apimgt.rest.api.admin.v1;


import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowDTO;

import javax.ws.rs.core.Response;


public interface WorkflowsApiService {
      public Response workflowsExternalWorkflowRefGet(String externalWorkflowRef, MessageContext messageContext) throws APIManagementException;
      public Response workflowsGet(Integer limit, Integer offset, String accept, String workflowType, MessageContext messageContext) throws APIManagementException;
      public Response workflowsUpdateWorkflowStatusPost(String workflowReferenceId, WorkflowDTO workflowDTO, MessageContext messageContext) throws APIManagementException;
}
