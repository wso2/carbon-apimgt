/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.apk.service.init;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.apk.apimgt.impl.APIManagerConfiguration;
import org.wso2.apk.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.apk.apimgt.impl.caching.CacheProvider;
import org.wso2.apk.apimgt.impl.internal.ServiceReferenceHolder;


public class APKComponent {

    public void activate(String configuration) {

        // Set configurations
        APIManagerConfiguration config = null;
        ObjectMapper objectMapper = new ObjectMapper();
        // TODO: Read configuration object and use object mapper to load APIManager Configuration
        //  APIManagerConfiguration config = objectMapper.readValue(configuration, APIManagerConfiguration.class);
        APIManagerConfigurationServiceImpl configurationService = new APIManagerConfigurationServiceImpl(config);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(configurationService);

        // initialize API-M Caches
        CacheProvider.createTenantConfigCache();
    }
}
