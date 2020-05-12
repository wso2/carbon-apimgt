package org.wso2.carbon.apimgt.rest.api.endpoint.registry;

import org.wso2.carbon.apimgt.rest.api.endpoint.registry.*;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryArrayDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryEntryArrayDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryEntryDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJAXRSCXFCDIServerCodegen", date = "2020-05-12T10:58:45.332+05:30[Asia/Colombo]")public interface RegistriesApiService {
        public Response addRegistry(RegistryDTO body, MessageContext messageContext) throws APIManagementException;
        public Response createRegistryEntry(String registryId, RegistryEntryDTO registryEntry, InputStream definitionFileInputStream, Attachment definitionFileDetail, MessageContext messageContext) throws APIManagementException;
        public Response deleteRegistry(String registryId, MessageContext messageContext) throws APIManagementException;
        public Response deleteRegistryEntry(String registryId, String entryId, MessageContext messageContext) throws APIManagementException;
        public Response getAllEntriesInRegistry(String registryId, String query, String sortBy, String sortOrder, MessageContext messageContext) throws APIManagementException;
        public Response getRegistries(String query, String sortBy, String sortOrder, MessageContext messageContext) throws APIManagementException;
        public Response getRegistryByUUID(String registryId, MessageContext messageContext) throws APIManagementException;
        public Response getRegistryEntryByUuid(String registryId, String entryId, MessageContext messageContext) throws APIManagementException;
        public Response updateRegistry(String registryId, RegistryDTO body, MessageContext messageContext) throws APIManagementException;
        public Response updateRegistryEntry(String registryId, String entryId, RegistryEntryDTO registryEntry, InputStream definitionFileInputStream, Attachment definitionFileDetail, MessageContext messageContext) throws APIManagementException;
}
