/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import axios from 'axios'

class ConfigManager {

    constructor() {
    }

    /**
     * get configurations from server: deployment.yaml
     * @returns {Object}: configuration object
     */
    static getConfigs() {
        return {
            'environments': ConfigRequestMethods.promised_environments()
        };
    }
}

/**
 * @type {{ConstPath: StringPath}} ConfigRequestPaths: Configuration requesting url paths
 */
const ConfigRequestPaths = {
    ENVIRONMENT_CONFIG_PATH: "/configService/environments"
};

/**
 * @type {{'ConstRequestString': (function Request_Method())}} ConfigRequestMethods: Configuration requesting methods
 */
const ConfigRequestMethods = {
    promised_environments() {
        if (ConfigManager._promised_environments) {
            return ConfigManager._promised_environments;
        }
        let host = window.location.origin;
        let requestUrl = host + ConfigRequestPaths.ENVIRONMENT_CONFIG_PATH;

        ConfigManager._promised_environments = axios.get(requestUrl);
        return ConfigManager._promised_environments;
    }
};

//List of private promised class variables to preserve single instance
ConfigManager._promised_environments = null;

export default ConfigManager;