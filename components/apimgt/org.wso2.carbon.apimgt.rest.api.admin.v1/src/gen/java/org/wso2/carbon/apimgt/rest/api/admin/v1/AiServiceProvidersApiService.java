package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AIServiceProviderResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AIServiceProviderSummaryResponseListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import java.io.File;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface AiServiceProvidersApiService {
      public Response addAIServiceProvider(String name, String apiVersion, String description, String multitpleModelProviderSupport, String configurations, InputStream apiDefinitionInputStream, Attachment apiDefinitionDetail, String modelProviders, MessageContext messageContext) throws APIManagementException;
      public Response deleteAIServiceProvider(String aiServiceProviderId, MessageContext messageContext) throws APIManagementException;
      public Response getAIServiceProvider(String aiServiceProviderId, MessageContext messageContext) throws APIManagementException;
      public Response getAIServiceProviders(MessageContext messageContext) throws APIManagementException;
      public Response updateAIServiceProvider(String aiServiceProviderId, String name, String apiVersion, String description, String multitpleModelProviderSupport, String configurations, InputStream apiDefinitionInputStream, Attachment apiDefinitionDetail, String modelProviders, MessageContext messageContext) throws APIManagementException;
}
