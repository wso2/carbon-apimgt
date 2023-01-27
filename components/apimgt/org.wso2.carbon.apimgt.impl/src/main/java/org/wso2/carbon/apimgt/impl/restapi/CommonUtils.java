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

package org.wso2.carbon.apimgt.impl.restapi;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIInfo;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is used to have the functionalities common to all REST API utils
 */
public class CommonUtils {

    private CommonUtils() {
    }

    /**
     * @return API Provider Impl
     * @throws APIManagementException when getting provider fails
     */
    public static APIProvider getLoggedInUserProvider() throws APIManagementException {

        return APIManagerFactory.getInstance().getAPIProvider(getLoggedInUsername());
    }

    /**
     * @return The logged-in user
     */
    public static String getLoggedInUsername() {
        return CarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    /**
     * @param apiId UUID of the API
     * @return API details
     * @throws APIManagementException when API does not exist in the DB
     */
    public static APIInfo validateAPIExistence(String apiId) throws APIManagementException {
        APIProvider apiProvider = getLoggedInUserProvider();
        APIInfo apiInfo = apiProvider.getAPIInfoByUUID(apiId);
        if (apiInfo == null) {
            throw new APIManagementException("Couldn't retrieve existing API with API UUID: "
                    + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                    apiId));
        }
        return apiInfo;
    }

    public static String constructEndpointConfigForService(String serviceUrl, String protocol) {

        StringBuilder sb = new StringBuilder();
        String endpointType = Constants.TypeEnum.HTTP.value().toLowerCase();
        if (StringUtils.isNotEmpty(protocol) && (Constants.TypeEnum.SSE.equals(protocol.toUpperCase())
                || Constants.TypeEnum.WS.equals(protocol.toUpperCase()))) {
            endpointType = "ws";
        }
        if (StringUtils.isNotEmpty(serviceUrl)) {
            sb.append("{\"endpoint_type\": \"")
                    .append(endpointType)
                    .append("\",")
                    .append("\"production_endpoints\": {\"url\": \"")
                    .append(serviceUrl)
                    .append("\"}}");
        } // TODO Need to check on the endpoint security
        return sb.toString();
    }

    public static void validateScopes(API api, APIProvider apiProvider, String username) throws APIManagementException {

        int tenantId = APIUtil.getInternalOrganizationId(api.getOrganization());
        String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
        Set<Scope> sharedAPIScopes = new HashSet<>();

        for (Scope scope : api.getScopes()) {
            String scopeName = scope.getKey();
            if (!(APIUtil.isAllowedScope(scopeName))) {
                // Check if each scope key is already assigned as a local scope to a different API which is also not a
                // different version of the same API. If true, return error.
                // If false, check if the scope key is already defined as a shared scope. If so, do not honor the
                // other scope attributes (description, role bindings) in the request payload, replace them with
                // already defined values for the existing shared scope.
                if (apiProvider.isScopeKeyAssignedLocally(api.getId().getApiName(), scopeName, api.getOrganization())) {
                    throw new APIManagementException(
                            "Scope " + scopeName + " is already assigned locally by another API",
                            ExceptionCodes.SCOPE_ALREADY_ASSIGNED);
                } else if (apiProvider.isSharedScopeNameExists(scopeName, tenantId)) {
                    sharedAPIScopes.add(scope);
                    continue;
                }
            }

            //set display name as empty if it is not provided
            if (StringUtils.isBlank(scope.getName())) {
                scope.setName(scopeName);
            }

            //set description as empty if it is not provided
            if (StringUtils.isBlank(scope.getDescription())) {
                scope.setDescription("");
            }
            validateScopeRoles(scope, username);
        }

        apiProvider.validateSharedScopes(sharedAPIScopes, tenantDomain);
    }

    /**
     * @param scope    Scope whose roles should be validated
     * @param username Username
     * @throws APIManagementException when role validation fails
     */
    private static void validateScopeRoles(Scope scope, String username) throws APIManagementException {
        if (scope.getRoles() != null) {
            for (String aRole : scope.getRoles().split(",")) {
                boolean isValidRole = APIUtil.isRoleNameExist(username, aRole);
                if (!isValidRole) {
                    throw new APIManagementException("Role '" + aRole + "' does not exist.",
                            ExceptionCodes.ROLE_DOES_NOT_EXIST);
                }
            }
        }

    }
}
