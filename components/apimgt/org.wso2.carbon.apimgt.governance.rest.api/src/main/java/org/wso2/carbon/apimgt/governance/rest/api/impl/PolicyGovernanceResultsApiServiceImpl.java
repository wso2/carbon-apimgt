package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.wso2.carbon.apimgt.governance.rest.api.*;
import org.wso2.carbon.apimgt.governance.rest.api.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyGovernanceResultListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyGovernanceResultWithArtifactsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyGovernanceResultsSummaryDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class PolicyGovernanceResultsApiServiceImpl implements PolicyGovernanceResultsApiService {

    public Response getGovernanceResultsByPolicyId(String policyId, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response getGovernanceResultsForAllPolicies(Integer limit, Integer offset, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response getPolicyGovernanceResultsSummary(MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }
}
