/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.rest.api.endpoint.registry.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.EndpointRegistry;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.RegistriesApiService;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryArrayDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryEntryArrayDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryEntryDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.util.EndpointRegistryMappingUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.InputStream;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.core.Response;

@RequestScoped
public class RegistriesApiServiceImpl implements RegistriesApiService {

    private static final Log log = LogFactory.getLog(RegistriesApiServiceImpl.class);

    @Override
    public Response getAllEntriesInRegistry(String registryId, MessageContext messageContext) {
        RegistryEntryArrayDTO registryEntryArray = new RegistryEntryArrayDTO();
        RegistryEntryDTO registryEntry = new RegistryEntryDTO();
        registryEntry.setEntryName("Pizzashack-endpoint");
        registryEntry.setMetadata("{ \"mutualTLS\" : true }");
        registryEntry.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS);
        registryEntry.setDefinitionUrl("http://localhost/pizzashack?swagger.json");
        registryEntry.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST);
        registryEntry.setServiceUrl("http://localhost/pizzashack");
        registryEntryArray.add(registryEntry);
        return Response.ok().entity(registryEntryArray).build();
    }

    @Override
    public Response getRegistryByUUID(String registryId, MessageContext messageContext) {
        RegistryDTO registry = new RegistryDTO();
        registry.setId("01234567-0123-0123-0123-012345678901");
        registry.setMode(RegistryDTO.ModeEnum.WRITE);
        registry.setType(RegistryDTO.TypeEnum.WSO2);
        registry.setName("Dev Registry");
        return Response.ok().entity(registry).build();
    }

    @Override
    public Response registriesGet(MessageContext messageContext) {
        RegistryArrayDTO registryArray = new RegistryArrayDTO();
        RegistryDTO registry = new RegistryDTO();
        registry.setId("01234567-0123-0123-0123-012345678901");
        registry.setMode(RegistryDTO.ModeEnum.WRITE);
        registry.setType(RegistryDTO.TypeEnum.WSO2);
        registry.setName("Dev Registry");
        registryArray.add(registry);
        return Response.ok().entity(registry).build();
    }

    @Override
    public Response addRegistry(RegistryDTO body, MessageContext messageContext) {
        String user = RestApiUtil.getLoggedInUsername();
        EndpointRegistry registry = EndpointRegistryMappingUtils.fromDTOtoEndpointRegistry(body, user);
        EndpointRegistry createdRegistry = new EndpointRegistry();
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String registryId = apiProvider.addEndpointRegistry(registry);
            createdRegistry = apiProvider.getEndpointRegistryByUUID(registryId);
        } catch (APIMgtResourceAlreadyExistsException e) {
            RestApiUtil.handleResourceAlreadyExistsError("Endpoint Registry with name '" + body.getName()
                    + "' already exists", e, log);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while adding new endpoint registry: "
                    + registry.getName(), e, log);
        }
        return Response.ok().entity(createdRegistry).build();
    }

    @Override
    public Response registriesRegistryIdEntryPost(String registryId, RegistryEntryDTO registryEntry, InputStream
            definitionFileInputStream, Attachment definitionFileDetail, MessageContext messageContext) {
        return Response.ok().entity(registryEntry).build();
    }
}
