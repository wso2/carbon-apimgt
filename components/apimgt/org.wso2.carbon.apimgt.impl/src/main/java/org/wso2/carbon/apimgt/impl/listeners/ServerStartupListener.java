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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;

/**
 * Class for performing operations on initial server startup
 */
public class ServerStartupListener implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(ServerStartupListener.class);

    @Override
    public void completedServerStartup() {
        copyToExtensions();
    }

    /**
     * Method for copying identity component jsp pages to webapp extensions upon initial server startup
     */
    private static void copyToExtensions() {
        String repositoryDir = "repository";
        String resourcesDir = "resources";
        String extensionsDir = "extensions";
        String webappDir = "webapps";
        String authenticationEndpointDir = "authenticationendpoint";
        String accountRecoveryEndpointDir = "accountrecoveryendpoint";
        String headerJspFile = "header.jsp";
        String footerJspFile = "footer.jsp";
        String titleJspFile = "title.jsp";
        String cookiePolicyContentJspFile = "cookie-policy-content.jsp";
        String privacyPolicyContentJspFile = "privacy-policy-content.jsp";
        try {
            String resourceExtDirectoryPath =
                    CarbonUtils.getCarbonHome() + File.separator + repositoryDir + File.separator + resourcesDir
                            + File.separator + extensionsDir;
            String authenticationEndpointWebAppPath =
                    CarbonUtils.getCarbonRepository() + webappDir + File.separator + authenticationEndpointDir;
            String authenticationEndpointWebAppExtPath =
                    authenticationEndpointWebAppPath + File.separator + extensionsDir;
            String accountRecoveryWebAppPath =
                    CarbonUtils.getCarbonRepository() + webappDir + File.separator + accountRecoveryEndpointDir;
            String accountRecoveryWebAppExtPath = accountRecoveryWebAppPath + File.separator + extensionsDir;
            if (new File(resourceExtDirectoryPath).exists()) {
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

    @Override
    public void completingServerStartup() {
    }
}
