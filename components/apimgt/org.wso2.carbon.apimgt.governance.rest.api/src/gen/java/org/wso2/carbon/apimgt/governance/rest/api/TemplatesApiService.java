package org.wso2.carbon.apimgt.governance.rest.api;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.rest.api.dto.DevportalGovernanceTemplateDTO;

import javax.ws.rs.core.Response;

public interface TemplatesApiService {

    Response createDevportalGovernanceTemplate(DevportalGovernanceTemplateDTO templateDTO,
                                               MessageContext messageContext) throws APIMGovernanceException;

    Response deleteDevportalGovernanceTemplate(String templateId, MessageContext messageContext)
            throws APIMGovernanceException;

    Response getDefaultDevportalGovernanceTemplate(MessageContext messageContext) throws APIMGovernanceException;

    Response getDevportalGovernanceTemplateById(String templateId, MessageContext messageContext)
            throws APIMGovernanceException;

    Response getDevportalGovernanceTemplates(Integer limit, Integer offset, MessageContext messageContext)
            throws APIMGovernanceException;

    Response updateDevportalGovernanceTemplateById(String templateId, DevportalGovernanceTemplateDTO templateDTO,
                                                   MessageContext messageContext) throws APIMGovernanceException;
}
