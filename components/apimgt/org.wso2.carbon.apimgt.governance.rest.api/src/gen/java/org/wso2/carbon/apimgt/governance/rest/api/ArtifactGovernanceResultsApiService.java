package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.*;
import org.wso2.carbon.apimgt.governance.rest.api.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactGovernanceResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactGovernanceResultListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactGovernanceResultsSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ArtifactGovernanceResultsApiService {
      public Response getArtifactGovernanceResults(Integer limit, Integer offset, MessageContext messageContext) throws GovernanceException;
      public Response getArtifactGovernanceResultsSummary(MessageContext messageContext) throws GovernanceException;
      public Response getGovernanceResultsByArtifactId(String artifactId, MessageContext messageContext) throws GovernanceException;
}
