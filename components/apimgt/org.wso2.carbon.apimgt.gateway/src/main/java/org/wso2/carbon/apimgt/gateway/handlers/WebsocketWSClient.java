/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers;

import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.keymgt.service.APIKeyValidationService;

import java.util.Arrays;

public class WebsocketWSClient {

	private APIKeyValidationService apiKeyValidationService;


	public WebsocketWSClient() {
		apiKeyValidationService = new APIKeyValidationService();
	}

	/**
	 * Initialize the WS key validation service
	 *
	 * @param context
	 * @param apiVersion
	 * @param apiKey
	 * @param tenantDomain
	 * @param matchingResource
	 * @return
	 * @throws APISecurityException
	 */
	public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion, String apiKey, String tenantDomain,
	                                             String matchingResource) throws APISecurityException {
		try {
			return apiKeyValidationService.validateKeyForHandshake(context, apiVersion, apiKey, tenantDomain,
			                                                       Arrays.asList(
					                                                       APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS),
			                                                       matchingResource);
		} catch (Exception e) {
			throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
			                               "Error while accessing backend services for API key validation", e);
		}
	}
}
