/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.util.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.KeyManagerApplicationConfigValidator;
import org.wso2.carbon.apimgt.api.model.AppConfigConstraintType;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.kmvalidator.KeyManagerApplicationConfigValidatorFactory;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import com.google.gson.Gson;

import java.util.*;

/**
 * This class contains REST API Store related utility operations
 */
public class RestAPIStoreUtils {
    private static final Log log = LogFactory.getLog(RestAPIStoreUtils.class);
    private static boolean isStoreCacheEnabled;

    static {
        APIManagerConfiguration apiManagerConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String isStoreCacheEnabledConfiguration = apiManagerConfiguration
                .getFirstProperty(APIConstants.SCOPE_CACHE_ENABLED);
        isStoreCacheEnabled =
                isStoreCacheEnabledConfiguration != null && Boolean.parseBoolean(isStoreCacheEnabledConfiguration);
    }

    /**
     * check whether current logged in user has access to the specified application
     *
     * @param application Application object
     * @return true if current logged in consumer has access to the specified application
     */
    public static boolean isUserAccessAllowedForApplication(Application application) {
        String groupId;

        if (application != null) {
            groupId = application.getGroupId();
            //If application  subscriber and the current logged in user  same then user can retrieve application
            // irrespective of the groupId
            if (application.getSubscriber() != null && isUserOwnerOfApplication(application)) {
                return true;
            }
            // Check for shared apps
            if (!StringUtils.isEmpty(groupId)) {
                String userGroupId = RestApiUtil.getLoggedInUserGroupId();
                //Check whether there is a common groupId between user and application
                if (userGroupId != null) {
                    List<String> groupIdList = new ArrayList<>(
                            Arrays.asList(groupId.split(APIConstants.MULTI_ATTRIBUTE_SEPARATOR_DEFAULT)));
                    for (String id : userGroupId.split(APIConstants.MULTI_ATTRIBUTE_SEPARATOR_DEFAULT)) {
                        if (groupIdList.contains(id)) {
                            return true;
                        }
                    }

                }
            }
        }
        //user don't have access
        return false;
    }

    /**
     * check whether current logged in user is the owner of the application
     *
     * @param application Application object
     * @return true if current logged in consumer is the owner of the specified application
     */
    public static boolean isUserOwnerOfApplication(Application application) {
        String username = RestApiCommonUtil.getLoggedInUsername();

        if (application.getSubscriber().getName().equals(username)) {
            return true;
        } else if (application.getSubscriber().getName().toLowerCase().equals(username.toLowerCase())) {
            APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String comparisonConfig = configuration
                    .getFirstProperty(APIConstants.API_STORE_FORCE_CI_COMPARISIONS);
            return (StringUtils.isNotEmpty(comparisonConfig) && Boolean.valueOf(comparisonConfig));
        }

        return false;
    }

    /**
     * check whether an application is shared with the current logged-in user
     *
     * @param application Application object
     * @return true if the application is shared with the current logged-in user
     */
    public static boolean isApplicationSharedtoUser(Application application) {
        boolean multiGroupAppSharingEnabled = APIUtil.isMultiGroupAppSharingEnabled();
        if (multiGroupAppSharingEnabled) {
            String groupId = application.getGroupId();
            String userGroupId = RestApiUtil.getLoggedInUserGroupId();
            if (groupId != null && userGroupId != null) {
                String[] grpIdArray = groupId.split(",");
                for (String id : grpIdArray) {
                    if (id.equals(userGroupId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check whether the specified API exists and the current logged in user has access to it.
     * <p>
     * When it tries to retrieve the resource from the registry, it will fail with AuthorizationFailedException if user
     * does not have enough privileges. If the API does not exist, this will throw a APIMgtResourceNotFoundException
     *
     * @param apiId API UUID
     * @param organization Identifier of the organization
     * @throws APIManagementException
     */
    public static boolean isUserAccessAllowedForAPIByUUID(String apiId, String organization) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer consumer = RestApiCommonUtil.getLoggedInUserConsumer();
        //this is just to check whether the user has access to the api or the api exists. 
        try {
            consumer.getLightweightAPIByUUID(apiId, organization);
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                String message =
                        "user " + username + " failed to access the API " + apiId + " due to an authorization failure";
                log.info(message);
                return false;
            } else {
                //This is an unexpected failure
                String message =
                        "Failed to retrieve the API " + apiId + " to check user " + username + " has access to the API";
                throw new APIManagementException(message, e);
            }
        }
        return true;
    }

    /**
     * Check whether the specified API exists and the current logged in user has access to it.
     * <p>
     * When it tries to retrieve the resource from the registry, it will fail with AuthorizationFailedException if user
     * does not have enough privileges. If the API does not exist, this will throw a APIMgtResourceNotFoundException
     *
     * @param apiId API identifier
     * @throws APIManagementException
     */
    public static boolean isUserAccessAllowedForAPI(APIIdentifier apiId) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        //this is just to check whether the user has access to the api or the api exists. 
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            String organization = ApiMgtDAO.getInstance().getOrganizationByAPIUUID(apiId.getUUID());
            apiConsumer.getLightweightAPIByUUID(apiId.getUUID(), organization);
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                String message =
                        "user " + username + " failed to access the API " + apiId + " due to an authorization failure";
                log.info(message);
                return false;
            } else {
                //This is an unexpected failure
                String message =
                        "Failed to retrieve the API " + apiId + " to check user " + username + " has access to the API";
                throw new APIManagementException(message, e);
            }
        }
        return true;
    }

    /**
     * Check whether user is allowed to access api product
     * @param product
     * @return
     * @throws APIManagementException
     */
    public static boolean isUserAccessAllowedForAPIProduct(APIProduct product) throws APIManagementException {
        //TODO check whether the username has external domain info as well
        String username = RestApiCommonUtil.getLoggedInUsername();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        if (log.isDebugEnabled()) {
            log.debug("isUserAccessAllowedForAPIProduct():- productId: " + product.getUuid() + ", visibility: "
                    + product.getVisibility() + " username:" + username + " tenantDomain:" + tenantDomain);
        }
        if (APIConstants.API_GLOBAL_VISIBILITY.equals(product.getVisibility())) {
            return true;
        } else if (APIConstants.API_RESTRICTED_VISIBILITY.equals(product.getVisibility())) {
            if (APIUtil.isRoleExistForUser(username, product.getVisibleRoles())
                    && tenantDomain.equals(product.getTenantDomain())) {
                return true;
            }
        } else if (APIConstants.API_PRIVATE_VISIBILITY.equals(product.getVisibility())
                && tenantDomain.equals(product.getTenantDomain())
                && !APIConstants.WSO2_ANONYMOUS_USER.equals(username)) {
            return true;
        }
        return false;
    }

    /**
     * Validates the application configuration against the constraints defined in the Key Manager.
     *
     * @param keyManager           Key Manager name or UUID
     * @param additionalProperties Additional properties from the request (Map or JSON string)
     * @throws APIManagementException If validation fails
     */
    public static void validateKeyManagerAppConfiguration(String keyManager, Object additionalProperties) throws APIManagementException {

        KeyManagerConfigurationDTO kmConfig = ApiMgtDAO.getInstance().getKeyManagerConfigurationByUUID(keyManager);
        if (kmConfig == null) {
            return;
        }
        Map<String, Object> kmProps = kmConfig.getAdditionalProperties();
        if (kmProps == null) {
            return;
        }
        Object constraintsObj = kmProps.get(APIConstants.KeyManager.CONSTRAINTS);
        if (constraintsObj == null) {
            return;
        }
        Map<String, Map<String, Object>> constraintsMap;
        if (constraintsObj instanceof Map) {
            constraintsMap = (Map<String, Map<String, Object>>) constraintsObj;
        } else {
             return;
        }
        Map<String, Object> inputProps;
        if (additionalProperties instanceof Map) {
            inputProps = (Map<String, Object>) additionalProperties;
        } else {
            return;
        }
        // for each of constraint check if that field (the key) exist in additionalProperties
        for (Map.Entry<String, Map<String, Object>> entry : constraintsMap.entrySet()) {
            String fieldName = entry.getKey();
            Map<String, Object> constraintConfig = entry.getValue();
            if (constraintConfig == null) {
                continue;
            }
            if (!inputProps.containsKey(fieldName)) {
                throw new APIManagementException("Missing input for constrained property: " + fieldName,
                        ExceptionCodes.from(ExceptionCodes.INVALID_APPLICATION_ADDITIONAL_PROPERTIES,
                                "Missing input for constrained property: " + fieldName));
            }
            Object inputValue = inputProps.get(fieldName);
            String constraintTypeStr = (String) constraintConfig.get("type");
            AppConfigConstraintType constraintType = AppConfigConstraintType.fromString(constraintTypeStr);
            if (constraintType == null) {
                // Ignore unknown types
                continue;
            }
            KeyManagerApplicationConfigValidator validator = KeyManagerApplicationConfigValidatorFactory
                    .getValidator(constraintType);
            if (validator != null) {
                Object constraintValue = constraintConfig.get("value");
                Map<String, Object> constraints = Collections.emptyMap();
                if (constraintValue instanceof Map) {
                    constraints = (Map<String, Object>) constraintValue;
                } else if (constraintValue != null) {
                    constraints = new Gson().fromJson(constraintValue.toString(), Map.class);
                }
                if (!validator.validate(inputValue, constraints)) {
                    String fieldError = "Property '" + fieldName + "' is invalid. " + validator.getErrorMessage();
                    log.error("Validation failed for property '" + fieldName + "': " + validator.getErrorMessage());
                    throw new APIManagementException(fieldError,
                            ExceptionCodes.from(ExceptionCodes.INVALID_APPLICATION_ADDITIONAL_PROPERTIES, fieldError));
                }
            }
        }
    }
}
