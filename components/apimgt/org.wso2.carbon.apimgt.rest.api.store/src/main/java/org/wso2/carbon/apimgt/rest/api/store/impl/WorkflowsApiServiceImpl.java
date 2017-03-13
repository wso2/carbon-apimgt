package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.WorkflowsApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.WorkflowDTO;

import javax.ws.rs.core.Response;

public class WorkflowsApiServiceImpl extends WorkflowsApiService {
    
    private static final Logger log = LoggerFactory.getLogger(WorkflowsApiServiceImpl.class);
    @Override
    public Response workflowsUpdateWorkflowStatusPost(String workflowReferenceId, WorkflowDTO body)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
