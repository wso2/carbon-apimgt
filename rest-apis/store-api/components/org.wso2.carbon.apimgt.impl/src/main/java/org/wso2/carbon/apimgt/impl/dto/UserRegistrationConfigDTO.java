/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Object to store tenant signup information taken from the registry
 * 
 */
public class UserRegistrationConfigDTO implements Serializable{

    private static final long serialVersionUID = 453085948357718066L;
    //user store name
	private String signUpDomain;
	
	//tenant admin info
	private String adminUserName;	
	private String adminPassword;
	
	//whether self signup is enabled
	private boolean isSignUpEnabled;	

	private Map roles = new HashMap<String, Boolean>(); // role name - external
														// (true/false) mapping

	public String getSignUpDomain() {
		return signUpDomain;
	}

	public void setSignUpDomain(String signUpDomain) {
		this.signUpDomain = signUpDomain;
	}

	public Map<String, Boolean> getRoles() {
		return roles;
	}

	public void setRoles(Map roles) {
		this.roles = roles;
	}
	
	public String getAdminUserName() {
		return adminUserName;
	}

	public void setAdminUserName(String adminUserName) {
		this.adminUserName = adminUserName;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}
	
	public boolean isSignUpEnabled() {
		return isSignUpEnabled;
	}

	public void setSignUpEnabled(boolean isSignUpEnabled) {
		this.isSignUpEnabled = isSignUpEnabled;
	}


}
