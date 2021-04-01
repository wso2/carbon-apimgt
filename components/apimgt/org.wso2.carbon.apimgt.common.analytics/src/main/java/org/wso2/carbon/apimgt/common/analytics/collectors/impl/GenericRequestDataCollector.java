/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.common.analytics.collectors.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.analytics.collectors.RequestDataCollector;
import org.wso2.carbon.apimgt.common.analytics.exceptions.AnalyticsException;

/**
 * Handle all the request and forward to appropriate sub request handlers.
 */
public class GenericRequestDataCollector implements RequestDataCollector {
    private static final Log log = LogFactory.getLog(GenericRequestDataCollector.class);
    private RequestDataCollector successDataCollector;
    private RequestDataCollector faultDataCollector;
    private RequestDataCollector unclassifiedDataCollector;
    private AnalyticsDataProvider provider;

    public GenericRequestDataCollector(AnalyticsDataProvider provider) {
        this.successDataCollector = new SuccessRequestDataCollector(provider);
        this.faultDataCollector = new FaultyRequestDataCollector(provider);
        this.unclassifiedDataCollector = new UnclassifiedRequestDataCollector(provider);
        this.provider = provider;
    }

    public void collectData() throws AnalyticsException {
        switch (provider.getEventCategory()) {
        case SUCCESS:
            successDataCollector.collectData();
            break;
        case FAULT:
            faultDataCollector.collectData();
            break;
        case INVALID:
            unclassifiedDataCollector.collectData();
            break;
        }
    }
}
