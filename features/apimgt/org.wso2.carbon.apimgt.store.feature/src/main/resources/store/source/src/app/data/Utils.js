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

import AuthManager from "./AuthManager";
import Axios from "axios";

/**
 * Utility class for Store application
 */
class Utils {

    /**
     * Get JavaScript accessible cookies saved in browser, by giving the cooke name.
     * @param {String} cookieName : Name of the cookie which need to be retrived
     * @param {String} environmentName : label of the environment of the cookie
     * @returns {String|null} : If found a cookie with given name , return its value,Else null value is returned
     */
    static getCookie(cookieName, environmentName) {
        environmentName = environmentName || Utils.getEnvironment().label;

        let pairs = document.cookie.split(";");
        let cookie = null;
        for (let pair of pairs) {
            pair = pair.split("=");
            let cookie_name = pair[0].trim();
            let value = encodeURIComponent(pair[1]);
            if (cookie_name === `${cookieName}_${environmentName}`) {
                cookie = value;
                break;
            }
        }
        return cookie;
    }

    /**
     * Delete a browser cookie given its name
     * @param {String} name : Name of the cookie which need to be deleted
     * @param {String} path : Path of the cookie which need to be deleted
     * @param {String} environmentName: label of the environment of the cookie
     */
    static delete_cookie(name, path, environmentName) {
        environmentName = environmentName || Utils.getEnvironment().label;
        document.cookie = `${name}_${environmentName}=; path=${path}; expires=Thu, 01 Jan 1970 00:00:01 GMT`;
    }

    /**
     * Set a cookie with given name and value assigned to it. Cookies can be only set to the same origin,
     * which the script is running
     * @param {String} name : Name of the cookie which need to be set
     * @param {String} value : Value of the cookie, expect it to be URLEncoded
     * @param {number} validityPeriod :  (Optional) Validity period of the cookie in seconds
     * @param {String} path : Path which needs to set the given cookie
     * @param {boolean} secured : secured parameter is set
     */
    static setCookie(name, value, validityPeriod, path = "/", environmentName, secured = true) {
        environmentName = environmentName || Utils.getEnvironment().label;
        let expiresDirective = "";
        const securedDirective = secured ? "; Secure" : "";
        if (validityPeriod) {
            const date = new Date();
            date.setTime(date.getTime() + validityPeriod * 1000);
            expiresDirective = "; expires=" + date.toUTCString();
        }

        document.cookie = `${name}_${environmentName}=${value}; path=${path}${expiresDirective}${securedDirective}`;
    }

    /**
     * Given an object returns whether the object is empty or not
     * @param {Object} object : Any JSON object
     * @returns {boolean}
     */
    static isEmptyObject(object) {
        return Object.keys(object).length === 0 && object.constructor === Object
    }

    /**
     * Get the current environment from local-storage
     * @returns {Object} environment: {label, host, loginTokenPath}
     */
    static getEnvironment() {
        if (Utils._environment) {
            return Utils._environment;
        }

        let environmentData = localStorage.getItem(Utils.CONST.LOCALSTORAGE_ENVIRONMENT);
        if (!environmentData) {
            return Utils._getDefaultEnvironment();
        }

        return JSON.parse(environmentData);
    }

    /**
     * Get current environment's index from the given environment array
     * @param {Array} environments
     * @param {string} name: name of the environment [default]: current environment name
     * @returns {number}
     */
    static getEnvironmentID(environments, name = Utils.getEnvironment().label) {
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
     * @param {object} environment
     */
    static setEnvironment(environment) {
        if (!environment) {
            environment = Utils._getDefaultEnvironment();
        }

        if (!environment.host) {
            environment.host = window.location.host;
        }
        //Store environment.
        Utils._environment = environment;
        localStorage.setItem(Utils.CONST.LOCALSTORAGE_ENVIRONMENT, JSON.stringify(environment));
    }

    static getPromised_DCRappInfo(environment) {
        return Axios.get(Utils.getDCRappInfoRequestURL(environment));
    }

    static getDCRappInfoRequestURL(environment = Utils.getEnvironment()) {
        return `${Utils.CONST.PROTOCOL}${environment.host}${Utils.CONST.DCR_APP_INFO}${Utils.CONST.CONTEXT_PATH}`;
    }

    static getAppLogoutURL() {
        return Utils.CONST.PROTOCOL + Utils.getEnvironment().host + Utils.CONST.LOGOUT + Utils.CONST.CONTEXT_PATH;
    }

    static getLoginTokenPath(environment = Utils.getEnvironment()) {
        return `${Utils.CONST.PROTOCOL}${environment.host}${Utils.CONST.LOGIN_TOKEN_PATH}${Utils.CONST.CONTEXT_PATH}`;
    }

    static getSwaggerURL() {
        return "https://" + Utils.getEnvironment().host + Utils.CONST.SWAGGER_YAML;
    }

    /**
     * Get an environment object with default values.
     * @returns {Object} environment: {label: string, host: string, loginTokenPath: string}
     * @private
     */
    static _getDefaultEnvironment() {
        return {label: 'Default', host: window.location.host, loginTokenPath: '/login/token'};
    }
}

Utils.CONST = {
    LOCALSTORAGE_ENVIRONMENT: 'environment_store',
    DCR_APP_INFO: '/login/login',
    LOGOUT: '/login/logout',
    LOGIN_TOKEN_PATH: '/login/token',
    SWAGGER_YAML: '/api/am/store/v1.0/apis/swagger.yaml',
    PROTOCOL: 'https://',
    CONTEXT_PATH: '/store'
};

/**
 * Current environment
 * @type {object} environment object
 * @private
 */
Utils._environment = undefined;
export default Utils;