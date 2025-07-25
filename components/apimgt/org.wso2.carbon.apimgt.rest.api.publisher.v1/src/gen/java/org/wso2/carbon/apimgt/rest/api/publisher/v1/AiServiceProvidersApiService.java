package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AIServiceProviderEndpointConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AIServiceProviderResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AIServiceProviderSummaryResponseListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ModelProviderDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface AiServiceProvidersApiService {
      public Response getAIServiceProvider(String aiServiceProviderId, MessageContext messageContext) throws APIManagementException;
      public Response getAIServiceProviderApiDefinition(String aiServiceProviderId, MessageContext messageContext) throws APIManagementException;
      public Response getAIServiceProviderEndpointConfiguration(String aiServiceProviderId, MessageContext messageContext) throws APIManagementException;
      public Response getAIServiceProviderModels(String aiServiceProviderId, MessageContext messageContext) throws APIManagementException;
      public Response getAIServiceProviders(MessageContext messageContext) throws APIManagementException;
}
