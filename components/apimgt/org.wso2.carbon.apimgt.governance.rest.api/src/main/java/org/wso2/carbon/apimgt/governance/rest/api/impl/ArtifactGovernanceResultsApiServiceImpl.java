package org.wso2.carbon.apimgt.governance.rest.api.impl;

import org.wso2.carbon.apimgt.governance.rest.api.*;
import org.wso2.carbon.apimgt.governance.rest.api.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactGovernanceResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactGovernanceResultListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactGovernanceResultsSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class ArtifactGovernanceResultsApiServiceImpl implements ArtifactGovernanceResultsApiService {

    public Response getArtifactGovernanceResults(Integer limit, Integer offset, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response getArtifactGovernanceResultsSummary(MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response getGovernanceResultsByArtifactId(String artifactId, MessageContext messageContext) {

        ArtifactGovernanceResultDTO artifactGovernanceResultDTO = new ArtifactGovernanceResultDTO();

        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }
}
