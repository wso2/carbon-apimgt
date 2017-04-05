package org.wso2.carbon.apimgt.gateway.internal;
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

//import org.wso2.carbon.kernel.configprovider.ConfigProvider;

import org.wso2.carbon.apimgt.gateway.analytics.AnalyticsConfiguration;
import org.wso2.carbon.apimgt.gateway.analytics.EventPublisher;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;


/**
 * Class used to hold the APIM configuration
 */
public class ServiceReferenceHolder {
    private static ServiceReferenceHolder instance = new ServiceReferenceHolder();
    private ConfigProvider configProvider;
    private EventPublisher publisher;
    private AnalyticsConfiguration analyticsConfiguration;

    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public ConfigProvider getConfigProvider() {
        return configProvider;
    }

    public EventPublisher getPublisher() {
        return publisher;
    }

    public void setPublisher(EventPublisher publisher) {
        this.publisher = publisher;
    }

    public AnalyticsConfiguration getAnalyticsConfiguration() {
        return analyticsConfiguration;
    }

    public void setAnalyticsConfiguration(AnalyticsConfiguration analyticsConfiguration) {
        this.analyticsConfiguration = analyticsConfiguration;
    }
}
