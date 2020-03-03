package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class WorkflowsApiService {
    public abstract Response workflowsExternalWorkflowRefGet(String externalWorkflowRef,String ifNoneMatch);
    public abstract Response workflowsGet(Integer limit,Integer offset,String accept,String ifNoneMatch,String workflowType);
    public abstract Response workflowsUpdateWorkflowStatusPost(String workflowReferenceId,WorkflowDTO body);
}

