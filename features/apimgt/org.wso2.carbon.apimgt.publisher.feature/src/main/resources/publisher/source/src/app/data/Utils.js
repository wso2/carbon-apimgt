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

import CONSTS from 'AppData/Constants';

/**
 * Utility class for Publisher application
 */
class Utils {
    /**
     * Get JavaScript accessible cookies saved in browser, by giving the cooke name.
     * @param {String} nameWithEnv - Name of the cookie which need to be retrieved
     * @param {String} environmentName - label of the environment of the cookie
     * @returns {String|null} - If found a cookie with given name , return its value,Else null value is returned
     */
    static getCookie(name, environmentName = Utils.getCurrentEnvironment().label) {
        const nameWithEnv = `${name}_${environmentName}`;

        const pairs = document.cookie.split(';');
        let cookie = null;
        for (let pair of pairs) {
            pair = pair.split('=');
            const cookieName = pair[0].trim();
            if (pair[1] !== 'undefined') {
                const value = encodeURIComponent(pair[1]);
                if (cookieName === nameWithEnv) {
                    cookie = value;
                    break;
                }
            }
        }
        return cookie;
    }

    /**
     * Get JavaScript accessible cookies saved in browser, by giving the cooke name.
     * @param {String} name - Name of the cookie which need to be retrieved
     * @returns {String|null} - If found a cookie with given name , return its value,Else null value is returned
     */
    static getCookieWithoutEnvironment(name) {
        const pairs = document.cookie.split(';');
        let cookie = null;
        for (let pair of pairs) {
            pair = pair.split('=');
            const cookieName = pair[0].trim();
            if (pair[1] !== 'undefined') {
                const value = encodeURIComponent(pair[1]);
                if (cookieName === name) {
                    cookie = value;
                    break;
                }
            }
        }
        return cookie;
    }

    /**
     * Delete a browser cookie given its name
     * @param {String} name - Name of the cookie which need to be deleted
     * @param {String} path - Path of the cookie which need to be deleted
     * @param {String} environmentName - label of the environment of the cookie
     */
    static deleteCookie(name, path, environmentName = Utils.getCurrentEnvironment().label) {
        document.cookie = `${name}_${environmentName}=; path=${path}; expires=Thu, 01 Jan 1970 00:00:01 GMT`;
    }

    /**
     * Set a cookie with given name and value assigned to it. Cookies can be only set to the same origin,
     * which the script is running
     * @param {String} name - Name of the cookie which need to be set
     * @param {String} value - Value of the cookie, expect it to be URLEncoded
     * @param {number} validityPeriod -  (Optional) Validity period of the cookie in seconds
     * @param {String} path - Path which needs to set the given cookie
     * @param {String} environmentName - Name of the environment to be appended to cookie name
     * @param {boolean} secured - secured parameter is set
     */
    static setCookie(
        name,
        value,
        validityPeriod,
        path = '/',
        environmentName = Utils.getCurrentEnvironment().label,
        secured = true,
    ) {
        let expiresDirective = '';
        const securedDirective = secured ? '; Secure' : '';
        if (validityPeriod) {
            const date = new Date();
            if (validityPeriod < 0) {
                date.setTime(date.getTime() + 1000000000000);
            } else {
                date.setTime((date.getTime() + validityPeriod) * 1000);
            }
            expiresDirective = '; expires=' + date.toUTCString();
        }

        document.cookie = `${name}_${environmentName}=${value}; path=${path}${expiresDirective}${securedDirective}`;
    }

    /**
     * Given an object returns whether the object is empty or not
     * @param {Object} object - Any JSON object
     * @returns {boolean}
     */
    static isEmptyObject(object) {
        return Object.keys(object).length === 0 && object.constructor === Object;
    }

    /**
     * Get the current environment from local-storage
     * @returns {Object} environment: {label, host, loginTokenPath}
     */
    static getCurrentEnvironment() {
        if (Utils.environment) {
            return Utils.environment;
        }

        const environmentData = localStorage.getItem(Utils.CONST.LOCAL_STORAGE_ENVIRONMENT);
        if (!environmentData) {
            return Utils.getDefaultEnvironment();
        }

        return JSON.parse(environmentData);
    }

    /**
     * Get current environment's index from the given environment array
     * @param {Array} environments - Array of environments
     * @param {string} name - name of the environment
     * @returns {number}
     */
    static getEnvironmentID(environments, name = Utils.getCurrentEnvironment().label) {
        if (!name) {
            return 0;
        }

        for (let i = 0; i < environments.length; i++) {
            if (name === environments[i].label) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Store the given environment in local-storage
     * @param {object} defaultEnvironment
     */
    static setEnvironment(environment) {
        let defaultEnvironment = environment;
        if (!environment) {
            defaultEnvironment = Utils.getDefaultEnvironment();
        }

        if (!environment.host) {
            defaultEnvironment.host = window.location.host;
        }
        // Store environment.
        Utils.environment = defaultEnvironment;
        localStorage.setItem(Utils.CONST.LOCAL_STORAGE_ENVIRONMENT, JSON.stringify(defaultEnvironment));
    }

    /**
     *
     * Get swagger definition URL
     * @static
     * @returns
     * @memberof Utils
     */
    static getSwaggerURL() {
        return 'https://' + Utils.getCurrentEnvironment().host + Utils.CONST.SWAGGER_YAML;
    }

    /**
     * Generate UUID V4 Source https://stackoverflow.com/questions/105034/how-to-create-a-guid-uuid
     */
    static generateUUID() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
            // Disable the no bitwise rule as this is a `very rare` usage of bitwise logic operators
            // eslint-disable-next-line no-bitwise
            const r = Math.random() * 16 | 0; const
                // eslint-disable-next-line no-bitwise
                v = c === 'x' ? r : (r & (0x3 | 0x8));
            return v.toString(16);
        });
    }

    /**
     *
     * Get service catalog swagger definition URL
     * @static
     * @returns
     * @memberof Utils
     */
    static getServiceCatalogSwaggerURL() {
        return 'https://' + Utils.getCurrentEnvironment().host + Utils.CONST.SERVICE_CATALOG_SWAGGER_YAML;
        // return Utils.CONST.SERVICE_CATALOG_SWAGGER_YAML;
    }

    /**
     * Return the time difference between the current time and the given time in the Date object in seconds
     * @param targetTime {Date|Integer} Date object which needs to be compared with current time
     * @returns {Integer} Time difference in seconds
     */
    static timeDifference(targetTime) {
        const currentTime = Date.now();
        return Math.floor((targetTime - currentTime) / 1000);
    }

    /**
     * Set Auto login info in local-storage according to environment
     * @param {Array} environments - Array of Environment objects
     * @param {Array} configs - Array of Auth Config objects
     */
    static setMultiEnvironmentOverviewEnabledInfo(environments, configs) {
        const autoLoginInfo = {};
        if (!Array.isArray(environments) || !Array.isArray(configs)) {
            console.error('Error while storing auto login configs in local-storage');
        }

        for (let i = 0; i < environments.length; i++) {
            autoLoginInfo[environments[i].label] = configs[i].is_multi_environment_overview_enabled.value;
        }

        const data = JSON.stringify(autoLoginInfo);
        localStorage.setItem(Utils.CONST.MULTI_ENVIRONMENT_OVERVIEW_ENABLED, data);
    }

    /**
     * Get whether Multi-Environment Overview feature is enabled in the specified environment or not
     * @param environmentName - Name of the environment
     * @return {Boolean|undefined} auto login enabled
     */
    static isMultiEnvironmentOverviewEnabled(environmentName = Utils.getCurrentEnvironment().label) {
        const autoLoginInfo = JSON.parse(localStorage.getItem(Utils.CONST.MULTI_ENVIRONMENT_OVERVIEW_ENABLED));
        return autoLoginInfo[environmentName];
    }

    /**
     * Get an environment object with default values.
     * @returns {Object} environment: {label: string, host: string, loginTokenPath: string}
     * @private
     */
    static getDefaultEnvironment() {
        return {
            label: 'Default',
            host: window.location.host,
            loginTokenPath: '/login/token',
            refreshTokenPath: '/services/refresh/refresh.jag',
        };
    }

    /**
     * Recursively freeze and object properties.
     * Source: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/freeze
     * @static
     * @param {Object} object Object that needs to be frozen
     * @returns {Object} Completely freeze an object
     * @memberof Utils
     */
    static deepFreeze(object) {
        if (Object.isFrozen(object)) {
            return object;
        }
        const trickObject = object; // This is to satisfy the es-lint rule
        // Retrieve the property names defined on object
        const propNames = Object.getOwnPropertyNames(object);

        // Freeze properties before freezing self
        for (const name of propNames) {
            const value = object[name];
            trickObject[name] = value && typeof value === 'object' ? Utils.deepFreeze(value) : value;
        }

        return Object.freeze(object);
    }

    /**
     *
     *
     * @static
     * @param {*} hex Color value in hex
     * @param {*} alpha alpha channel intensity (0.0 to 1.0)
     * @returns {String} CSS friendly RGBA string
     * @memberof Utils
     */
    static hexToRGBA(hex, alpha) {
        const r = parseInt(hex.slice(1, 3), 16);
        const g = parseInt(hex.slice(3, 5), 16);
        const b = parseInt(hex.slice(5, 7), 16);

        if (alpha) {
            return 'rgba(' + r + ', ' + g + ', ' + b + ', ' + alpha + ')';
        } else {
            return 'rgb(' + r + ', ' + g + ', ' + b + ')';
        }
    }

    /**
     * return R,G & B color components
     * @param {Strinng} hex HEX color code string i:e `#AF2386`
     * @returns {Object} colors
     */
    static hexToRGBHash(hex) {
        // Expand shorthand form (e.g. "03F") to full form (e.g. "0033FF")
        const shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
        const commonHex = hex.replace(shorthandRegex, (m, r, g, b) => {
            return r + r + g + g + b + b;
        });

        const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(commonHex);
        return result
            ? {
                r: parseInt(result[1], 16),
                g: parseInt(result[2], 16),
                b: parseInt(result[3], 16),
            }
            : null;
    }

    /**
     * Return HEX hashed color code given the R,G & B color components
     * @param {Integer} r Red
     * @param {Integer} g Green
     * @param {Integer} b Blue
     * @returns {String} Hex code
     */
    static rgbToHex(r, g, b) {
        return '#' + [r, g, b].map((x) => {
            const hex = x.toString(16);
            return hex.length === 1 ? '0' + hex : hex;
        }).join('');
    }

    /**
     * Force file download in browser
     *
     * @static
     * @param {*} response
     * @memberof Utils
     */
    static forceDownload(response) {
        let fileName = '';
        const contentDisposition = response.headers['content-disposition'];

        if (contentDisposition && contentDisposition.indexOf('attachment') !== -1) {
            const fileNameReg = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            const matches = fileNameReg.exec(contentDisposition);
            if (matches != null && matches[1]) fileName = matches[1].replace(/['"]/g, '');
        }
        const contentType = response.headers['content-type'];
        const blob = new Blob([response.data], {
            type: contentType,
        });
        if (typeof window.navigator.msSaveBlob !== 'undefined') {
            window.navigator.msSaveBlob(blob, fileName);
        } else {
            const URL = window.URL || window.webkitURL;
            const downloadUrl = URL.createObjectURL(blob);

            if (fileName) {
                const aTag = document.createElement('a');
                if (typeof aTag.download === 'undefined') {
                    window.location = downloadUrl;
                } else {
                    aTag.href = downloadUrl;
                    aTag.download = fileName;
                    document.body.appendChild(aTag);
                    aTag.click();
                }
            } else {
                window.location = downloadUrl;
            }

            setTimeout(() => {
                URL.revokeObjectURL(downloadUrl);
            }, 100);
        }
    }

    /**
     * Force service definition download in browser
     *
     * @static
     * @param {*} response
     * @memberof Utils
     */
    static downloadServiceDefinition(response) {
        const fileName = 'service-definition';
        const contentType = 'application/yaml';
        const blob = new Blob([JSON.stringify(response)], {
            type: contentType,
        });
        if (typeof window.navigator.msSaveBlob !== 'undefined') {
            window.navigator.msSaveBlob(blob, fileName);
        } else {
            const URL = window.URL || window.webkitURL;
            const downloadUrl = URL.createObjectURL(blob);

            if (fileName) {
                const aTag = document.createElement('a');
                if (typeof aTag.download === 'undefined') {
                    window.location = downloadUrl;
                } else {
                    aTag.href = downloadUrl;
                    aTag.download = fileName;
                    document.body.appendChild(aTag);
                    aTag.click();
                }
            } else {
                window.location = downloadUrl;
            }

            setTimeout(() => {
                URL.revokeObjectURL(downloadUrl);
            }, 100);
        }
    }

    /**
     * Simply split the token by dots `.` and parse it as a JSON object
     * @param {String} token raw token string
     * @returns {JSON} decoded JWT token in JSON format
     */
    static decodeJWT(token) {
        const [header, payload, signature] = token.split('.');
        try {
            return {
                header: JSON.parse(atob(header)),
                payload: JSON.parse(atob(payload)),
                signature,
            };
        } catch (e) {
            return null;
        }
    }

    /**
     * Get the super tenent user without domain
     * @param {String} userName - Name of the user
     * @returns {String} - user name without domain
     */
    static getUserNameWithoutDomain(userName) {
        let username = userName;
        const count = (username.match(/@/g) || []).length;
        if (username.endsWith('@carbon.super') && count <= 1) {
            username = username.replace('@carbon.super', '');
        }

        return username;
    }

    static getAllEnvironmentDeployments(environments, allEnvRevision) {
        // allEnvDeployments represents all deployments of the API with mapping
        // environment -> {revision deployed to env, vhost deployed to env with revision}
        const allEnvDeployments = [];
        environments.forEach((env) => {
            const revision = allEnvRevision && allEnvRevision.find(
                (r) => r.deploymentInfo.some((e) => e.name === env.name),
            );
            const envDetails = revision && revision.deploymentInfo.find((e) => e.name === env.name);
            const disPlayDevportal = envDetails && envDetails.displayOnDevportal;
            let vhost = envDetails && env.vhosts && env.vhosts.find((e) => e.host === envDetails.vhost);
            if (!vhost) { // if vhost is deleted after deploying the revision, there is no matching vhost
                vhost = { ...CONSTS.DEFAULT_VHOST, host: envDetails && envDetails.vhost };
            }
            allEnvDeployments[env.name] = { revision, vhost, disPlayDevportal };
        });
        return allEnvDeployments;
    }
}

Utils.CONST = {
    LOCAL_STORAGE_ENVIRONMENT: 'environment_publisher',
    // TODO: fix/remove below wrong paths
    MULTI_ENVIRONMENT_OVERVIEW_ENABLED: 'multi_env_overview',

    LOGOUT_CALLBACK: '/services/auth/callback/logout',
    INTROSPECT: '/services/auth/introspect',
    SERVICE_CATALOG_SWAGGER_YAML: '/api/am/service-catalog/v0/oas.yaml',
    SWAGGER_YAML: '/api/am/publisher/v2/swagger.yaml',
    PROTOCOL: 'https://',
    API_CLIENT: 'apiClient',
    SERVICE_CATALOG_CLIENT: 'serviceCatalogClient',
};

/**
 * Current environment
 * @type {object} environment object
 * @private
 */
Utils.environment = undefined;
export default Utils;
