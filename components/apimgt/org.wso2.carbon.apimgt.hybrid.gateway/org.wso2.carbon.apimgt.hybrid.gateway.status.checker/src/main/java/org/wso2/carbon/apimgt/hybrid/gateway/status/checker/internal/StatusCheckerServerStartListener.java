/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.apimgt.hybrid.gateway.status.checker.internal;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.hybrid.gateway.common.config.ConfigManager;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.OnPremiseGatewayConstants;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.apimgt.hybrid.gateway.status.checker.StatusChecker;
import org.wso2.carbon.apimgt.hybrid.gateway.status.checker.util.StatusCheckerConstants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Represents the listener that identify the server startup
 */
public class StatusCheckerServerStartListener implements ServerStartupHandler {

    private static final Log log = LogFactory.getLog(StatusCheckerServerStartListener.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Wait until the server starts for execution
     */
    @Override public void invoke() {
        if (log.isDebugEnabled()) {
            log.debug("StatusCheckerServerStartListener is activated");
        }
        startStatusCheck();
    }

    /**
     * Starts status check
     */
    public void startStatusCheck() {
        String token = null;
        String pingURL = null;
        try {
            token = ConfigManager.getConfigManager().getProperty(StatusCheckerConstants.UNIQUE_IDENTIFIER);
            pingURL = ConfigManager.getConfigManager().getProperty(StatusCheckerConstants.PING_API_URL);

        } catch (OnPremiseGatewayException e) {
            log.error("Error occurred while reading " + StatusCheckerConstants.PING_API_URL + " and " +
                              StatusCheckerConstants.UNIQUE_IDENTIFIER + " from " +
                              OnPremiseGatewayConstants.CONFIG_FILE_NAME);
        }
        //If pinging details are set, start pinging
        if (isPingURLSet(pingURL) && isTokenSet(token)) {
            scheduler.scheduleAtFixedRate(new StatusChecker(token, pingURL), 0, StatusCheckerConstants.PING_INTERVAL,
                                          TimeUnit.MINUTES);
            log.info("StatusCheckerServerStartListener started");
        } else {
            //Else print a log indicating that the pinging details are not set
            log.info("You have not configured the Micro Gateway Ping URL, pinging will not be activated.");
        }
    }

    /**
     * Checks if a correct value is set as token
     *
     * @param token String
     * @return isSet
     */
    public boolean isTokenSet(String token) {
        boolean isSet = false;
        if (StringUtils.isNotBlank(token) && !(OnPremiseGatewayConstants.UNIQUE_IDENTIFIER_HOLDER.equals(token))) {
            isSet = true;
        }
        return isSet;
    }

    /**
     * Checks if a correct value is set as ping URL
     *
     * @param pingURL String
     * @return isSet
     */
    public boolean isPingURLSet(String pingURL) {
        boolean isSet = false;
        if (StringUtils.isNotBlank(pingURL) && !(StatusCheckerConstants.PING_URL_HOLDER.equals(pingURL))) {
            isSet = true;
        }
        return isSet;
    }
}
