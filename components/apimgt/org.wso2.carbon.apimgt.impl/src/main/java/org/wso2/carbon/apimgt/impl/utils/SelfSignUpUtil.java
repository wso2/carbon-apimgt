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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.PasswordResolver;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.PasswordResolverFactory;
import org.wso2.carbon.apimgt.impl.dto.UserRegistrationConfigDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class contains the utility methods used for self signup
 */
public final class SelfSignUpUtil {

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

		return getSignupConfigurationFromRegistry(tenantDomain);
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

		UserRegistrationConfigDTO config;

		try {
			String selfSighupConfig =
					ServiceReferenceHolder.getInstance().getApimConfigService().getSelfSighupConfig(tenantDomain);
			OMElement element = AXIOMUtil.stringToOM(selfSighupConfig);
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
			OMElement rolesElement =
					element.getFirstChildWithName(new QName(APIConstants.SELF_SIGN_UP_REG_ROLES_ELEM));
			Iterator roleListIterator = rolesElement.getChildrenWithLocalName(APIConstants.SELF_SIGN_UP_REG_ROLE_ELEM);
			while (roleListIterator.hasNext()) {
				OMElement roleElement = (OMElement) roleListIterator.next();
				String tmpRole = roleElement.getFirstChildWithName(
						new QName(APIConstants.SELF_SIGN_UP_REG_ROLE_NAME_ELEMENT)).getText();
				boolean tmpIsExternal = Boolean.parseBoolean(roleElement.getFirstChildWithName(
						new QName(APIConstants.SELF_SIGN_UP_REG_ROLE_IS_EXTERNAL)).getText());
				config.getRoles().put(tmpRole, tmpIsExternal);
			}
		} catch (XMLStreamException e) {
			throw new APIManagementException("Error while parsing configuration ", e);
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

}
