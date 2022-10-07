/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.apimgt.rest.api.admin.v1.common.impl;

import org.wso2.apk.apimgt.api.APIAdmin;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.apk.apimgt.rest.api.admin.v1.common.utils.mappings.EnvironmentMappingUtil;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.EnvironmentDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.EnvironmentListDTO;

import java.util.List;

public class EnvironmentsCommonImpl {
    private EnvironmentsCommonImpl() {
    }

    /**
     * Delete gateway environment
     *
     * @param environmentId Environment ID
     * @param organization  Tenant organization
     * @throws APIManagementException When an internal error occurs
     */
    public static void removeEnvironment(String environmentId, String organization) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        apiAdmin.deleteEnvironment(organization, environmentId);
    }

    /**
     * Update gateway environment
     *
     * @param environmentId Environment ID
     * @param body          Environment details
     * @param organization  Tenant organization
     * @throws APIManagementException When an internal error occurs
     */
    public static void updateEnvironment(String environmentId, EnvironmentDTO body, String organization)
            throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        body.setId(environmentId);
        Environment env = EnvironmentMappingUtil.fromEnvDtoToEnv(body);
        apiAdmin.updateEnvironment(organization, env);
    }

    /**
     * Get list of gateway environments from config api-manager.xml and dynamic environments (from DB)
     *
     * @param organization Tenant organization
     * @return List of environments
     * @throws APIManagementException When an internal error occurs
     */
    public static EnvironmentListDTO getEnvironments(String organization) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        List<Environment> envList = apiAdmin.getAllEnvironments(organization);
        return EnvironmentMappingUtil.fromEnvListToEnvListDTO(envList);
    }

    /**
     * Create a dynamic gateway environment
     *
     * @param body         Environment details
     * @param organization Tenant organization
     * @return Created environment details
     * @throws APIManagementException When an internal error occurs
     */
    public static EnvironmentDTO addEnvironment(EnvironmentDTO body, String organization)
            throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        Environment env = EnvironmentMappingUtil.fromEnvDtoToEnv(body);
        return EnvironmentMappingUtil.fromEnvToEnvDTO(apiAdmin.addEnvironment(organization, env));
    }
}
