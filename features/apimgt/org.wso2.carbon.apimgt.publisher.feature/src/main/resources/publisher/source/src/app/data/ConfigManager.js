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

import axios from 'axios';

class ConfigManager {
    /**
     * get promised config and update the configMap
     * @param configPath: Path to read configs from
     * @returns {Promise}: promised config
     * @private
     */
    /* eslint-disable no-underscore-dangle */
    // indicate “private” members of APIClientFactory that is why underscore has used here
    static _getPromisedConfigs(configPath) {
        let promisedConfig = ConfigManager._promisedConfigMap.get(configPath);
        if (promisedConfig) {
            return promisedConfig;
        }
        const { origin } = window.location;
        const requestUrl = origin + configPath;

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
            environments: ConfigManager._getPromisedConfigs(ConfigManager.ConfigRequestPaths.ENVIRONMENT_CONFIG_PATH),
            features: ConfigManager._getPromisedConfigs(ConfigManager.ConfigRequestPaths.FEATURE_LIST_PATH),
        };
    }
}

/**
 * ConfigRequestPaths: Configuration requesting url paths
 * @type {Object}
 */
ConfigManager.ConfigRequestPaths = {
    ENVIRONMENT_CONFIG_PATH: '/api/am/config/v1.0/environments',
    FEATURE_LIST_PATH: '/api/am/config/v1.0/features',
};

/**
 * The map of single instance promised configs objects
 * {key}: ConfigRequestPaths
 * {value}: promised config
 * @type {Map}
 * @private
 */
ConfigManager._promisedConfigMap = new Map();
/* eslint-enable no-underscore-dangle */
export default ConfigManager;
