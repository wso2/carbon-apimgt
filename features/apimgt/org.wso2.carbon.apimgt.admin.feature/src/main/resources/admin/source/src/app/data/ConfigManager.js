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

    /**
     * get promised config and update the configMap
     * @param configPath: Path to read configs from
     * @returns {Promise Object}: promised config
     * @private
     */
    static _getPromisedConfigs(configPath) {
        let promisedConfig = ConfigManager._promisedConfigMap.get(configPath);
        if (promisedConfig) {
            return promisedConfig;
        }
        let origin = window.location.origin;
        let requestUrl = origin + configPath;

        promisedConfig = axios.get(requestUrl);
        ConfigManager._promisedConfigMap.set(configPath, promisedConfig);
        return promisedConfig;
    }

    /**
     * get configurations from server: deployment.yaml
     * @returns {Object}: configuration object
     */
    static getConfigs() {
        return {
            'environments': ConfigManager._getPromisedConfigs(ConfigRequestPaths.ENVIRONMENT_CONFIG_PATH)
        };
    }
}

/**
 * @type {{ConstPath: StringPath}} ConfigRequestPaths: Configuration requesting url paths
 */
const ConfigRequestPaths = {
    ENVIRONMENT_CONFIG_PATH: "/api/am/config/v1.0/environments"
};

/**
 * The map of single instance promised configs objects
 * {key}: ConfigRequestPaths
 * {value}: promised config
 * @type {Map}
 * @private
 */
ConfigManager._promisedConfigMap = new Map();

export default ConfigManager;