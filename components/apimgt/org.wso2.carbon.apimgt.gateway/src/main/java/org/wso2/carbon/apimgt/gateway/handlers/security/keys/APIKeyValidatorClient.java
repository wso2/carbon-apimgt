/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.keys;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.Header;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.keymgt.stub.validator.APIKeyValidationServiceAPIManagementException;
import org.wso2.carbon.apimgt.keymgt.stub.validator.APIKeyValidationServiceStub;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class APIKeyValidatorClient {

    private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;

    private APIKeyValidationServiceStub clientStub;
    private String username;
    private String password;
    private String cookie;
    
    public APIKeyValidatorClient() throws APISecurityException {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        String serviceURL = config.getFirstProperty(APIConstants.API_KEY_MANAGER_URL);
        username = config.getFirstProperty(APIConstants.API_KEY_MANAGER_USERNAME);
        password = config.getFirstProperty(APIConstants.API_KEY_MANAGER_PASSWORD);
        if (serviceURL == null || username == null || password == null) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Required connection details for the key management server not provided");
        }

        try {
            clientStub = new APIKeyValidationServiceStub(null, serviceURL + "APIKeyValidationService");
            ServiceClient client = clientStub._getServiceClient();
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

    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion,
                                                 String apiKey,String requiredAuthenticationLevel,
                                                 String clientDomain) throws APISecurityException {

        CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                true, clientStub._getServiceClient());
        if (cookie != null) {
            clientStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }
        try {
            List headerList = new ArrayList();
            Map headers = (Map) MessageContext.getCurrentMessageContext().getProperty(
                    org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            if (headers != null) {
                headerList.add(new Header("activityID", (String)headers.get("activityID")));
            }
            clientStub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.HTTP_HEADERS, headerList);
            org.wso2.carbon.apimgt.impl.dto.xsd.APIKeyValidationInfoDTO dto =
                    clientStub.validateKey(context, apiVersion, apiKey,requiredAuthenticationLevel,clientDomain);
            ServiceContext serviceContext = clientStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            return toDTO(dto);
        }
        catch (APIKeyValidationServiceAPIManagementException ex){
                    throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                                    "Resource forbidden", ex);
        }catch (Exception e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for API key validation", e);
        }
    }

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
        dto.setValidationStatus(generatedDto.getValidationStatus());
        dto.setApplicationId(generatedDto.getApplicationId());
        dto.setApplicationTier(generatedDto.getApplicationTier());
        return dto;
    }
    public ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion
    ) throws APISecurityException {

        CarbonUtils.setBasicAccessSecurityHeaders(username, password,
                                                  true, clientStub._getServiceClient());
        if (cookie != null) {
            clientStub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
        }
        try {
            org.wso2.carbon.apimgt.api.model.xsd.URITemplate[] dto =
                    clientStub.getAllURITemplates(context, apiVersion);
            ServiceContext serviceContext = clientStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            ArrayList<URITemplate> templates = new ArrayList<URITemplate>();
            for (org.wso2.carbon.apimgt.api.model.xsd.URITemplate aDto : dto) {
                URITemplate temp = toTemplates(aDto);
                templates.add(temp);
            }
            return templates;
        } catch (Exception e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                           "Error while accessing backend services for API key validation", e);
        }
    }
    private URITemplate toTemplates(
            org.wso2.carbon.apimgt.api.model.xsd.URITemplate dto) {
        URITemplate template = new URITemplate();
        template.setAuthType(dto.getAuthType());
        template.setHTTPVerb(dto.getHTTPVerb());
        template.setResourceSandboxURI(dto.getResourceSandboxURI());
        template.setUriTemplate(dto.getUriTemplate());
        template.setThrottlingTier(dto.getThrottlingTier());
        return template;
    }
}
