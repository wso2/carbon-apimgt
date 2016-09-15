package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WorkflowDTO.*;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WorkflowDTO;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.List;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public class WorkflowsApiServiceImpl extends WorkflowsApiService {
    
    private static final Log log = LogFactory.getLog(WorkflowsApiService.class);
    @Override
    public Response workflowsUpdateWorkflowStatusPost(String workflowReferenceId,WorkflowDTO body){
        
        
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

        boolean isTenantFlowStarted = false;
        
        

        try {
            if (workflowReferenceId != null) {
                org.wso2.carbon.apimgt.impl.dto.WorkflowDTO workflowDTO = apiMgtDAO.retrieveWorkflow(workflowReferenceId);
                String tenantDomain = workflowDTO.getTenantDomain();
                if (tenantDomain != null && !org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    isTenantFlowStarted = true;
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                }

                if (workflowDTO == null) {
           ////////////////////////////////////////////////////////////
                }

                workflowDTO.setWorkflowDescription(body.getDescription());
                workflowDTO.setStatus(WorkflowStatus.valueOf(body.getStatus().toString()));
                workflowDTO.setAttributes(body.getAttributes());
                String workflowType = workflowDTO.getWorkflowType();
                WorkflowExecutor workflowExecutor = WorkflowExecutorFactory.getInstance()
                        .getWorkflowExecutor(workflowType);

                workflowExecutor.complete(workflowDTO);
                return Response.ok().entity(body).build(); /////////check
            }
        } catch (APIManagementException e) {
            String msg = "Error while resuming workflow " + workflowReferenceId;
            RestApiUtil.handleInternalServerError(msg, e, log);
        } catch (WorkflowException e) {
            String msg = "Error while resuming workflow " + workflowReferenceId;
            RestApiUtil.handleInternalServerError(msg, e, log);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
       return null;
    }
}
