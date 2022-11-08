/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.impl.utils;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import javax.cache.Caching;
import java.util.*;

public class SystemScopeUtils {

    private static final Log log = LogFactory.getLog(SystemScopeUtils.class);

    private static final String AUTHENTICATOR_NAME = APIConstants.SystemScopeConstants.SAML2_SSO_AUTHENTICATOR_NAME;

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
                                log.debug("Adding attributes for Assertion: " + assertion + " AttributeName : "
                                        + attributeName + ", AttributeValue : " + Arrays
                                        .toString(attributeValuesArray));
                            }
                            roles.addAll(Arrays.asList(attributeValuesArray));
                        } else if (attributeValues != null && attributeValues.size() > 1) {
                            for (XMLObject attributeValue : attributeValues) {
                                String attributeValueString = getAttributeValue(attributeValue);
                                if (log.isDebugEnabled()) {
                                    log.debug("Adding attributes for Assertion: " + assertion + " AttributeName : "
                                            + attributeName + ", AttributeValue : " + attributeValue);
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
        if (attributeValue == null) {
            return null;
        } else if (attributeValue instanceof XSString) {
            return getStringAttributeValue((XSString) attributeValue);
        } else if (attributeValue instanceof XSAnyImpl) {
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
            if (configParameters.containsKey(APIConstants.SystemScopeConstants.ATTRIBUTE_VALUE_SEPARATOR)) {
                return configParameters.get(APIConstants.SystemScopeConstants.ATTRIBUTE_VALUE_SEPARATOR);
            }
        }

        return APIConstants.SystemScopeConstants.ATTRIBUTE_VALUE_SEPERATER;
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
            if (configParameters.containsKey(APIConstants.SystemScopeConstants.ROLE_CLAIM_ATTRIBUTE)) {
                return configParameters.get(APIConstants.SystemScopeConstants.ROLE_CLAIM_ATTRIBUTE);
            }
        }

        return APIConstants.SystemScopeConstants.ROLE_ATTRIBUTE_NAME;
    }

    /**
     * This method gets the RESTAPIScopes configuration from REST_API_SCOPE_CACHE if available, if not from
     * tenant-conf.json in registry.
     *
     * @param tenantDomain tenant domain name
     * @return Map of scopes which contains scope names and associated role list
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getRESTAPIScopesForTenant(String tenantDomain) throws APIManagementException {

        Map<String, String> restAPIScopes;
        restAPIScopes = (Map) CacheProvider.getRESTAPIScopeCache().get(tenantDomain);
        if (restAPIScopes == null) {

            restAPIScopes = getRESTAPIScopesFromConfig(getTenantRESTAPIScopesConfig(tenantDomain),
                    getTenantRESTAPIScopeRoleMappingsConfig(tenantDomain));
            //call load tenant config for rest API.
            //then put cache
            Caching.getCacheManager(APIConstants.SystemScopeConstants.EXTENTIONS_CACHE_MANAGER)
                    .getCache(APIConstants.SystemScopeConstants.REST_API_SCOPE_CACHE).put(tenantDomain, restAPIScopes);

        }
        return restAPIScopes;
    }

    /**
     * @param scopesConfig JSON configuration object with scopes and associated roles
     * @param roleMappings JSON Configuration object with role mappings
     * @return Map of scopes which contains scope names and associated role list
     */
    public static Map<String, String> getRESTAPIScopesFromConfig(JSONObject scopesConfig, JSONObject roleMappings) {

        Map<String, String> scopes = new HashMap<String, String>();
        if (scopesConfig != null) {
            JSONArray scopesArray = (JSONArray) scopesConfig.get("Scope");
            for (Object scopeObj : scopesArray) {
                JSONObject scope = (JSONObject) scopeObj;
                String scopeName = scope.get(APIConstants.SystemScopeConstants.REST_API_SCOPE_NAME).toString();
                String scopeRoles = scope.get(APIConstants.SystemScopeConstants.REST_API_SCOPE_ROLE).toString();
                if (roleMappings != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("REST API scope role mappings exist. Hence proceeding to swap original scope roles "
                                + "for mapped scope roles.");
                    }
                    //split role list string read using comma separator
                    List<String> originalRoles = Arrays.asList(scopeRoles.split("\\s*,\\s*"));
                    List<String> mappedRoles = new ArrayList<String>();
                    for (String role : originalRoles) {
                        String mappedRole = (String) roleMappings.get(role);
                        if (mappedRole != null) {
                            if (log.isDebugEnabled()) {
                                log.debug(role + " was mapped to " + mappedRole);
                            }
                            mappedRoles.add(mappedRole);
                        } else {
                            mappedRoles.add(role);
                        }
                    }
                    scopeRoles = String.join(",", mappedRoles);
                }
                scopes.put(scopeName, scopeRoles);
            }
        }

        return scopes;
    }

    /**
     * @param tenantDomain Tenant domain to be used to get configurations for REST API scopes
     * @return JSON object which contains configuration for REST API scopes
     */
    public static JSONObject getTenantRESTAPIScopesConfig(String tenantDomain) throws APIManagementException {

        JSONObject restAPIConfigJSON = null;
        JSONObject tenantConfJson = APIUtil.getTenantConfig(tenantDomain);
        if (tenantConfJson != null) {
            restAPIConfigJSON = getRESTAPIScopesFromTenantConfig(tenantConfJson);
            if (restAPIConfigJSON == null) {
                throw new APIManagementException("RESTAPIScopes config does not exist for tenant "
                        + tenantDomain);
            }
        }
        return restAPIConfigJSON;
    }

    /**
     * @param tenantDomain Tenant domain to be used to get configurations for REST API scopes
     * @return JSON object which contains configuration for REST API scopes
     * @throws APIManagementException
     */
    public static JSONObject getTenantRESTAPIScopeRoleMappingsConfig(String tenantDomain) throws APIManagementException {

        JSONObject restAPIConfigJSON = null;
        JSONObject tenantConfJson = APIUtil.getTenantConfig(tenantDomain);
        if (tenantConfJson != null) {
            restAPIConfigJSON = getRESTAPIScopeRoleMappingsFromTenantConfig(tenantConfJson);
            if (restAPIConfigJSON == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No REST API role mappings are defined for the tenant " + tenantDomain);
                }
            }
        }
        return restAPIConfigJSON;
    }

    /**
     * load tenant axis configurations.
     *
     * @param tenantDomain
     */
    public static void loadTenantConfigBlockingMode(String tenantDomain) {

        try {
            ConfigurationContext ctx = ServiceReferenceHolder.getContextService().getServerConfigContext();
            TenantAxisUtils.getTenantAxisConfiguration(tenantDomain, ctx);
        } catch (Exception e) {
            log.error("Error while creating axis configuration for tenant " + tenantDomain, e);
        }
    }

    /**
     * Helper method to get tenantId from tenantDomain
     *
     * @param tenantDomain tenant Domain
     * @return tenantId
     */
    public static int getTenantIdFromTenantDomain(String tenantDomain) {

        RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();

        if (realmService == null || tenantDomain == null) {
            return MultitenantConstants.SUPER_TENANT_ID;
        }

        try {
            return realmService.getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            String msg = "Failed to get the Tenant Id of the the Tenant Domain : " + tenantDomain;
            log.error(msg, e);
        }
        return -1;
    }

    private static JSONObject getRESTAPIScopesFromTenantConfig(JSONObject tenantConf) {

        return (JSONObject) tenantConf.get(APIConstants.SystemScopeConstants.REST_API_SCOPES_CONFIG);
    }

    private static JSONObject getRESTAPIScopeRoleMappingsFromTenantConfig(JSONObject tenantConf) {

        return (JSONObject) tenantConf.get(APIConstants.SystemScopeConstants.REST_API_ROLE_MAPPINGS_CONFIG);
    }
}
