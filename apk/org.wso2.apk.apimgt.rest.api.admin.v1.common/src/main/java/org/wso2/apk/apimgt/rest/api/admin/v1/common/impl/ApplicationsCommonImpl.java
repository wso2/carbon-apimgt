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

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.apk.apimgt.api.APIAdmin;
import org.wso2.apk.apimgt.api.APIConsumer;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.model.Application;
import org.wso2.apk.apimgt.api.model.Scope;
import org.wso2.apk.apimgt.impl.APIAdminImpl;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.APIManagerFactory;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.admin.v1.common.utils.mappings.ApplicationMappingUtil;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.ApplicationDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.ApplicationListDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.ScopeInfoDTO;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;
import org.wso2.apk.apimgt.user.ctx.UserContext;
import org.wso2.apk.apimgt.user.mgt.util.UserUtils;
//import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApplicationsCommonImpl {

    private ApplicationsCommonImpl() {
    }

    /**
     * Change application owner
     *
     * @param owner         New owner name
     * @param applicationId Application ID
     * @param organization  Tenant organization
     * @return True if owner was changed successfully
     * @throws APIManagementException When an internal error occurs
     */
    public static boolean changeApplicationOwner(String owner, String applicationId, String organization)
            throws APIManagementException {
        try {
            APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(owner);
            Application application = apiConsumer.getApplicationByUUID(applicationId);
            return apiConsumer.updateApplicationOwner(owner, organization, application);
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while updating application owner " + applicationId, e,
                    ExceptionCodes.ERROR_CHANGING_APP_OWNER);
        }
    }

    /**
     * Remove application
     *
     * @param applicationId Application ID
     * @throws APIManagementException When an internal error occurs
     */
    public static void removeApplication(String applicationId) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
        Application application = apiConsumer.getApplicationByUUID(applicationId);
        if (application != null) {
            apiConsumer.removeApplication(application, application.getOwner());
        } else {
            throw new APIManagementException(ExceptionCodes.APPLICATION_NOT_FOUND);
        }
    }

    /**
     * Get applications by user
     *
     * @param user            Application owner
     * @param limit           Limit
     * @param offset          Offset
     * @param applicationName Application name
     * @param tenantDomain    Tenant domain
     * @param sortBy          Sort By
     * @param sortOrder       Sort Order
     * @return List of application details
     * @throws APIManagementException When an internal error occurs
     */
    public static ApplicationListDTO getApplicationsByUser(String user, Integer limit, Integer offset,
                                                           String applicationName, String tenantDomain,
                                                           String sortBy, String sortOrder)
            throws APIManagementException {
        // To store the initial value of the user (specially if it is null or empty)
        String givenUser = user;
        // if no username provided user associated with access token will be used
        if (user == null || StringUtils.isEmpty(user)) {
            user = RestApiCommonUtil.getLoggedInUsername();
            givenUser = StringUtils.EMPTY;
        }

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        sortOrder = sortOrder != null ? sortOrder : RestApiConstants.DEFAULT_SORT_ORDER;
        sortBy = getApplicationsSortByField(sortBy);
        applicationName = applicationName != null ? applicationName : StringUtils.EMPTY;

        ApplicationListDTO applicationListDTO;
        Application[] allMatchedApps;
        int allApplicationsCount = 0;
        // If no user is passed, get the applications for the tenant (not only for the user)
        APIAdmin apiAdmin = new APIAdminImpl();
        String userOrganization = APIUtil.getTenantDomain(user);
        allMatchedApps = apiAdmin.getApplicationsWithPagination(user, givenUser, userOrganization, limit, offset,
                applicationName, sortBy, sortOrder);
        allApplicationsCount = apiAdmin.getApplicationsCount(userOrganization, givenUser, applicationName);
        applicationListDTO = ApplicationMappingUtil.fromApplicationsToDTO(allMatchedApps);
        ApplicationMappingUtil.setPaginationParams(applicationListDTO, limit, offset, allApplicationsCount);
        return applicationListDTO;
    }

    /**
     * Get application by ID
     *
     * @param applicationId Application ID
     * @param organization  Tenant organization
     * @return Application details
     * @throws APIManagementException When an internal error occurs
     */
    public static ApplicationDTO getApplicationById(String applicationId, String organization)
            throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer = APIManagerFactory.getInstance().getAPIConsumer(username);
        String tenantDomain = UserContext.getThreadLocalUserContext().getOrganization();
        Application application = apiConsumer.getApplicationByUUID(applicationId, organization);
        if (application != null) {
            String applicationTenantDomain = UserUtils.getTenantDomain(application.getOwner());
            //If we need to remove this validation due to cross tenant subscription feature, we have to further validate
            //and verify that the invoking user's tenant domain has an API subscribed by this application
            if (tenantDomain.equals(applicationTenantDomain)) {
                // Remove hidden attributes and set the rest of the attributes from config
                Map<String, String> applicationAttributes =
                        setApplicationAttributes(apiConsumer, application, username);
                application.setApplicationAttributes(applicationAttributes);
                ApplicationDTO applicationDTO = ApplicationMappingUtil.fromApplicationtoDTO(application);
                Set<Scope> scopes = apiConsumer
                        .getScopesForApplicationSubscription(username, application.getId(), organization);
                List<ScopeInfoDTO> scopeInfoList = ApplicationMappingUtil.getScopeInfoDTO(scopes);
                applicationDTO.setSubscriptionScopes(scopeInfoList);
                return applicationDTO;
            }
        } else {
            throw new APIManagementException(ExceptionCodes.APPLICATION_NOT_FOUND);
        }
        return null;
    }

    /**
     * @param sortBy Sort By
     * @return When an internal error occurs
     */
    private static String getApplicationsSortByField(String sortBy) {
        String updatedSortBy = StringUtils.EMPTY;
        // Default sortBy field is name
        if (sortBy == null || "name".equals(sortBy)) {
            updatedSortBy = "NAME";
        } else if ("owner".equals(sortBy)) {
            updatedSortBy = "CREATED_BY";
        }
        return updatedSortBy;
    }

    /**
     * @param apiConsumer API Consumer Impl
     * @param application Application
     * @param username    Username
     * @return Application attributes map
     * @throws APIManagementException When an internal error occurs
     */
    private static Map<String, String> setApplicationAttributes(APIConsumer apiConsumer, Application application,
                                                                String username)
            throws APIManagementException {
        JSONArray applicationAttributesFromConfig = apiConsumer.getAppAttributesFromConfig(username);
        Map<String, String> existingApplicationAttributes = application.getApplicationAttributes();
        Map<String, String> applicationAttributes = new HashMap<>();
        if (existingApplicationAttributes != null && applicationAttributesFromConfig != null) {
            for (Object object : applicationAttributesFromConfig) {
                JSONObject attribute = (JSONObject) object;
                Boolean hidden = (Boolean) attribute.get(APIConstants.ApplicationAttributes.HIDDEN);
                String attributeName = (String) attribute.get(APIConstants.ApplicationAttributes.ATTRIBUTE);
                if (!BooleanUtils.isTrue(hidden)) {
                    String attributeVal = existingApplicationAttributes.get(attributeName);
                    if (attributeVal != null) {
                        applicationAttributes.put(attributeName, attributeVal);
                    } else {
                        applicationAttributes.put(attributeName, "");
                    }
                }
            }
        }
        return applicationAttributes;
    }
}
