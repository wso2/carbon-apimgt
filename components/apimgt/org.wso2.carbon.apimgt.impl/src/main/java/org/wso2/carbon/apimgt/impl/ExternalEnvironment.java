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
 */
package org.wso2.carbon.apimgt.impl;

import org.wso2.carbon.apimgt.api.model.AsyncProtocolEndpoint;
import org.wso2.carbon.apimgt.api.model.Environment;

import java.util.List;

/**
 * This class controls the External environment object parsing tasks
 */
public interface ExternalEnvironment {

    /**
     * Get external endpoints of the external environment
     *
     * @return List of protocol endpoint URLs map
     */

    public List<AsyncProtocolEndpoint> getExternalEndpointURLs(Environment environment) ;

    /**
     * Get provider of the external environment
     *
     * @return String vendor name
     */
    public String getType();
}
