/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings;

import java.util.ArrayList;
import java.util.List;

import org.wso2.carbon.apimgt.api.dto.OrganizationDetailsDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.OrganizationDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.OrganizationListDTO;

public class OrganizationsMappingUtil {

    public static OrganizationListDTO toOrganizationsListDTO(List<OrganizationDetailsDTO> orgList, String parentOrgId) {
        OrganizationListDTO listDto = new OrganizationListDTO();
        listDto.setCount(orgList.size());
        List<OrganizationDTO> list = new ArrayList<OrganizationDTO>();
        for (OrganizationDetailsDTO organizationDTO : orgList) {
            OrganizationDTO dto = new OrganizationDTO();
            dto.displayName(organizationDTO.getName());
            dto.setOrganizationId(organizationDTO.getOrganizationId());
            dto.setParentOrganizationId(parentOrgId);
            dto.setExternalOrganizationId(organizationDTO.getExternalOrganizationReference());
            dto.setDescription(organizationDTO.getDescription());
            list.add(dto);
        }
        listDto.setList(list);
        return listDto;
    }

    public static OrganizationDetailsDTO toOrganizationDetailsDTO(OrganizationDTO organizationDTO) {
        OrganizationDetailsDTO orgDetailsDto = new OrganizationDetailsDTO();
        orgDetailsDto.setName(organizationDTO.getDisplayName());
        orgDetailsDto.setOrganizationId(organizationDTO.getOrganizationId());
        orgDetailsDto.setExternalOrganizationReference(organizationDTO.getExternalOrganizationId());
        orgDetailsDto.setDescription(organizationDTO.getDescription());
        return orgDetailsDto;
    }

    public static OrganizationDTO toOrganizationsDTO(OrganizationDetailsDTO organizationDetailsDto, String parentOrgId) {
        OrganizationDTO orgDto = new OrganizationDTO();
        orgDto.setDisplayName(organizationDetailsDto.getName());
        orgDto.setOrganizationId(organizationDetailsDto.getOrganizationId());
        orgDto.setParentOrganizationId(parentOrgId);
        orgDto.setExternalOrganizationId(organizationDetailsDto.getExternalOrganizationReference());
        orgDto.setDescription(organizationDetailsDto.getDescription());
        return orgDto;
    }

}
