/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import Axios from "axios";
import Settings from 'Settings';
import AuthManager from "./AuthManager";

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
     * @param {String} name : Name of the cookie which need to be deleted
     * @param {String} path : Path of the cookie which need to be deleted
     * @param {String} environmentName: label of the environment of the cookie
     */
    static deleteCookie(name, path, environmentName) {
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
            return Utils.getDefaultEnvironment();
        }

        return JSON.parse(environmentData);
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
            environment = Utils.getDefaultEnvironment();
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
        return `${Utils.CONST.PROTOCOL}${environment.host}${Utils.CONST.DCR_APP_INFO}${Settings.app.context}`;
    }

    static getAppLogoutURL() {
        return Utils.CONST.PROTOCOL + Utils.getEnvironment().host + Utils.CONST.LOGOUT + Settings.app.context;
    }

    static getLoginTokenPath(environment = Utils.getEnvironment()) {
        return `${Utils.CONST.PROTOCOL}${environment.host}${Utils.CONST.LOGIN_TOKEN_PATH}${Settings.app.context}`;
    }

    static getSignUpTokenPath(environment) {
        return `${Utils.CONST.PROTOCOL}${environment.host}${Utils.CONST.LOGIN_SIGN_UP_PATH}${Settings.app.context}`;
    }

    static getSwaggerURL() {
        if (Settings.app.proxy_context_path) {
            return 'https://'
            + Utils.getCurrentEnvironment().host
            + Settings.app.proxy_context_path
            + Utils.CONST.SWAGGER_YAML;
        } else {
            return 'https://'
            + Utils.getCurrentEnvironment().host
            + Utils.CONST.SWAGGER_YAML;
        }
    }

    static downloadFile = (response) => {
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
    };
    static getBrowserLocal() {
        const language = (navigator.languages && navigator.languages[0]) || navigator.language || navigator.userLanguage;
        const languageWithoutRegionCode = language.toLowerCase().split(/[_-]+/)[0];
        return(languageWithoutRegionCode || language);
    }
}

Utils.CONST = {
    LOCALSTORAGE_ENVIRONMENT: 'environment_store',
    //TODO: fix/remove below wrong paths
    DCR_APP_INFO: '/login/login',
    LOGOUT: '/login/logout',
    LOGIN_TOKEN_PATH: '/login/token',
    LOGIN_SIGN_UP_PATH: '/login/signup',

    LOGOUT_CALLBACK: '/services/auth/callback/logout',
    SWAGGER_YAML: '/api/am/store/v1/swagger.yaml',
    PROTOCOL: 'https://',
};

/**
 * Current environment
 * @type {object} environment object
 * @private
 */
Utils._environment = undefined;
export default Utils;