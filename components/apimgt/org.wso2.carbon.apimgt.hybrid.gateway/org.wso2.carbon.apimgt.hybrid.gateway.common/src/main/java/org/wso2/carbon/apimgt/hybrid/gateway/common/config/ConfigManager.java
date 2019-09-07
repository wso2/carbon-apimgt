/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.common.config;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.OnPremiseGatewayConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class used to read the on-premise-gateway.properties configuration file
 */
public class ConfigManager {

    private static final Log log = LogFactory.getLog(ConfigManager.class);

    private Properties configProperties;
    private static volatile ConfigManager configManager = null;

    private ConfigManager() throws OnPremiseGatewayException {
        init();
    }

    /**
     * Method to get micro API gateway configuration manager
     *
     * @return micro API gateway configuration manager
     */
    public static ConfigManager getConfigManager() throws OnPremiseGatewayException {
        if (configManager == null) {
            synchronized (ConfigManager.class) {
                if (configManager == null) {
                    configManager = new ConfigManager();
                }
            }
        }
        return configManager;
    }


    /**
     * Method to get initialize micro API gateway configuration
     *
     */
    private void init() throws OnPremiseGatewayException {
        configProperties = new Properties();
        String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator +
                OnPremiseGatewayConstants.CONFIG_FILE_NAME;
        try (InputStream inputStream = new FileInputStream(filePath)) {
            if (log.isDebugEnabled()) {
                log.debug("Reading On Premise Gateway configuration file from : "
                        + CarbonUtils.getCarbonConfigDirPath() + File.separator +
                        OnPremiseGatewayConstants.CONFIG_FILE_NAME);
            }
            configProperties.load(inputStream);
        } catch (IOException ex) {
            String errorMessage = "Error occurred while reading the config file : "
                    + OnPremiseGatewayConstants.CONFIG_FILE_NAME;
            throw new OnPremiseGatewayException(errorMessage, ex);
        }
    }

    /**
     * Method to get a micro API gateway config property given a key
     *
     */
    public String getProperty(String key) {
        return configProperties.getProperty(key);
    }
}
