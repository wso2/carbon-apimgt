package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LLMProviderEndpointConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LLMProviderResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LLMProviderSummaryResponseListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface LlmProvidersApiService {
      public Response getLLMProvider(String llmProviderId, MessageContext messageContext) throws APIManagementException;
      public Response getLLMProviderApiDefinition(String llmProviderId, MessageContext messageContext) throws APIManagementException;
      public Response getLLMProviderEndpointConfiguration(String llmProviderId, MessageContext messageContext) throws APIManagementException;
      public Response getLLMProviderModels(String llmProviderId, MessageContext messageContext) throws APIManagementException;
      public Response getLLMProviders(MessageContext messageContext) throws APIManagementException;
}
