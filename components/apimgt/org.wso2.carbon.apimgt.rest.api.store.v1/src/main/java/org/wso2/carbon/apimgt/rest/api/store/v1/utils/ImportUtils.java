/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.store.v1.utils;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.models.ExportedSubscribedAPI;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ImportUtils {
    private static final Log log = LogFactory.getLog(ImportUtils.class);
    private static final String GRANT_TYPE_IMPLICIT = "implicit";
    private static final String GRANT_TYPE_CODE = "code";
    private static final String DEFAULT_TOKEN_SCOPE = "am_application_scope default";

    /**
     * Retrieve Application Definition as JSON.
     *
     * @param pathToArchive Path Application archive
     * @throws IOException            If an error occurs while reading the file
     * @throws APIManagementException If an error occurs while fetching the application definition
     */
    public static String getApplicationDefinitionAsJson(String pathToArchive) throws IOException,
            APIManagementException {
        String jsonContent = null;
        String pathToYamlFile = pathToArchive + ImportExportConstants.YAML_APPLICATION_FILE_LOCATION;
        String pathToJsonFile = pathToArchive + ImportExportConstants.JSON_APPLICATION_FILE_LOCATION;

        // Load yaml representation first if it is present
        if (CommonUtil.checkFileExistence(pathToYamlFile)) {
            if (log.isDebugEnabled()) {
                log.debug("Found application definition file " + pathToYamlFile);
            }
            String yamlContent = FileUtils.readFileToString(new File(pathToYamlFile));
            jsonContent = CommonUtil.yamlToJson(yamlContent);
        } else if (CommonUtil.checkFileExistence(pathToJsonFile)) {
            // load as a json fallback
            if (log.isDebugEnabled()) {
                log.debug("Found application definition file " + pathToJsonFile);
            }
            jsonContent = FileUtils.readFileToString(new File(pathToJsonFile));
        } else {
            throw new APIManagementException(
                    "Cannot find Application definition. application.yaml or application.json should present",
                    ExceptionCodes.ERROR_FETCHING_DEFINITION_FILE);
        }
        return jsonContent;
    }

    /**
     * Check whether a provided userId corresponds to a valid consumer of the store and subscribe if valid
     *
     * @param userId      Username of the Owner
     * @param groupId     The groupId to which the target subscriber belongs to
     * @param apiConsumer API Consumer
     * @throws APIManagementException if an error occurs while checking the validity of user
     */
    public static void validateOwner(String userId, String groupId, APIConsumer apiConsumer)
            throws APIManagementException {
        Subscriber subscriber = apiConsumer.getSubscriber(userId);
        try {
            if (subscriber == null && !APIUtil.isPermissionCheckDisabled()) {
                apiConsumer.addSubscriber(userId, groupId);
            }
        } catch (APIManagementException e) {
            throw new APIManagementException("Provided Application Owner is Invalid", e);
        }
    }

    /**
     * This extracts information for creating an APIKey from an OAuthApplication
     *
     * @param applicationKeyDto Application Key DTO
     * @return An APIKey containing keys from OAuthApplication
     */
    public static APIKey getAPIKeyFromApplicationKeyDTO(ApplicationKeyDTO applicationKeyDto) {
        APIKey apiKey = new APIKey();
        apiKey.setType(String.valueOf(applicationKeyDto.getKeyType()));
        apiKey.setConsumerKey(applicationKeyDto.getConsumerKey());
        apiKey.setConsumerSecret(new String(Base64.decodeBase64(applicationKeyDto.getConsumerSecret())));
        apiKey.setKeyManager(applicationKeyDto.getKeyManager());
        apiKey.setGrantTypes(StringUtils.join(applicationKeyDto.getSupportedGrantTypes(), ", "));

        if (apiKey.getGrantTypes() != null && (apiKey.getGrantTypes().contains(GRANT_TYPE_IMPLICIT)
                || apiKey.getGrantTypes().contains(GRANT_TYPE_CODE))) {
            apiKey.setCallbackUrl(applicationKeyDto.getCallbackUrl());
        }
        apiKey.setValidityPeriod(applicationKeyDto.getToken().getValidityTime());
        apiKey.setTokenScope(DEFAULT_TOKEN_SCOPE);
        return apiKey;
    }

    /**
     * Import and add subscriptions of a particular application for the available APIs and API products
     *
     * @param subscribedAPIs Subscribed APIs
     * @param userId         Username of the subscriber
     * @param application    Application
     * @param update         Whether to update the application or not
     * @param apiConsumer    API Consumer
     * @param organization   Organization
     * @return a list of APIIdentifiers of the skipped subscriptions
     * @throws APIManagementException if an error occurs while importing and adding subscriptions
     * @throws UserStoreException     if an error occurs while checking whether the tenant domain exists
     */
    public static List<APIIdentifier> importSubscriptions(Set<ExportedSubscribedAPI> subscribedAPIs, String userId,
                                                          Application application, Boolean update, APIConsumer apiConsumer, String organization)
            throws APIManagementException,
            UserStoreException {
        List<APIIdentifier> skippedAPIList = new ArrayList<>();
        // removing existing subscribed apis
        if (update) {
            Subscriber subscriber = apiConsumer.getSubscriber(userId);
            Set<SubscribedAPI> currentSubscribedAPIs = apiConsumer.getSubscribedAPIs(subscriber, application.getName(),
                    application.getGroupId());
            for (SubscribedAPI subscribedAPI : currentSubscribedAPIs) {
                apiConsumer.removeSubscription(subscribedAPI.getAPIIdentifier(), userId, application.getId(),
                        application.getGroupId(), application.getOrganization());
            }
        }
        for (ExportedSubscribedAPI subscribedAPI : subscribedAPIs) {
            APIIdentifier apiIdentifier = subscribedAPI.getApiId();
            String tenantDomain = MultitenantUtils
                    .getTenantDomain(APIUtil.replaceEmailDomainBack(apiIdentifier.getProviderName()));
            if (!StringUtils.isEmpty(tenantDomain) && APIUtil.isTenantAvailable(tenantDomain)) {
                String uuidFromIdentifier = ApiMgtDAO.getInstance().getUUIDFromIdentifier(apiIdentifier, tenantDomain);
                if (StringUtils.isNotEmpty(uuidFromIdentifier)) {
                    ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(uuidFromIdentifier, organization);
                    // Tier of the imported subscription
                    String targetTier = subscribedAPI.getThrottlingPolicy();
                    // Checking whether the target tier is available
                    if (isTierAvailable(targetTier, apiTypeWrapper) && apiTypeWrapper.getStatus() != null
                            && APIConstants.PUBLISHED.equals(apiTypeWrapper.getStatus())) {
                        apiTypeWrapper.setTier(targetTier);
                        // Add subscription if update flag is not specified
                        // It will throw an error if subscriber already exists
                        if (update == null || !update) {
                            apiConsumer.addSubscription(apiTypeWrapper, userId, application);
                        } else if (!apiConsumer.isSubscribedToApp(subscribedAPI.getApiId(), userId
                                , application.getId())) {
                            // on update skip subscriptions that already exists
                            apiConsumer.addSubscription(apiTypeWrapper, userId, application);
                        }
                    } else {
                        log.error("Failed to import Subscription as API/API Product "
                                + apiIdentifier.getName() + "-" + apiIdentifier.getVersion() + " as one or more tiers may "
                                + "be unavailable or the API/API Product may not have been published ");
                        skippedAPIList.add(subscribedAPI.getApiId());
                    }
                } else {
                    log.error("Failed to import Subscription as API " + apiIdentifier.getName() + "-" +
                            apiIdentifier.getVersion() + " is not available");
                    skippedAPIList.add(subscribedAPI.getApiId());
                }
            } else {
                log.error("Failed to import Subscription as Tenant domain: " + tenantDomain + " is not available");
                skippedAPIList.add(subscribedAPI.getApiId());
            }
        }
        return skippedAPIList;
    }

    /**
     * Check whether the object is a type of ApiProduct
     *
     * @param object - {@link Object}
     * @return true, if the object is an ApiProduct, otherwise false
     */
    private static boolean isApiProduct(Object object) {
        //Check whether the object is an instance of ApiProduct
        return (object) instanceof APIProduct;
    }

    /**
     * Check whether a target Tier is available to subscribe
     *
     * @param targetTierName Target Tier Name
     * @param apiTypeWrapper - {@link ApiTypeWrapper}
     * @return true, if the target tier is available
     */
    private static boolean isTierAvailable(String targetTierName, ApiTypeWrapper apiTypeWrapper) {
        Set<Tier> availableTiers;
        API api = null;
        APIProduct apiProduct = null;
        if (!apiTypeWrapper.isAPIProduct()) {
            api = apiTypeWrapper.getApi();
            availableTiers = api.getAvailableTiers();
        } else {
            apiProduct = apiTypeWrapper.getApiProduct();
            availableTiers = apiProduct.getAvailableTiers();
        }
        for (Tier tier : availableTiers) {
            if (StringUtils.equals(tier.getName(), targetTierName)) {
                return true;
            }
        }
        if (!apiTypeWrapper.isAPIProduct()) {
            log.error("Tier:" + targetTierName + " is not available for API " + api.getId().getApiName() + "-" + api
                    .getId().getVersion());
        } else {
            log.error(
                    "Tier:" + targetTierName + " is not available for API Product " + apiProduct.getId().getName() + "-"
                            + apiProduct.getId().getVersion());
        }
        return false;
    }

    /**
     * Adds a key to a given Application
     *
     * @param username          User for import application
     * @param application       Application used to add key
     * @param applicationKeyDTO Application Key DTO
     * @param apiConsumer       API Consumer
     * @param update            Whether to update the OAuth Client or not
     * @throws APIManagementException
     */
    public static void addApplicationKey(String username, Application application, ApplicationKeyDTO applicationKeyDTO,
            APIConsumer apiConsumer, Boolean update) throws APIManagementException {
        String[] accessAllowDomainsArray = { "ALL" };
        JSONObject jsonParamObj = new JSONObject();
        jsonParamObj.put(ApplicationConstants.OAUTH_CLIENT_USERNAME, username);
        String grantTypes = StringUtils.join(applicationKeyDTO.getSupportedGrantTypes(), ',');
        if (!StringUtils.isEmpty(grantTypes)) {
            jsonParamObj.put(APIConstants.JSON_GRANT_TYPES, grantTypes);
        }
        /* Read clientId & clientSecret from ApplicationKeyGenerateRequestDTO object.
           User can provide clientId only or both clientId and clientSecret
           User cannot provide clientSecret only
         */
        if (!StringUtils.isEmpty(applicationKeyDTO.getConsumerKey())) {
            jsonParamObj.put(APIConstants.JSON_CLIENT_ID, applicationKeyDTO.getConsumerKey());
            if (!StringUtils.isEmpty(applicationKeyDTO.getConsumerSecret())) {
                byte[] bytes = Base64.decodeBase64(applicationKeyDTO.getConsumerSecret());
                String consumerSecret = new String(bytes, StandardCharsets.UTF_8);
                jsonParamObj.put(APIConstants.JSON_CLIENT_SECRET, consumerSecret);
            }
        }
        if (!StringUtils.isEmpty(applicationKeyDTO.getCallbackUrl())) {
            jsonParamObj.put(APIConstants.JSON_CALLBACK_URL, applicationKeyDTO.getCallbackUrl());
        }
        if (applicationKeyDTO.getAdditionalProperties() != null) {
            String additionalProperties = new Gson().toJson(applicationKeyDTO.getAdditionalProperties());
            org.json.JSONObject jsonObject = new org.json.JSONObject(additionalProperties);
            Set<String> keysSet = jsonObject.keySet();
            for (String key : keysSet) {
                if (jsonObject.get(key) instanceof Double) {
                    jsonObject.put(key, String.valueOf(((Double) jsonObject.get(key)).intValue()));
                } else {
                    jsonObject.put(key, jsonObject.get(key).toString());
                }
            }
            jsonParamObj.put(APIConstants.JSON_ADDITIONAL_PROPERTIES, jsonObject.toString());
        }
        String jsonParams = jsonParamObj.toString();
        String tokenScopes = StringUtils.join(applicationKeyDTO.getToken().getTokenScopes(), ',');

        if (!update) {
            apiConsumer.requestApprovalForApplicationRegistration(username, application,
                    applicationKeyDTO.getKeyType().toString(), applicationKeyDTO.getCallbackUrl(),
                    accessAllowDomainsArray, Long.toString(applicationKeyDTO.getToken().getValidityTime()), tokenScopes,
                    jsonParams, applicationKeyDTO.getKeyManager(), null, true);
        } else {
            apiConsumer.updateAuthClient(username, application, applicationKeyDTO.getKeyType().toString(),
                    applicationKeyDTO.getCallbackUrl(), null, null, null, application.getGroupId(), jsonParams,
                    applicationKeyDTO.getKeyManager());
        }
    }
}
