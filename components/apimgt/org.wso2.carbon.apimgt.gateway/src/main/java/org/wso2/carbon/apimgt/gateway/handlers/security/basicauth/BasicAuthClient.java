/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.basicauth;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.dto.BasicAuthValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.keymgt.stub.usermanager.APIKeyMgtRemoteUserStoreMgtServiceAPIManagementException;
import org.wso2.carbon.apimgt.keymgt.stub.usermanager.APIKeyMgtRemoteUserStoreMgtServiceStub;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;

/**
 * Client class to handle APIKeyMgtRemoteUserStoreMgtServiceStub operations.
 */
public class BasicAuthClient {

    private static final Log log = LogFactory.getLog(BasicAuthClient.class);
    private APIKeyMgtRemoteUserStoreMgtServiceStub apiKeyMgtRemoteUserStoreMgtServiceStub;

    /**
     * Constructor to initialize the BasicAuthClient with service stub using the default
     * configuration context and event hub configuration from the ServiceReferenceHolder.
     *
     * @throws APISecurityException If initialization fails
     */
    public BasicAuthClient() throws APISecurityException {

        ConfigurationContext configurationContext = ServiceReferenceHolder.getInstance()
                .getAxis2ConfigurationContext();
        EventHubConfigurationDto eventHubConfigurationDto = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfiguration().getEventHubConfigurationDto();
        initializeServiceStub(configurationContext, eventHubConfigurationDto);
    }

    /**
     * Initialize the APIKeyMgtRemoteUserStoreMgtServiceStub with proper configuration.
     *
     * @param configurationContext     The configuration context
     * @param eventHubConfigurationDto Event hub configuration containing service details
     * @throws APISecurityException If initialization fails
     */
    private void initializeServiceStub(ConfigurationContext configurationContext,
                                       EventHubConfigurationDto eventHubConfigurationDto) throws APISecurityException {

        String username = eventHubConfigurationDto.getUsername();
        char[] passwordCharArray = eventHubConfigurationDto.getPassword().toCharArray();
        String url = eventHubConfigurationDto.getServiceUrl();

        if (url == null) {
            log.error("API key manager URL is not configured");
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "API key manager URL unspecified");
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing APIKeyMgtRemoteUserStoreMgtServiceStub with URL: " + url);
            }
            apiKeyMgtRemoteUserStoreMgtServiceStub = new APIKeyMgtRemoteUserStoreMgtServiceStub(configurationContext,
                    url + "/services/APIKeyMgtRemoteUserStoreMgtService");

            ServiceClient client = apiKeyMgtRemoteUserStoreMgtServiceStub._getServiceClient();
            Options options = client.getOptions();
            options.setCallTransportCleanup(true);
            options.setManageSession(true);

            if (System.getProperty(APIMgtGatewayConstants.AUTO_TRANSPORT_OPERATION_CLEANUP) != null) {
                options.setProperty(ServiceClient.AUTO_OPERATION_CLEANUP,
                        Boolean.parseBoolean(
                                System.getProperty(APIMgtGatewayConstants.AUTO_TRANSPORT_OPERATION_CLEANUP)));
            }

            CarbonUtils.setBasicAccessSecurityHeaders(username, new String(passwordCharArray), client);
        } catch (AxisFault axisFault) {
            log.error("Failed to initialize APIKeyMgtRemoteUserStoreMgtService stub: " + axisFault.getMessage());
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    axisFault.getMessage(), axisFault);
        }
    }

    /**
     * Get user authentication information from the remote user store.
     *
     * @param username The username to authenticate
     * @param password The password to authenticate
     * @return BasicAuthValidationInfoDTO containing authentication result
     * @throws APISecurityException If authentication check fails
     */
    public BasicAuthValidationInfoDTO getUserAuthenticationInfo(String username, String password)
            throws APISecurityException {

        try {
            org.wso2.carbon.apimgt.impl.dto.xsd.BasicAuthValidationInfoDTO generatedInfoDTO =
                    apiKeyMgtRemoteUserStoreMgtServiceStub.getUserAuthenticationInfo(username, password);
            return convertToDTO(generatedInfoDTO);
        } catch (APIKeyMgtRemoteUserStoreMgtServiceAPIManagementException | RemoteException e) {
            log.error("Failed to authenticate user", e);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, e.getMessage(), e);
        }
    }

    /**
     * Convert the generated DTO to the local BasicAuthValidationInfoDTO.
     *
     * @param generatedDto The generated DTO from the service
     * @return BasicAuthValidationInfoDTO The converted DTO
     */
    private BasicAuthValidationInfoDTO convertToDTO(
            org.wso2.carbon.apimgt.impl.dto.xsd.BasicAuthValidationInfoDTO generatedDto) {

        BasicAuthValidationInfoDTO dto = new BasicAuthValidationInfoDTO();
        if (generatedDto == null) {
            return new BasicAuthValidationInfoDTO(); // authenticated=false by default
        }
        dto.setAuthenticated(generatedDto.getAuthenticated());
        dto.setHashedPassword(generatedDto.getHashedPassword());
        dto.setDomainQualifiedUsername(generatedDto.getDomainQualifiedUsername());
        dto.setUserRoleList(generatedDto.getUserRoleList());
        return dto;
    }
}
