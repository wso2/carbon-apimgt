package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.*;
import org.wso2.carbon.apimgt.governance.rest.api.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;

import org.wso2.carbon.apimgt.governance.rest.api.dto.DevportalGovernanceTemplateDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.DevportalGovernanceTemplateListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.TemplateDefaultViolationListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface TemplatesApiService {
      public Response createDevportalGovernanceTemplate(DevportalGovernanceTemplateDTO devportalGovernanceTemplateDTO, MessageContext messageContext) throws APIMGovernanceException;
      public Response deleteDevportalGovernanceTemplate(String templateId, MessageContext messageContext) throws APIMGovernanceException;
      public Response getDefaultDevportalGovernanceTemplate(MessageContext messageContext) throws APIMGovernanceException;
      public Response getDevportalGovernanceTemplateById(String templateId, MessageContext messageContext) throws APIMGovernanceException;
      public Response getDevportalGovernanceTemplates(Integer limit, Integer offset, MessageContext messageContext) throws APIMGovernanceException;
      public Response updateDevportalGovernanceTemplateById(String templateId, DevportalGovernanceTemplateDTO devportalGovernanceTemplateDTO, MessageContext messageContext) throws APIMGovernanceException;
      public Response validateTemplateDefaults(String templateId, MessageContext messageContext) throws APIMGovernanceException;
}
