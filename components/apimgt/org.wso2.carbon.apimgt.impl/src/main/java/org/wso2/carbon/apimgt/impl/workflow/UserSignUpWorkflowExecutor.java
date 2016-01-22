/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.ArrayList;
import java.util.List;


public abstract class UserSignUpWorkflowExecutor extends WorkflowExecutor {

	private static final Log log = LogFactory.getLog(UserSignUpWorkflowExecutor.class);

	/**
	 * Method updates Roles users with subscriber role
	 * @param serverURL
	 * @param adminUsername
	 * @param adminPassword
	 * @param userName
	 * @param role
	 * @throws Exception
	 */
	protected static void updateRolesOfUser(String serverURL, String adminUsername,
	                                        String adminPassword, String userName, String role)
	                                                                                           throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Adding Subscriber role to " + userName);
		}

		String url = serverURL + "UserAdmin";
		RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
		UserRealm realm = realmService.getBootstrapRealm();
		UserStoreManager manager = realm.getUserStoreManager();
		if (!manager.isExistingRole(role)){
			log.error("Could not find role " + role + " in the user store");
			throw new Exception("Could not find role " + role + " in the user store");
		}

		UserAdminStub userAdminStub = new UserAdminStub(url);
		CarbonUtils.setBasicAccessSecurityHeaders(adminUsername, adminPassword,
		                                          true, userAdminStub._getServiceClient());
		FlaggedName[] flaggedNames = userAdminStub.getRolesOfUser(userName, "*", -1);
		List<String> roles = new ArrayList<String>();
		if (flaggedNames != null) {
			for (FlaggedName flaggedName : flaggedNames) {
				if (flaggedName.getSelected()) {
					roles.add(flaggedName.getItemName());
				}
			}
		}
		roles.add(role);
		userAdminStub.updateRolesOfUser(userName, roles.toArray(new String[roles.size()]));
	}

	/**
	 * Method updates Roles users with list of roles
	 * @param serverURL
	 * @param adminUsername
	 * @param adminPassword
	 * @param userName
	 * @param tenantID
	 * @param role
	 * @throws Exception
	 */
	protected static void updateRolesOfUser(String serverURL, String adminUsername,
	                                        String adminPassword, String userName,
	                                        List<String> roleList, String tenantDomain)
	                                        		throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Adding roles to " + userName + "in " + tenantDomain + " Domain");
		}
		String url = serverURL + "UserAdmin";
		RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
		int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
				.getTenantId(tenantDomain);
		UserRealm realm = (UserRealm) realmService.getTenantUserRealm(tenantId);
		UserStoreManager manager = realm.getUserStoreManager();
		
		if(manager.isExistingUser(userName)) {
			// check whether given roles exist
			for (String role : roleList) {
				if (!manager.isExistingRole(role)) {
					log.error("Could not find role " + role + " in the user store");
					throw new Exception("Could not find role " + role + " in the user store");
				}
			}

			UserAdminStub userAdminStub = new UserAdminStub(url);
			CarbonUtils.setBasicAccessSecurityHeaders(adminUsername, adminPassword, true,
			                                          userAdminStub._getServiceClient());
			
			FlaggedName[] flaggedNames = userAdminStub.getRolesOfUser(userName, "*", -1);
			List<String> roles = new ArrayList<String>();
			if (flaggedNames != null) {
				for (FlaggedName flaggedName : flaggedNames) {
					if (flaggedName.getSelected()) {
						roles.add(flaggedName.getItemName());
					}
				}
			}
			roles.addAll(roleList);
			userAdminStub.updateRolesOfUser(userName, roles.toArray(new String[roles.size()]));
		} else {
			log.error("User does not exist. Unable to approve user " + userName);
		} 
		
	}

	/**
	 * Method to delete a user
	 * @param serverURL
	 * @param adminUsername
	 * @param adminPassword
	 * @param userName
	 * @throws Exception
	 */
	protected static void deleteUser(String serverURL, String adminUsername,
	                                 String adminPassword, String userName) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Remove the rejected user :" + userName);
		}		
		String url = serverURL + "UserAdmin";
		
		int index = userName.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
		//remove the PRIMARY part from the user name
		if (index > 0) {
			if(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equalsIgnoreCase(userName.substring(0, index))){
				userName = userName.substring(index + 1);
			}			
		} 

		UserAdminStub userAdminStub = new UserAdminStub(url);
		CarbonUtils.setBasicAccessSecurityHeaders(adminUsername, adminPassword,
		                                          true, userAdminStub._getServiceClient());
		userAdminStub.deleteUser(userName);

	}
}
