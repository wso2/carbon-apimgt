/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.KeyManagersApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.KeyManagerMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.List;

import javax.ws.rs.core.Response;

public class KeyManagersApiServiceImpl implements KeyManagersApiService {

    @Override
    public Response getAllKeyManagers(MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);
        APIAdmin apiAdmin = new APIAdminImpl();
        List<KeyManagerConfigurationDTO> keyManagerConfigurations =
                apiAdmin.getKeyManagerConfigurationsByOrganization(organization);
        List<KeyManagerConfigurationDTO> globalKeyManagerConfigurations = apiAdmin.getGlobalKeyManagerConfigurations();
        keyManagerConfigurations.addAll(globalKeyManagerConfigurations);
        return Response.ok(KeyManagerMappingUtil.toKeyManagerListDto(keyManagerConfigurations)).build();
    }
}
