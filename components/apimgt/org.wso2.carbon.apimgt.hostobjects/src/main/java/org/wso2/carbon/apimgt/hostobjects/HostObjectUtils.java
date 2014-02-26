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


package org.wso2.carbon.apimgt.hostobjects;

import java.util.Comparator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.hostobjects.internal.HostObjectComponent;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClient;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsagePublisherConstants;
import org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

public class HostObjectUtils {
    private static final Log log = LogFactory.getLog(APIProviderHostObject.class);
    private static ConfigurationContextService configContextService = null;

     public static void setConfigContextService(ConfigurationContextService configContext) {
        HostObjectUtils.configContextService = configContext;
    }

    public static ConfigurationContext getConfigContext() throws APIManagementException {
        if (configContextService == null) {
            throw new APIManagementException("ConfigurationContextService is null");
        }

        return configContextService.getServerConfigContext();

    }

    /**
     * Get the running transport port
     *
     * @param transport [http/https]
     * @return port
     */
    public static String getBackendPort(String transport) {
        int port;
        String backendPort;
        try {
            port = CarbonUtils.getTransportProxyPort(getConfigContext(), transport);
            if (port == -1) {
                port = CarbonUtils.getTransportPort(getConfigContext(), transport);
            }
            backendPort = Integer.toString(port);
            return backendPort;
        } catch (APIManagementException e) {
            log.error(e.getMessage());
            return null;

        }
    }

    protected static SubscriberKeyMgtClient getKeyManagementClient() throws APIManagementException {
        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String url = config.getFirstProperty(APIConstants.API_KEY_MANAGER_URL);
        if (url == null) {
            handleException("API key manager URL unspecified");
        }

        String username = config.getFirstProperty(APIConstants.API_KEY_MANAGER_USERNAME);
        String password = config.getFirstProperty(APIConstants.API_KEY_MANAGER_PASSWORD);
        if (username == null || password == null) {
            handleException("Authentication credentials for API key manager unspecified");
        }

        try {
            return new SubscriberKeyMgtClient(url, username, password);
        } catch (Exception e) {
            handleException("Error while initializing the subscriber key management client", e);
            return null;
        }
    }

    private static void handleException(String msg) throws APIManagementException {
        log.error(msg);
        throw new APIManagementException(msg);
    }

    private static void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }
    
    public static class RequiredUserFieldComparator implements Comparator<UserFieldDTO> {

        public int compare(UserFieldDTO filed1, UserFieldDTO filed2) {
            if (filed1.getDisplayOrder() == 0) {
                filed1.setDisplayOrder(Integer.MAX_VALUE);
            }

            if (filed2.getDisplayOrder() == 0) {
                filed2.setDisplayOrder(Integer.MAX_VALUE);
            }
            
            if (!filed1.getRequired() && filed2.getRequired()){
            	return 1;
            }
            
            if (filed1.getRequired() && filed2.getRequired()){
            	return 0;
            }
            
            if (filed1.getRequired() && !filed2.getRequired()){
            	return -1;
            }

            return 0;
        }

    }
    public static class UserFieldComparator implements Comparator<UserFieldDTO> {

        public int compare(UserFieldDTO filed1, UserFieldDTO filed2) {
            if (filed1.getDisplayOrder() == 0) {
                filed1.setDisplayOrder(Integer.MAX_VALUE);
            }

            if (filed2.getDisplayOrder() == 0) {
                filed2.setDisplayOrder(Integer.MAX_VALUE);
            }
            
            if (filed1.getDisplayOrder() < filed2.getDisplayOrder()) {
                return -1;
            }
            if (filed1.getDisplayOrder() == filed2.getDisplayOrder()) {
                return 0;
            }
            if (filed1.getDisplayOrder() > filed2.getDisplayOrder()) {
                return 1;
            }
            return 0;
        }

    }

    protected static boolean checkDataPublishingEnabled() {
        APIManagerConfiguration configuration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String enabledStr = configuration.getFirstProperty(APIMgtUsagePublisherConstants.API_USAGE_ENABLED);
        return enabledStr != null && Boolean.parseBoolean(enabledStr);
    }
}
