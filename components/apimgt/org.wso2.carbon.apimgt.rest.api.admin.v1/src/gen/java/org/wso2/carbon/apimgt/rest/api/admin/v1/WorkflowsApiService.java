package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface WorkflowsApiService {
      public Response workflowsExternalWorkflowRefGet(String externalWorkflowRef, MessageContext messageContext) throws APIManagementException;
      public Response workflowsGet(Integer limit, Integer offset, String accept, String workflowType, MessageContext messageContext) throws APIManagementException;
      public Response workflowsUpdateWorkflowStatusPost(String workflowReferenceId, WorkflowDTO workflowDTO, MessageContext messageContext) throws APIManagementException;
}
