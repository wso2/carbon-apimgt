/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.URL;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.PasswordResolver;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.PasswordResolverFactory;
import org.wso2.carbon.apimgt.impl.dto.UserRegistrationConfigDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;

/**
 * This class contains the utility methods used for self signup
 */
public final class SelfSignUpUtil {

	private static final Log log = LogFactory.getLog(SelfSignUpUtil.class);

	private static final String CONSENT_API_RELATIVE_PATH = "api/identity/consent-mgt/v1.0";
	private static final String PURPOSE_ID = "purposeId";
	private static final String PURPOSES_ENDPOINT_RELATIVE_PATH = "/consents/purposes";
	private static final String PURPOSES = "purposes";
	private static final String PURPOSE = "purpose";
	private static final String PII_CATEGORIES = "piiCategories";
	private static final String DEFAULT = "DEFAULT";

	/**
	 * retrieve self signup configuration from the cache. if cache mises, load
	 * to the cache from
	 * the registry and return configuration
	 * 
	 * @param tenantDomain
	 *            Domain name of the tenant
	 * @return UserRegistrationConfigDTO self signup configuration for the
	 *         tenant
	 * @throws APIManagementException
	 */
	public static UserRegistrationConfigDTO getSignupConfiguration(String tenantDomain)
			throws APIManagementException {
		UserRegistrationConfigDTO config = null;
		String currentFlowDomain =
				PrivilegedCarbonContext.getThreadLocalCarbonContext()
				.getTenantDomain();
		boolean isTenantFlowStarted = false;
		try {

			/* start the correct tenant flow to load the tenant's registry*/
			if (tenantDomain != null &&
					!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
				if (!currentFlowDomain.equals(tenantDomain)) {
					/* if the current flow is not the one related to the domain */
					isTenantFlowStarted = true;
					PrivilegedCarbonContext.startTenantFlow();
					PrivilegedCarbonContext.getThreadLocalCarbonContext()
					.setTenantDomain(tenantDomain, true);
				}
			}
			config = getSignupConfigurationFromRegistry(tenantDomain);
		} finally {
			if (isTenantFlowStarted) {
				PrivilegedCarbonContext.endTenantFlow();
			}
		}	

		return config;
	}

	/**
	 * load configuration from the registry
	 * 
	 * @param tenantDomain - The Tenant Domain
	 * @return - A UserRegistrationConfigDTO instance
	 * @throws APIManagementException
	 */
    private static UserRegistrationConfigDTO getSignupConfigurationFromRegistry(String tenantDomain)
                                                                                      throws APIManagementException {

        UserRegistrationConfigDTO config = null;

        try {

            int tenantId =
                           ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                                                 .getTenantId(tenantDomain);
            APIUtil.loadTenantRegistry(tenantId);
            Registry registry =
                                (Registry) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                                                  .getRegistry(RegistryType.SYSTEM_GOVERNANCE);
            if (registry.resourceExists(APIConstants.SELF_SIGN_UP_CONFIG_LOCATION)) {
                Resource resource = registry.get(APIConstants.SELF_SIGN_UP_CONFIG_LOCATION);
                String content = new String((byte[]) resource.getContent(), Charset.defaultCharset());
                OMElement element = AXIOMUtil.stringToOM(content);
                config = new UserRegistrationConfigDTO();
                
                
                config.setSignUpDomain(element.getFirstChildWithName(
                                                      new QName(APIConstants.SELF_SIGN_UP_REG_DOMAIN_ELEM)).getText());
                config.setAdminUserName(APIUtil.replaceSystemProperty(
                                                      element.getFirstChildWithName(new QName(
                                                                APIConstants.SELF_SIGN_UP_REG_USERNAME)).getText()));

                String encryptedPassword = element
                        .getFirstChildWithName(new QName(APIConstants.SELF_SIGN_UP_REG_PASSWORD)).getText();
                PasswordResolver passwordResolver = PasswordResolverFactory.getInstance();
                String resovledPassword = passwordResolver.getPassword(encryptedPassword);
                config.setAdminPassword(APIUtil.replaceSystemProperty(resovledPassword));
                config.setSignUpEnabled(Boolean.parseBoolean(element.getFirstChildWithName(
                                                      new QName(APIConstants.SELF_SIGN_UP_REG_ENABLED)).getText()));
                
                OMElement rolesElement = element.getFirstChildWithName(new QName(APIConstants.SELF_SIGN_UP_REG_ROLES_ELEM));
                
                Iterator roleListIterator = rolesElement.getChildrenWithLocalName(APIConstants.SELF_SIGN_UP_REG_ROLE_ELEM);
                
                while (roleListIterator.hasNext()) {
                    OMElement roleElement = (OMElement) roleListIterator.next();
                    String tmpRole = roleElement.getFirstChildWithName(
                                                 new QName(APIConstants.SELF_SIGN_UP_REG_ROLE_NAME_ELEMENT)).getText();
                    boolean tmpIsExternal = Boolean.parseBoolean(roleElement.getFirstChildWithName(
                                                 new QName(APIConstants.SELF_SIGN_UP_REG_ROLE_IS_EXTERNAL)).getText());
                    config.getRoles().put(tmpRole, tmpIsExternal);
                }
			}
		} catch (RegistryException e) {
			throw new APIManagementException("Error while reading registry " +
					APIConstants.SELF_SIGN_UP_CONFIG_LOCATION, e);
		} catch (XMLStreamException e) {
		    throw new APIManagementException("Error while parsing configuration " +
                    APIConstants.SELF_SIGN_UP_CONFIG_LOCATION, e);
        } catch (UserStoreException e) {
            throw new APIManagementException("Error in retrieving Tenant Information while reading SignUp "
                                             + "configuration", e);
        }
		return config;
	}

	/**
	 * Check whether user can signup to the tenant domain
	 * 
	 * @param userName - The user name
	 * @param realm - The realm
	 * @return - A boolean value
	 * @throws APIManagementException
	 */
	public static boolean isUserNameWithAllowedDomainName(String userName, UserRealm realm)
			throws APIManagementException {
		int index;
		index = userName.indexOf('/');

		// Check whether we have a secondary UserStoreManager setup.
		if (index > 0) {
			// Using the short-circuit. User name comes with the domain name.
			try {
				return !realm.getRealmConfiguration()
						.isRestrictedDomainForSlefSignUp(userName.substring(0, index));
			} catch (UserStoreException e) {
				throw new APIManagementException(e.getMessage(), e);				
			}
		}

		return true;
	}

	/**
	 * get the full role name list (ex: internal/subscriber)
	 * 
	 * @param config - A UserRegistrationConfigDTO instance
	 * @return - A list object containing role names
	 */
	public static List<String> getRoleNames(UserRegistrationConfigDTO config) {

		ArrayList<String> roleNamesArr = new ArrayList<String>();
		Map<String, Boolean> roles = config.getRoles();
		for (Map.Entry<String, Boolean> entry : roles.entrySet()) {
			String roleName;
			if (entry.getValue()) {
				// external role
				roleName =
						config.getSignUpDomain().toUpperCase() +
						UserCoreConstants.DOMAIN_SEPARATOR + entry.getKey();
			} else {
				// internal role
				roleName =
						UserCoreConstants.INTERNAL_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR +
						entry.getKey();
			}
			roleNamesArr.add(roleName);
		}
		return roleNamesArr;

	}

	/**
	 * modify user name with user storeage information. 
	 * @param username - The user name
	 * @param signupConfig - The sign up configuration
	 * @return - The modified user name
	 */
	public static String getDomainSpecificUserName(String username, UserRegistrationConfigDTO signupConfig) {
		String modifiedUsername = null;	
		// set tenant specific sign up user storage
		if (signupConfig != null && !signupConfig.getSignUpDomain().equals("")) {
			
			int index = username.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
			/*
			 * if there is a different domain provided by the user other than one 
			 * given in the configuration, add the correct signup domain. Here signup
			 * domain refers to the user storage
			 */
		
			if (index > 0) {
				modifiedUsername =
						signupConfig.getSignUpDomain().toUpperCase() +
						UserCoreConstants.DOMAIN_SEPARATOR +
						username.substring(index + 1);
			} else {
				modifiedUsername =
						signupConfig.getSignUpDomain().toUpperCase() +
						UserCoreConstants.DOMAIN_SEPARATOR + username;
			}
		}
		
		return modifiedUsername;
	}

    /**
     * This method is used to get the consent purposes
     *
     * @param tenantDomain tenant domain
     * @return A json string containing consent purposes
     * @throws APIManagementException APIManagement Exception
     * @throws IOException            IO Exception
     * @throws ParseException         Parse Exception
     */
    public static String getConsentPurposes(String tenantDomain)
            throws APIManagementException, IOException, ParseException {
        String tenant = tenantDomain;
        String purposesEndpoint;
        String purposesJsonString = "";
        if (tenant == null) {
            tenant = APIConstants.SUPER_TENANT_DOMAIN;
        }
        purposesEndpoint = getPurposesEndpoint(tenant);
        String purposesResponse = executeGet(purposesEndpoint, tenantDomain);
        JSONParser parser = new JSONParser();
        JSONArray purposes = (JSONArray) parser.parse(purposesResponse);
        JSONArray purposesResponseArray = new JSONArray();
        for (int purposeIndex = 0; purposeIndex < purposes.size(); purposeIndex++) {
            JSONObject purpose = (JSONObject) purposes.get(purposeIndex);
            if (!isDefaultPurpose(purpose)) {
                purpose = retrievePurpose(((Long) purpose.get(PURPOSE_ID)).intValue(), tenant);
                if (hasPIICategories(purpose)) {
                    purposesResponseArray.add(purpose);
                }
            }
        }
        if (!purposesResponseArray.isEmpty()) {
            JSONObject purposesJson = new JSONObject();
            purposesJson.put(PURPOSES, purposesResponseArray);
            purposesJsonString = purposesJson.toString();
        }
        return purposesJsonString;
    }

    /**
     * This method is used to construct the endpoint URL to call the consent management service
     *
     * @param tenantDomain The tenant domain
     * @return endpoint url
     */
    private static String getPurposesEndpoint(String tenantDomain) {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        String serviceUrl = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
        String purposesEndpoint;
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
            purposesEndpoint = serviceUrl.replace(APIConstants.SERVICES_URL_RELATIVE_PATH,
                    "t/" + tenantDomain + "/" + CONSENT_API_RELATIVE_PATH + PURPOSES_ENDPOINT_RELATIVE_PATH);
        } else {
            purposesEndpoint = serviceUrl.replace(APIConstants.SERVICES_URL_RELATIVE_PATH,
                    CONSENT_API_RELATIVE_PATH + PURPOSES_ENDPOINT_RELATIVE_PATH);
        }
        return purposesEndpoint;
    }

    /**
     * This method is used to execute a get request to the consent management service
     *
     * @param url          The endpoint url of the consent management service
     * @param tenantDomain The tenant domain
     * @return The response string
     * @throws APIManagementException APIManagement Exception
     * @throws IOException            IO Exception
     */
    private static String executeGet(String url, String tenantDomain) throws APIManagementException, IOException {

        boolean isDebugEnabled = log.isDebugEnabled();
	    URL consentURL = new URL(url);
        try (CloseableHttpClient httpclient = (CloseableHttpClient) APIUtil
		        .getHttpClient(consentURL.getPort(), consentURL.getProtocol())) {

            HttpGet httpGet = new HttpGet(url);
            setAuthorizationHeader(httpGet, tenantDomain);

            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {

                if (isDebugEnabled) {
                    log.debug("HTTP status " + response.getStatusLine().getStatusCode());
                }
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent()));
                    String inputLine;
                    StringBuilder responseString = new StringBuilder();

                    while ((inputLine = reader.readLine()) != null) {
                        responseString.append(inputLine);
                    }
                    return responseString.toString();
                } else {
                    throw new APIManagementException(
                            "Error while retrieving data from " + url + ". Found http status " + response
                                    .getStatusLine());
                }
            } finally {
                httpGet.releaseConnection();
            }
        }
    }

    /**
     * This method is used to set the Authorization header for the request sent to consent management service
     *
     * @param httpMethod   The method which requires to add the Authorization header
     * @param tenantDomain The tenant domain
     * @throws APIManagementException APIManagement Exception
     */
    private static void setAuthorizationHeader(HttpRequestBase httpMethod, String tenantDomain)
            throws APIManagementException {
        UserRegistrationConfigDTO signupConfig = SelfSignUpUtil.getSignupConfiguration(tenantDomain);
        String adminUsername = signupConfig.getAdminUserName();
        String adminPassword = signupConfig.getAdminPassword();
        String toEncode = adminUsername + ":" + adminPassword;
        byte[] encoding = Base64.encodeBase64(toEncode.getBytes());
        String authHeader = new String(encoding, Charset.defaultCharset());
        httpMethod.addHeader(HTTPConstants.HEADER_AUTHORIZATION,
                APIConstants.AUTHORIZATION_HEADER_BASIC + " " + authHeader);
    }

    /**
     * This method is used to retrieve the set of attributes for a given consent purpose
     *
     * @param purposeId    Id of the purpose
     * @param tenantDomain The tenant domain
     * @return A JSONObject for the given consent purpose
     * @throws APIManagementException APIManagement Exception
     * @throws IOException            IO Exception
     * @throws ParseException         Parse Exception
     */
    private static JSONObject retrievePurpose(int purposeId, String tenantDomain)
            throws APIManagementException, IOException, ParseException {
        String purposeResponse = executeGet(getPurposesEndpoint(tenantDomain) + purposeId, tenantDomain);
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(purposeResponse);
    }

    /**
     * This method is used to check whether a given consent purpose is the default purpose
     *
     * @param purpose The consent purpose
     * @return Boolean whether it is the default purpose
     */
    private static boolean isDefaultPurpose(JSONObject purpose) {
        return DEFAULT.equalsIgnoreCase((String) purpose.get(PURPOSE));
    }

    /**
     * This method is used to check for PII Categories for a particular consent management purpose
     *
     * @param purpose The consent purpose
     * @return Boolean if there are PII Categories for the purpose
     */
    private static boolean hasPIICategories(JSONObject purpose) {
        JSONArray piiCategories = (JSONArray) purpose.get(PII_CATEGORIES);
        return !piiCategories.isEmpty();
    }
}
