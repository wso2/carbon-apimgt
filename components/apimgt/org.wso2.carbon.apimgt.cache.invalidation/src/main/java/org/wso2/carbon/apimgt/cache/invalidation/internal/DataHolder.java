/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.cache.invalidation.internal;

import org.wso2.carbon.apimgt.cache.invalidation.utils.JMSTransportHandler;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.CacheInvalidationConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;

import java.util.UUID;

public class DataHolder {

    private static final String nodeId = UUID.randomUUID().toString();
    private static final DataHolder instance = new DataHolder();
    private OutputEventAdapterService outputEventAdapterService;
    private APIManagerConfigurationService apiManagerConfigurationService;
    private Boolean started = false;
    private JMSTransportHandler jmsTransportHandler;
    private CacheInvalidationConfiguration cacheInvalidationConfiguration;
    private DataHolder() {

    }

    public static DataHolder getInstance() {

        return instance;
    }

    public static String getNodeId() {

        return nodeId;
    }

    public APIManagerConfigurationService getAPIManagerConfigurationService() {

        return apiManagerConfigurationService;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {

        this.apiManagerConfigurationService = amcService;
        if (apiManagerConfigurationService != null &&
                apiManagerConfigurationService.getAPIManagerConfiguration() != null) {
            cacheInvalidationConfiguration =
                    apiManagerConfigurationService.getAPIManagerConfiguration().getCacheInvalidationConfiguration();
        }
    }

    public OutputEventAdapterService getOutputEventAdapterService() {

        return outputEventAdapterService;
    }

    public void setOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {

        this.outputEventAdapterService = outputEventAdapterService;
    }

    public boolean isStarted() {

        return started;
    }

    public void setStarted(Boolean started) {

        synchronized (this.started) {
            this.started = started;
        }
    }

    public JMSTransportHandler getJmsTransportHandler() {

        return jmsTransportHandler;
    }

    public void setJmsTransportHandler(JMSTransportHandler jmsTransportHandler) {

        this.jmsTransportHandler = jmsTransportHandler;
    }

    public CacheInvalidationConfiguration getCacheInvalidationConfiguration() {

        return cacheInvalidationConfiguration;
    }
}
