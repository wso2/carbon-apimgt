package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.*;
import org.wso2.carbon.apimgt.governance.rest.api.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface RulesetsApiService {
      public Response createRuleset(String name, InputStream rulesetContentInputStream, Attachment rulesetContentDetail, String ruleType, String artifactType, String description, String ruleCategory, String documentationLink, String provider, MessageContext messageContext) throws APIMGovernanceException;
      public Response deleteRuleset(String rulesetId, MessageContext messageContext) throws APIMGovernanceException;
      public Response getRulesetById(String rulesetId, MessageContext messageContext) throws APIMGovernanceException;
      public Response getRulesetContent(String rulesetId, MessageContext messageContext) throws APIMGovernanceException;
      public Response getRulesetUsage(String rulesetId, MessageContext messageContext) throws APIMGovernanceException;
      public Response getRulesets(Integer limit, Integer offset, String query, MessageContext messageContext) throws APIMGovernanceException;
      public Response updateRulesetById(String rulesetId, String name, InputStream rulesetContentInputStream, Attachment rulesetContentDetail, String ruleType, String artifactType, String description, String ruleCategory, String documentationLink, String provider, MessageContext messageContext) throws APIMGovernanceException;
}
