/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl;

import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.APIManagementException;

/**
 * This interface used to handle newly introduced 2025 version of Developer Portal's configuration with APIM.
 */
public interface DevPortalHandler {

    boolean isPortalEnabled();

    String publishAPIMetadata(String organization, API api) throws APIManagementException;

    void updateAPIMetadata(String organization, API api, String refId) throws APIManagementException;

    void unpublishAPIMetadata(String organization, API api, String refId) throws APIManagementException;
}
