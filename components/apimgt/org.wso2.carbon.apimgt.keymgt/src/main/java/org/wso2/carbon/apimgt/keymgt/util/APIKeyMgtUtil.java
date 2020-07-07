/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.apimgt.keymgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.handlers.ResourceConstants;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.Caching;

public class APIKeyMgtUtil {

    private static final Log log = LogFactory.getLog(APIKeyMgtUtil.class);

    private  static boolean isKeyCacheInistialized = false;

    private static final String AUTHENTICATOR_NAME = ResourceConstants.SAML2_SSO_AUTHENTICATOR_NAME;

    public static Map<String,String> constructParameterMap(OAuth2TokenValidationRequestDTO.TokenValidationContextParam[] params){
        Map<String,String> paramMap = null;
        if(params != null){
            paramMap = new HashMap<String, String>();
            for(OAuth2TokenValidationRequestDTO.TokenValidationContextParam param : params){
                paramMap.put(param.getKey(),param.getValue());
            }
        }

        return paramMap;
    }
    /**
     * Get a database connection instance from the Identity Persistence Manager
     * @return Database Connection
     */
    public static Connection getDBConnection(){
        return IdentityDatabaseUtil.getDBConnection();
    }

    /**
     * Get the KeyValidationInfo object from cache, for a given cache-Key
     *
     * @param cacheKey Key for the Cache Entry
     * @return APIKeyValidationInfoDTO
     * @throws APIKeyMgtException
     */
    public static APIKeyValidationInfoDTO getFromKeyManagerCache(String cacheKey) {

        APIKeyValidationInfoDTO info = null;

        boolean cacheEnabledKeyMgt = APIKeyMgtDataHolder.getKeyCacheEnabledKeyMgt();

        Cache cache = getKeyManagerCache();

        //We only fetch from cache if KeyMgtValidationInfoCache is enabled.
        if (cacheEnabledKeyMgt) {
            info = (APIKeyValidationInfoDTO) cache.get(cacheKey);
            //If key validation information is not null then only we proceed with cached object
            if (info != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found cached access token for : " + cacheKey + ".");
                }
            }
        }

        return info;
    }


    /**
     * Store KeyValidationInfoDTO in Key Manager Cache
     *
     * @param cacheKey          Key for the Cache Entry to be stored
     * @param validationInfoDTO KeyValidationInfoDTO object
     */
    public static void writeToKeyManagerCache(String cacheKey, APIKeyValidationInfoDTO validationInfoDTO) {

        boolean cacheEnabledKeyMgt = APIKeyMgtDataHolder.getKeyCacheEnabledKeyMgt();

        if (cacheKey != null) {
            if (log.isDebugEnabled()) {
                log.debug("Storing KeyValidationDTO for key: " + cacheKey + ".");
            }
        }

        if (validationInfoDTO != null) {
            if (cacheEnabledKeyMgt) {
                Cache cache = getKeyManagerCache();
                cache.put(cacheKey, validationInfoDTO);
            }
        }
    }

    /**
     * Remove APIKeyValidationInfoDTO from Key Manager Cache
     *
     * @param cacheKey Key for the Cache Entry to be removed
     */
    public static void removeFromKeyManagerCache(String cacheKey) {

        boolean cacheEnabledKeyMgt = APIKeyMgtDataHolder.getKeyCacheEnabledKeyMgt();

        if (cacheKey != null && cacheEnabledKeyMgt) {

            Cache cache = getKeyManagerCache();
            cache.remove(cacheKey);
            log.debug("KeyValidationInfoDTO removed for key : " + cacheKey);
        }
    }

    private static Cache getKeyManagerCache(){
        String apimKeyCacheExpiry = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                getAPIManagerConfiguration().getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY);
        if(!isKeyCacheInistialized && apimKeyCacheExpiry != null ) {
            isKeyCacheInistialized = true;
            return Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
                    createCacheBuilder(APIConstants.KEY_CACHE_NAME)
                    .setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                            Long.parseLong(apimKeyCacheExpiry)))
                    .setExpiry(CacheConfiguration.ExpiryType.ACCESSED, new CacheConfiguration.Duration(TimeUnit.SECONDS,
                            Long.parseLong(apimKeyCacheExpiry))).setStoreByValue(false).build();
        } else{
          return  Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).
                    getCache(APIConstants.KEY_CACHE_NAME);
        }

    }

    /**
     * This returns API object for given APIIdentifier. Reads from registry entry for given APIIdentifier
     * creates API object
     *
     * @param identifier APIIdentifier object for the API
     * @return API object for given identifier
     * @throws APIManagementException on error in getting API artifact
     */
    public static API getAPI(APIIdentifier identifier) throws APIManagementException {
        String apiPath = APIUtil.getAPIPath(identifier);

        try {
            Registry registry = APIKeyMgtDataHolder.getRegistryService().getGovernanceSystemRegistry();
            GenericArtifactManager artifactManager = APIUtil.getArtifactManager(registry,
                    APIConstants.API_KEY);
            if (artifactManager == null) {
                String errorMessage = "Artifact manager is null when retrieving API " + identifier.getApiName();
                log.error(errorMessage);
                throw new APIManagementException(errorMessage);
            }
            Resource apiResource = registry.get(apiPath);
            String artifactId = apiResource.getUUID();
            if (artifactId == null) {
                throw new APIManagementException("artifact id is null for : " + apiPath);
            }
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
            return APIUtil.getAPI(apiArtifact, registry);

        } catch (RegistryException e) {
            return null;
        }
    }

    /**
     * Get the role list from the SAML2 Assertion
     *
     * @param assertion SAML2 assertion
     * @return Role list from the assertion
     */
    public static String[] getRolesFromAssertion(Assertion assertion) {
        List<String> roles = new ArrayList<String>();
        String roleClaim = getRoleClaim();
        List<AttributeStatement> attributeStatementList = assertion.getAttributeStatements();

        if (attributeStatementList != null) {
            for (AttributeStatement statement : attributeStatementList) {
                List<Attribute> attributesList = statement.getAttributes();
                for (Attribute attribute : attributesList) {
                    String attributeName = attribute.getName();
                    if (attributeName != null && roleClaim.equals(attributeName)) {
                        List<XMLObject> attributeValues = attribute.getAttributeValues();
                        if (attributeValues != null && attributeValues.size() == 1) {
                            String attributeValueString = getAttributeValue(attributeValues.get(0));
                            String multiAttributeSeparator = getAttributeSeparator();
                            String[] attributeValuesArray = attributeValueString.split(multiAttributeSeparator);
                            if (log.isDebugEnabled()) {
                                log.debug("Adding attributes for Assertion: " + assertion + " AttributeName : " +
                                        attributeName + ", AttributeValue : " + Arrays.toString(attributeValuesArray));
                            }
                            roles.addAll(Arrays.asList(attributeValuesArray));
                        } else if (attributeValues != null && attributeValues.size() > 1) {
                            for (XMLObject attributeValue : attributeValues) {
                                String attributeValueString = getAttributeValue(attributeValue);
                                if (log.isDebugEnabled()) {
                                    log.debug("Adding attributes for Assertion: " + assertion + " AttributeName : " +
                                            attributeName + ", AttributeValue : " + attributeValue);
                                }
                                roles.add(attributeValueString);
                            }
                        }
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Role list found for assertion: " + assertion + ", roles: " + roles);
        }
        return roles.toArray(new String[roles.size()]);
    }

    private static String getAttributeValue(XMLObject attributeValue) {
        if (attributeValue == null){
            return null;
        } else if (attributeValue instanceof XSString){
            return getStringAttributeValue((XSString) attributeValue);
        } else if(attributeValue instanceof XSAnyImpl){
            return getAnyAttributeValue((XSAnyImpl) attributeValue);
        } else {
            return attributeValue.toString();
        }
    }

    private static String getStringAttributeValue(XSString attributeValue) {
        return attributeValue.getValue();
    }

    private static String getAnyAttributeValue(XSAnyImpl attributeValue) {
        return attributeValue.getTextContent();
    }

    /**
     * Get attribute separator from configuration or from the constants
     *
     * @return
     */
    private static String getAttributeSeparator() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig = authenticatorsConfiguration
                .getAuthenticatorConfig(AUTHENTICATOR_NAME);

        if (authenticatorConfig != null) {
            Map<String, String> configParameters = authenticatorConfig.getParameters();
            if (configParameters.containsKey(ResourceConstants.ATTRIBUTE_VALUE_SEPARATOR)) {
                return configParameters.get(ResourceConstants.ATTRIBUTE_VALUE_SEPARATOR);
            }
        }

        return ResourceConstants.ATTRIBUTE_VALUE_SEPERATER;
    }

    /**
     * Role claim attribute value from configuration file or from constants
     *
     * @return
     */
    private static String getRoleClaim() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig = authenticatorsConfiguration
                .getAuthenticatorConfig(AUTHENTICATOR_NAME);

        if (authenticatorConfig != null) {
            Map<String, String> configParameters = authenticatorConfig.getParameters();
            if (configParameters.containsKey(ResourceConstants.ROLE_CLAIM_ATTRIBUTE)) {
                return configParameters.get(ResourceConstants.ROLE_CLAIM_ATTRIBUTE);
            }
        }

        return ResourceConstants.ROLE_ATTRIBUTE_NAME;
    }

}
