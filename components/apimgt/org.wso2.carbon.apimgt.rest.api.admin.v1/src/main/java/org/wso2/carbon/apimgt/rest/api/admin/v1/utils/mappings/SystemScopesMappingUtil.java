/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ScopeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SystemScopesMappingUtil {

    private static final Log log = LogFactory.getLog(SystemScopesMappingUtil.class);

    /**
     * Convert list of API Scope to ScopeListDTO
     *
     * @param scopeRoleMapping Map of a Role Scope  Mapping
     * @return ScopeListDTO list containing role scope mapping data
     */
    public static ScopeListDTO fromScopeListToScopeListDTO(Map<String, String>  scopeRoleMapping) throws APIManagementException{
        ScopeListDTO scopeListDTO = new ScopeListDTO();
        scopeListDTO.setCount(scopeRoleMapping.size());
        scopeListDTO.setList(fromRoleScopeMapToRoleScopeDTOList(scopeRoleMapping));
        return scopeListDTO;
    }

    /**
     * Converts api scope-role mapping to RoleScopeDTO List.
     *
     * @param scopeRoleMapping Map of a Role Scope  Mapping
     * @return RoleScopeDTO list
     */
    private static List<ScopeDTO> fromRoleScopeMapToRoleScopeDTOList(Map<String, String>  scopeRoleMapping)
            throws APIManagementException {
        List<ScopeDTO> scopeDTOs = new ArrayList<>(scopeRoleMapping.size());
        String [] fileNameArray = {"/admin-api.yaml", "/publisher-api.yaml", "/store-api.yaml"};
            for (String fileName : fileNameArray) {
                Map<String, String> portalScopeList = RestApiUtil.getScopes(fileName);
                for (Map.Entry<String, String>  mapping : portalScopeList.entrySet()) {
                    if (scopeRoleMapping.containsKey(mapping.getKey())) {
                        ScopeDTO roleScopeDTO = new ScopeDTO();
                        roleScopeDTO.setName(mapping.getKey());
                        roleScopeDTO.setRoles(scopeRoleMapping.get(mapping.getKey()));
                        roleScopeDTO.setDescription(mapping.getValue());
                        roleScopeDTO.setTag((fileName.replaceAll("-api.yaml", "")).replaceAll("/", ""));
                        scopeDTOs.add(roleScopeDTO);
                    } else {
                        log.warn("The scope "+ mapping.getKey() +" does not exist in tenant.conf");
                    }
                }
            }
        return scopeDTOs;
    }

}
