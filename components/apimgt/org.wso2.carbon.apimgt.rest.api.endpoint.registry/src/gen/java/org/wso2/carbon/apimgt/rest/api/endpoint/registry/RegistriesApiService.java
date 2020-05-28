package org.wso2.carbon.apimgt.rest.api.endpoint.registry;

import org.wso2.carbon.apimgt.rest.api.endpoint.registry.*;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.impl.endpoint.registry.api.EndpointRegistryException;

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

import org.wso2.carbon.apimgt.rest.api.endpoint.registry.RegistriesApi.*;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJAXRSCXFCDIServerCodegen", date = "2020-05-28T10:45:00.026+05:30[Asia/Colombo]")public interface RegistriesApiService {
        public Response addRegistry(RegistryDTO body, MessageContext messageContext) throws EndpointRegistryException;
        public Response createNewEntryVersion(String registryId, String entryId, String version, MessageContext messageContext) throws EndpointRegistryException;
        public Response createRegistryEntry(String registryId, RegistryEntryDTO registryEntry, InputStream definitionFileInputStream, Attachment definitionFileDetail, MessageContext messageContext) throws EndpointRegistryException;
        public Response deleteRegistry(String registryId, MessageContext messageContext) throws EndpointRegistryException;
        public Response deleteRegistryEntry(String registryId, String entryId, MessageContext messageContext) throws EndpointRegistryException;
        public Response getAllEntriesInRegistry(String registryId, Boolean exactNameMatch, String version, ServiceTypeEnum serviceType, DefinitionTypeEnum definitionType, String name, ServiceCategoryEnum serviceCategory, SortEntryByEnum sortEntryBy, SortEntryOrderEnum sortEntryOrder, Integer limit, Integer offset, MessageContext messageContext) throws EndpointRegistryException;
        public Response getEndpointDefinition(String registryId, String entryId, MessageContext messageContext) throws EndpointRegistryException;
        public Response getRegistries(MessageContext messageContext) throws EndpointRegistryException;
        public Response getRegistryByUUID(String registryId, MessageContext messageContext) throws EndpointRegistryException;
        public Response getRegistryEntryByUuid(String registryId, String entryId, MessageContext messageContext) throws EndpointRegistryException;
        public Response updateRegistry(RegistryDTO body, String registryId, MessageContext messageContext) throws EndpointRegistryException;
        public Response updateRegistryEntry(String registryId, String entryId, RegistryEntryDTO registryEntry, InputStream definitionFileInputStream, Attachment definitionFileDetail, MessageContext messageContext) throws EndpointRegistryException;
}
