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

/**
 * Utility class for Publisher application
 */
class PublisherUtils {

    /**
     * TODO: Remove this method one the initial phase is done, This is used to continue the API class until the login page is create
     * @returns {promise}
     */
    static autoLogin() {
        let auth = new AuthManager();
        return auth.authenticateUser('admin', 'admin');
    }

    /**
     * Get JavaScript accessible cookies saved in browser, by giving the cooke name.
     * @param {String} name : Name of the cookie which need to be retrived
     * @returns {String|null} : If found a cookie with given name , return its value,Else null value is returned
     */
    static getCookie(name) {
        let pairs = document.cookie.split(";");
        let cookie = null;
        for (let pair of pairs) {
            pair = pair.split("=");
            let cookie_name = pair[0].trim();
            let value = encodeURIComponent(pair[1]);
            if (cookie_name === name) {
                cookie = value;
                break;
            }
        }
        return cookie;
    }

    /**
     * Delete a browser cookie given its name
     * @param {String} name : Name of the cookie which need to be deleted
     */
    static delete_cookie(name) {
        document.cookie = name + '=; Path=' + "/" + '; Expires=Thu, 01 Jan 1970 00:00:01 GMT;';
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
    static setCookie(name, value, validityPeriod, path = "/", secured = true) {
        let expires = "";
        const securedDirective = secured ? "; Secure" : "";
        if (validityPeriod) {
            const date = new Date();
            date.setTime(date.getTime() + validityPeriod * 1000);
            expires = "; expires=" + date.toUTCString();
        }
        document.cookie = name + "=" + value + expires + "; path=" + path + securedDirective;
    }

    /**
     * Given an object returns whether the object is empty or not
     * @param {Object} object : Any JSON object
     * @returns {boolean}
     */
    static isEmptyObject(object) {
        return Object.keys(object).length === 0 && object.constructor === Object
    }


}

export default PublisherUtils;