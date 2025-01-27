package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.*;
import org.wso2.carbon.apimgt.governance.rest.api.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceDetailsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetValidationResultDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ArtifactComplianceApiService {
      public Response getArtifactComplianceByArtifactId(String artifactId, MessageContext messageContext) throws GovernanceException;
      public Response getArtifactComplianceForAllArtifacts(Integer limit, Integer offset, String artifactType, MessageContext messageContext) throws GovernanceException;
      public Response getArtifactComplianceSummary(String artifactType, MessageContext messageContext) throws GovernanceException;
      public Response getRulesetValidationResultsByArtifactId(String artifactId, String rulesetId, MessageContext messageContext) throws GovernanceException;
}
