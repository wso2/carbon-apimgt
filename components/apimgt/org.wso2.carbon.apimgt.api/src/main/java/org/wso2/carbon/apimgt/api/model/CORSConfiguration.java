/*
*  Copyright (c) 2005-2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.api.model;

import java.io.Serializable;
import java.util.Set;

public class CORSConfiguration implements Serializable {

	private boolean corsConfigurationEnabled;
	private Set<String> accessControlAllowOrigins;
	private boolean accessControlAllowCredentials;
	private Set<String> accessControlAllowHeaders;
	private Set<String> accessControlAllowMethods;

	public CORSConfiguration(boolean corsConfigurationEnabled, Set<String> accessControlAllowOrigins,
	                         boolean accessControlAllowCredentials,
	                         Set<String> accessControlAllowHeaders, Set<String> accessControlAllowMethods) {
		this.corsConfigurationEnabled = corsConfigurationEnabled;
		this.accessControlAllowOrigins = accessControlAllowOrigins;
		this.accessControlAllowCredentials = accessControlAllowCredentials;
		this.accessControlAllowHeaders = accessControlAllowHeaders;
		this.accessControlAllowMethods = accessControlAllowMethods;
	}

	public boolean isCorsConfigurationEnabled() {

		return corsConfigurationEnabled;
	}

	public void setCorsConfigurationEnabled(boolean corsConfigurationEnabled) {
		this.corsConfigurationEnabled = corsConfigurationEnabled;
	}

	public Set<String> getAccessControlAllowOrigins() {
		return accessControlAllowOrigins;
	}

	public void setAccessControlAllowOrigins(Set<String> accessControlAllowOrigins) {
		this.accessControlAllowOrigins = accessControlAllowOrigins;
	}

	public boolean isAccessControlAllowCredentials() {
		return accessControlAllowCredentials;
	}

	public void setAccessControlAllowCredentials(boolean accessControlAllowCredentials) {
		this.accessControlAllowCredentials = accessControlAllowCredentials;
	}

	public Set<String> getAccessControlAllowHeaders() {
		return accessControlAllowHeaders;
	}

	public void setAccessControlAllowHeaders(Set<String> accessControlAllowHeaders) {
		this.accessControlAllowHeaders = accessControlAllowHeaders;
	}

	public Set<String> getAccessControlAllowMethods() {
		return accessControlAllowMethods;
	}

	public void setAccessControlAllowMethods(Set<String> accessControlAllowMethods) {
		this.accessControlAllowMethods = accessControlAllowMethods;
	}
}
