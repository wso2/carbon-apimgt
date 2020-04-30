/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.endpoint.registry.v1.impl;

import org.wso2.carbon.apimgt.rest.api.endpoint.registry.v1.*;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.endpoint.registry.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.v1.dto.RegistryArrayDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.v1.dto.RegistryDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.v1.dto.RegistryEntryDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.v1.dto.RegistryEntryListDTO;


import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class RegistriesApiServiceImpl implements RegistriesApiService {

      public Response addRegistry(RegistryDTO body, MessageContext messageContext) {
      RegistryDTO registryDTO = new RegistryDTO();
      registryDTO.setId(body.getId());
      registryDTO.setMode(body.getMode());
      registryDTO.setType(body.getType());
      registryDTO.setName(body.getName());
      return Response.ok().entity(registryDTO).build();
  }
      public Response addRegistryEntry(String registryId, RegistryEntryDTO registryEntry, MessageContext messageContext) {
      return Response.ok().entity(registryEntry).build();
  }
      public Response getAllEntriesInRegistry(String registryId, MessageContext messageContext) {
      RegistryEntryListDTO registryEntryList = new RegistryEntryListDTO();
      RegistryEntryDTO registryEntryDTO = new RegistryEntryDTO();
      registryEntryDTO.setEntryName("Pizzashack-endpoint");
      registryEntryDTO.setMetadata("{ \"mutualTLS\" : true }");
      registryEntryDTO.setDefinitionType(RegistryEntryDTO.DefinitionTypeEnum.OAS);
      registryEntryDTO.setDefinitionUrl("http://localhost/pizzashack?swagger.json");
      registryEntryDTO.setServiceType(RegistryEntryDTO.ServiceTypeEnum.REST);
      registryEntryDTO.setServiceUrl("http://localhost/pizzashack");
      registryEntryList.add(registryEntryDTO);
      return Response.ok().entity(registryEntryList).build();
  }
      public Response getAllRegistries(MessageContext messageContext) {
      RegistryArrayDTO registryArrayDTO = new RegistryArrayDTO();
      RegistryDTO registryDTO = new RegistryDTO();
      registryDTO.setId("01234567-0123-0123-0123-012345678901");
      registryDTO.setMode(RegistryDTO.ModeEnum.READONLY);
      registryDTO.setType(RegistryDTO.TypeEnum.WSO2);
      registryDTO.setName("Dev Registry");
      registryArrayDTO.add(registryDTO);
      return Response.ok().entity(registryArrayDTO).build();
  }
      public Response getRegistryByUUID(String registryId, MessageContext messageContext) {
      RegistryDTO registryDTO = new RegistryDTO();
      registryDTO.setId("01234567-0123-0123-0123-012345678901");
      registryDTO.setMode(RegistryDTO.ModeEnum.READONLY);
      registryDTO.setType(RegistryDTO.TypeEnum.WSO2);
      registryDTO.setName("Dev Registry");
      return Response.ok().entity(registryDTO).build();
  }
}
