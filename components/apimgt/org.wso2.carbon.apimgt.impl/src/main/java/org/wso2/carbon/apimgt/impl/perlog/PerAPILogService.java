/*
 *
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.apimgt.impl.perlog;

import java.util.Map;

/**
 * This interfact PerAPILogService represents the operations related to per API logging
 * It involves publishing log data to TM, syncing with local node, retrieve data etc
 */

public interface PerAPILogService {
    /**
     * Publish the API log details to TM to be synced across gateways
     *
     * @param context context of the API
     * @param value   logLevel of the API to be logged
     */
    void publishLogAPIData(String context, String value);

    /**
     * Retrieve the API data related to logging
     *
     * @return Map of API context and log level
     */
    Map<String, String> getLogData();

    /**
     * Retrieve the log level of a given API context if it exists
     * If not, it would retrieve null value
     *
     * @param context
     * @return
     */
    String getLogData(String context);

    /**
     * Sync local API data holding map wit
     *
     * @param details Map of the new entries given by the user
     */
    void syncLocalAPILogDetailsMap(Map<String, Object> details);

}
