package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.*;
import org.wso2.carbon.apimgt.governance.rest.api.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface RulesetsApiService {
      public Response createRuleset(String name, InputStream rulesetContentInputStream, Attachment rulesetContentDetail, String ruleType, String artifactType, String provider, String description, String ruleCategory, String documentationLink, MessageContext messageContext) throws GovernanceException;
      public Response deleteRuleset(String rulesetId, MessageContext messageContext) throws GovernanceException;
      public Response getRulesetById(String rulesetId, MessageContext messageContext) throws GovernanceException;
      public Response getRulesetContent(String rulesetId, MessageContext messageContext) throws GovernanceException;
      public Response getRulesetUsage(String rulesetId, MessageContext messageContext) throws GovernanceException;
      public Response getRulesets(Integer limit, Integer offset, MessageContext messageContext) throws GovernanceException;
      public Response updateRulesetById(String rulesetId, String name, InputStream rulesetContentInputStream, Attachment rulesetContentDetail, String ruleType, String artifactType, String provider, String description, String ruleCategory, String documentationLink, MessageContext messageContext) throws GovernanceException;
}
