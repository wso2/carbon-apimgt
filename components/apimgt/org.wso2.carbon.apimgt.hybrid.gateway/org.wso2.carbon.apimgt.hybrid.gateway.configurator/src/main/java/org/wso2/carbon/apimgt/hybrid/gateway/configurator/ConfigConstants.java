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

package org.wso2.carbon.apimgt.hybrid.gateway.configurator;

/**
 * Config Constants used in Configurator
 */
public class ConfigConstants {

    public static final String CARBON_HOME = "carbon.home";
    public static final String REPOSITORY_DIR = "repository";
    public static final String CONF_DIR = "conf";
    public static final String UPDATES_DIR = "updates";
    public static final String WUM_DIR = "wum";

    public static final String HOST_NAME = "hostname";
    public static final String MAC_ADDRESS = "macAddress";
    public static final String PORT = "port";
    public static final String LAST_WUM_UPDATE = "lastWumUpdate";

    public static final String CONFIGURE_LOCK_FILE_NAME = "configure.lck";
    public static final String CONFIG_TOOL_CONFIG_FILE_NAME = "gateway-config-tool.properties";
    public static final String GATEWAY_CARBON_FILE_NAME = "carbon.xml";
    public static final String HYBRID_GATEWAY_ENV_METADATA = "api.hybrid.meta.env.";
    public static final String DEFAULT_FILE_DATA_PUBLISHER_CLASS
            = "org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.APIMgtUsageFileDataPublisher";
    public static final String DEFAULT_FILE_DATA_UPLOAD_TASK_CLASS
            = "org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.tasks.APIUsageFileUploadTask";

    public static final String DEFAULT_FILE_DATA_CLEANUP_TASK_CLASS
            = "org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.tasks.APIUsageFileCleanupTask";

    public static final String DEFAULT_THROTTLING_SYNC_TASK_CLASS
            = "org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.tasks.ThrottlingSyncTask";
    public static final String DEFAULT_API_UPDATE_TASK_CLASS
            = "org.wso2.carbon.apimgt.hybrid.gateway.api.synchronizer.tasks.APISynchronizationTask";

    public static final String DELIMITER = ":";
    public static final int GATEWAY_DEFAULT_PORT = 9443;
    public static final String DEFAULT_MAC_ADDRESS = "00:00:00:00:00";
    public static final String START_OFFSET_TAG = "<Offset>";
    public static final String END_OFFSET_TAG = "</Offset>";
}
