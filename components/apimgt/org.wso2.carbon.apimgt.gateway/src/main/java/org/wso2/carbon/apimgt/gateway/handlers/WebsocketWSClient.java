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

import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.keymgt.stub.validator.APIKeyValidationServiceStub;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.HashSet;


public class WebsocketWSClient {
	private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;

	private APIKeyValidationServiceStub keyValidationServiceStub;
	private String username;
	private String password;
	private String cookie;

	public WebsocketWSClient() throws APISecurityException {
		APIManagerConfiguration
				config = org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
		String serviceURL = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);
		username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
		password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD);
		if (serviceURL == null || username == null || password == null) {
			throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
			                               "Required connection details for the key management server not provided");
		}

		try {
			ConfigurationContext ctx = ConfigurationContextFactory
					.createConfigurationContextFromFileSystem(null, null);
			keyValidationServiceStub = new APIKeyValidationServiceStub(ctx, serviceURL + "APIKeyValidationService");
			ServiceClient client = keyValidationServiceStub._getServiceClient();
			Options options = client.getOptions();
			options.setTimeOutInMilliSeconds(TIMEOUT_IN_MILLIS);
			options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT_IN_MILLIS);
			options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT_IN_MILLIS);
			options.setCallTransportCleanup(true);
			options.setManageSession(true);


		} catch (AxisFault axisFault) {
			throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
			                               "Error while initializing the API key validation stub", axisFault);
		}
	}

	/**
	 * create the APIKeyValidationInfoDTO
	 * @param generatedDto
	 * @return
	 */
	private APIKeyValidationInfoDTO toDTO(
			org.wso2.carbon.apimgt.impl.dto.xsd.APIKeyValidationInfoDTO generatedDto) {
		APIKeyValidationInfoDTO dto = new APIKeyValidationInfoDTO();
		dto.setSubscriber(generatedDto.getSubscriber());
		dto.setAuthorized(generatedDto.getAuthorized());
		dto.setTier(generatedDto.getTier());
		dto.setType(generatedDto.getType());
		dto.setEndUserToken(generatedDto.getEndUserToken());
		dto.setEndUserName(generatedDto.getEndUserName());
		dto.setApplicationName(generatedDto.getApplicationName());
		dto.setEndUserName(generatedDto.getEndUserName());
		dto.setConsumerKey(generatedDto.getConsumerKey());
		//dto.setAuthorizedDomains(Arrays.asList(generatedDto.getAuthorizedDomains()));
		dto.setValidationStatus(generatedDto.getValidationStatus());
		dto.setApplicationId(generatedDto.getApplicationId());
		dto.setApplicationTier(generatedDto.getApplicationTier());
		dto.setApiPublisher(generatedDto.getApiPublisher());
		dto.setApiName(generatedDto.getApiName());
		dto.setValidityPeriod(generatedDto.getValidityPeriod());
		dto.setIssuedTime(generatedDto.getIssuedTime());
		dto.setApiTier(generatedDto.getApiTier());
		dto.setContentAware(generatedDto.getContentAware());
		dto.setScopes(generatedDto.getScopes() == null ? null : new HashSet<String>(
				Arrays.asList(generatedDto.getScopes())));
		dto.setThrottlingDataList(Arrays.asList(generatedDto.getThrottlingDataList()));
		dto.setSpikeArrestLimit(generatedDto.getSpikeArrestLimit());
		dto.setSpikeArrestUnit(generatedDto.getSpikeArrestUnit());
		dto.setSubscriberTenantDomain(generatedDto.getSubscriberTenantDomain());
		dto.setStopOnQuotaReach(generatedDto.getStopOnQuotaReach());
		return dto;
	}

	/**
	 * Initialize the WS key validation service
	 * @param context
	 * @param apiVersion
	 * @param apiKey
	 * @return
	 * @throws APISecurityException
	 */
	public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion, String apiKey) throws
	                                                                                               APISecurityException {

		CarbonUtils.setBasicAccessSecurityHeaders(username, password,
		                                          true, keyValidationServiceStub._getServiceClient());

		try {
			org.wso2.carbon.apimgt.impl.dto.xsd.APIKeyValidationInfoDTO dto =
					keyValidationServiceStub.validateKeyforHandshake(context, apiVersion, apiKey);
			return toDTO(dto);
		} catch (Exception e) {
			throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
			                               "Error while accessing backend services for API key validation", e);
		}
	}


}
