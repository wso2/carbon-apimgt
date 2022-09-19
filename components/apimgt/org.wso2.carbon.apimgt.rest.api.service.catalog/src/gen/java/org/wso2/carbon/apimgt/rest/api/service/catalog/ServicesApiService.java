package org.wso2.carbon.apimgt.rest.api.service.catalog;


import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceDTO;

import java.io.InputStream;

import javax.ws.rs.core.Response;


public interface ServicesApiService {
      public Response addService(ServiceDTO serviceMetadata, InputStream definitionFileInputStream, Attachment definitionFileDetail, String inlineContent, MessageContext messageContext) throws APIManagementException;
      public Response deleteService(String serviceId, MessageContext messageContext) throws APIManagementException;
      public Response exportService(String name, String version, MessageContext messageContext) throws APIManagementException;
      public Response getServiceById(String serviceId, MessageContext messageContext) throws APIManagementException;
      public Response getServiceDefinition(String serviceId, MessageContext messageContext) throws APIManagementException;
      public Response getServiceUsage(String serviceId, MessageContext messageContext) throws APIManagementException;
      public Response importService(InputStream fileInputStream, Attachment fileDetail, Boolean overwrite, String verifier, MessageContext messageContext) throws APIManagementException;
      public Response searchServices(String name, String version, String definitionType, String key, Boolean shrink, String sortBy, String sortOrder, Integer limit, Integer offset, MessageContext messageContext) throws APIManagementException;
      public Response updateService(String serviceId, ServiceDTO serviceMetadata, InputStream definitionFileInputStream, Attachment definitionFileDetail, String inlineContent, MessageContext messageContext) throws APIManagementException;
}
