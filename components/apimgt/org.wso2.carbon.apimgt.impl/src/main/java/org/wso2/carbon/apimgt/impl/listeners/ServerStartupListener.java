/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.listeners;

import org.apache.axis2.util.JavaUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.loader.KeyManagerConfigurationDataRetriever;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Class for performing operations on initial server startup
 */
public class ServerStartupListener implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(ServerStartupListener.class);

    @Override
    public void completedServerStartup() {

        copyToExtensions();

        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (apiManagerConfiguration != null) {
            String enableKeyManagerRetrieval =
                    apiManagerConfiguration.getFirstProperty(APIConstants.ENABLE_KEY_MANAGER_RETRIVAL);
            if (JavaUtils.isTrueExplicitly(enableKeyManagerRetrieval)) {
                startConfigureKeyManagerConfigurations();
            }
            Map<String, TokenIssuerDto> tokenIssuerDtoMap =
                    apiManagerConfiguration.getJwtConfigurationDto().getTokenIssuerDtoMap();
            tokenIssuerDtoMap.forEach((issuer, tokenIssuer) -> KeyManagerHolder.addGlobalJWTValidators(tokenIssuer));
        }
    }

    /**
     * Method for copying identity component jsp pages to webapp extensions upon initial server startup
     */
    private static void copyToExtensions() {
        String repositoryDir = "repository";
        String resourcesDir = "resources";
        String extensionsDir = "extensions";
        String customAssetsDir = "customAssets";
        String webappDir = "webapps";
        String authenticationEndpointDir = "authenticationendpoint";
        String accountRecoveryEndpointDir = "accountrecoveryendpoint";
        String headerJspFile = "header.jsp";
        String footerJspFile = "product-footer.jsp";
        String titleJspFile = "product-title.jsp";
        String cookiePolicyContentJspFile = "cookie-policy-content.jsp";
        String privacyPolicyContentJspFile = "privacy-policy-content.jsp";
        try {
            String resourceExtDirectoryPath =
                    CarbonUtils.getCarbonHome() + File.separator + repositoryDir + File.separator + resourcesDir
                            + File.separator + extensionsDir;
            String customAssetsExtDirectoryPath =
                    CarbonUtils.getCarbonHome() + File.separator + repositoryDir + File.separator + resourcesDir
                    + File.separator + extensionsDir + File.separator + customAssetsDir;
            String authenticationEndpointWebAppPath =
                    CarbonUtils.getCarbonRepository() + webappDir + File.separator + authenticationEndpointDir;
            String authenticationEndpointWebAppExtPath =
                    authenticationEndpointWebAppPath + File.separator + extensionsDir;
            String accountRecoveryWebAppPath =
                    CarbonUtils.getCarbonRepository() + webappDir + File.separator + accountRecoveryEndpointDir;
            String accountRecoveryWebAppExtPath = accountRecoveryWebAppPath + File.separator + extensionsDir;
            if (new File(resourceExtDirectoryPath).exists()) {
                // delete extensions directory from the webapp folders if they exist
                FileUtils.deleteDirectory(new File(authenticationEndpointWebAppExtPath));
                FileUtils.deleteDirectory(new File(accountRecoveryWebAppExtPath));
                log.info("Starting to copy identity page extensions...");
                String headerJsp = resourceExtDirectoryPath + File.separator + headerJspFile;
                String footerJsp = resourceExtDirectoryPath + File.separator + footerJspFile;
                String titleJsp = resourceExtDirectoryPath + File.separator + titleJspFile;
                String cookiePolicyContentJsp = resourceExtDirectoryPath + File.separator + cookiePolicyContentJspFile;
                String privacyPolicyContentJsp =
                        resourceExtDirectoryPath + File.separator + privacyPolicyContentJspFile;
                if (new File(headerJsp).exists()) {
                    copyFileToDirectory(headerJsp, authenticationEndpointWebAppExtPath,
                            authenticationEndpointWebAppPath);
                    copyFileToDirectory(headerJsp, accountRecoveryWebAppExtPath, accountRecoveryWebAppPath);
                }
                if (new File(footerJsp).exists()) {
                    copyFileToDirectory(footerJsp, authenticationEndpointWebAppExtPath,
                            authenticationEndpointWebAppPath);
                    copyFileToDirectory(footerJsp, accountRecoveryWebAppExtPath, accountRecoveryWebAppPath);
                }
                if (new File(titleJsp).exists()) {
                    copyFileToDirectory(titleJsp, authenticationEndpointWebAppExtPath,
                            authenticationEndpointWebAppPath);
                    copyFileToDirectory(titleJsp, accountRecoveryWebAppExtPath, accountRecoveryWebAppPath);
                }
                if (new File(cookiePolicyContentJsp).exists()) {
                    copyFileToDirectory(cookiePolicyContentJsp, authenticationEndpointWebAppExtPath,
                            authenticationEndpointWebAppPath);
                }
                if (new File(privacyPolicyContentJsp).exists()) {
                    copyFileToDirectory(privacyPolicyContentJsp, authenticationEndpointWebAppExtPath,
                            authenticationEndpointWebAppPath);
                }
                // copy custom asset files to the webapp directories
                if (new File(customAssetsExtDirectoryPath).exists()) {
                    FileUtils.copyDirectory(new File(customAssetsExtDirectoryPath),
                            new File(authenticationEndpointWebAppExtPath + File.separator + customAssetsDir));
                    FileUtils.copyDirectory(new File(customAssetsExtDirectoryPath),
                            new File(accountRecoveryWebAppExtPath + File.separator + customAssetsDir));
                }
                log.info("Successfully completed copying identity page extensions");
            }
        } catch (IOException ex) {
            log.error("An error occurred while copying extension files to web apps", ex);
        }
    }

    private static void copyFileToDirectory(String filePath, String directoryPath, String parentDir)
            throws IOException {
        try {
            if (new File(parentDir).exists()) {
                FileUtils.copyFileToDirectory(new File(filePath), new File(directoryPath));
            }
        } catch (IOException ex) {
            log.error("An error occurred while copying file to directory", ex);
            throw new IOException("An error occurred while copying file to directory", ex);
        }
    }

    private void startConfigureKeyManagerConfigurations() {

        KeyManagerConfigurationDataRetriever keyManagerConfigurationDataRetriever =
                new KeyManagerConfigurationDataRetriever(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        keyManagerConfigurationDataRetriever.startLoadKeyManagerConfigurations();
    }

    @Override
    public void completingServerStartup() {
    }
}
