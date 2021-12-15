package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDefinitionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDefinitionsListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface OperationPolicyTemplatesApiService {
      public Response addOperationPolicyTemplate(InputStream templateSpecFileInputStream, Attachment templateSpecFileDetail, InputStream policyDefinitionFileInputStream, Attachment policyDefinitionFileDetail, String policyName, String flow, MessageContext messageContext) throws APIManagementException;
      public Response getAllOperationPolicyTemplates(Integer limit, Integer offset, String query, MessageContext messageContext) throws APIManagementException;
}
