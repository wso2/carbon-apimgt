package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowRequestDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowResponseDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class WorkflowsApiService {
    public abstract Response workflowsGet(String ifNoneMatch
 ,String ifModifiedSince
 ,String workflowType
 , Request request) throws NotFoundException;
    public abstract Response workflowsWorkflowReferenceIdGet(String workflowReferenceId
 , Request request) throws NotFoundException;
    public abstract Response workflowsWorkflowReferenceIdPut(String workflowReferenceId
 ,WorkflowRequestDTO body
 , Request request) throws NotFoundException;
}
