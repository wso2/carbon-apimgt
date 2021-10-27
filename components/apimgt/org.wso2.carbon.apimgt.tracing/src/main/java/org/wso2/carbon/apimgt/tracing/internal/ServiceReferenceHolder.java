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

package org.wso2.carbon.apimgt.tracing.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.tracing.OpenTracer;

import java.util.HashMap;
import java.util.Map;

/**
 * This class holds the osgi references for opentracing.
 */
public class ServiceReferenceHolder {
    private static final Log log = LogFactory.getLog(TracingServiceComponent.class);
    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();
    private APIManagerConfiguration configuration;
    private static Map<String, OpenTracer> openTracerMap = new HashMap();
    private ServiceReferenceHolder() {
    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public APIManagerConfiguration getAPIManagerConfiguration() {
        return configuration;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService amConfigService) {
        if (amConfigService != null) {
            this.configuration = amConfigService.getAPIManagerConfiguration();
        }
    }

    public static Map<String, OpenTracer> getOpenTracerMap() {

        return openTracerMap;
    }
}
