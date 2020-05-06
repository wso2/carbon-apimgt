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

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJAXRSCXFCDIServerCodegen", date = "2020-05-05T16:15:55.172+05:30[Asia/Colombo]")public interface RegistriesApiService {
        public Response addRegistry(RegistryDTO body, MessageContext messageContext) throws APIManagementException;
        public Response getAllEntriesInRegistry(String registryId, MessageContext messageContext) throws APIManagementException;
        public Response getRegistries(MessageContext messageContext) throws APIManagementException;
        public Response getRegistryByUUID(String registryId, MessageContext messageContext) throws APIManagementException;
        public Response registriesRegistryIdEntryPost(String registryId, RegistryEntryDTO registryEntry, InputStream definitionFileInputStream, Attachment definitionFileDetail, MessageContext messageContext) throws APIManagementException;
}
