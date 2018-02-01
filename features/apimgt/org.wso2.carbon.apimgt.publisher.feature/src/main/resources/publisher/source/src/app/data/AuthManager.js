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
"use strict";

import axios from 'axios'
import qs from 'qs'
import Utils from './Utils'
import User from './User'
import APIClient from './APIClient'
import APIClientFactory from "./APIClientFactory";

class AuthManager {
    constructor() {
        this.isLogged = false;
        this.username = null;
    }

    /**
     * Refresh the access token and set new access token to the intercepted request
     * @param {Request} request
     */
    static refreshTokenOnExpire(request) {
        const refreshPeriod = 60;
        const user = AuthManager.getUser();
        let timeToExpire = Utils.timeDifference(user.getExpiryTime());
        if (timeToExpire >= refreshPeriod) {
            return request;
        }
        let loginPromise = AuthManager.refresh();
        loginPromise.then(response => {
            const user = AuthManager.loginUserMapper(response);
            AuthManager.setUser(user);
        });
        loginPromise.catch(
            function(error) {
                let error_data = JSON.parse(error.responseText);
                let message = "Error while refreshing token You will be redirect to the login page ...";
                console.error(message);
            }
        );
    }

    /**
     * Static method to handle unauthorized user action error catch, It will look for response status code and skip !401 errors
     * @param {object} error_response
     */
    static unauthorizedErrorHandler(error_response) {
        if (error_response.status !== 401) { /* Skip unrelated response code to handle in unauthorizedErrorHandler*/
            throw error_response;
            /* re throwing the error since we don't handle it here and propagate to downstream error handlers in catch chain*/
        }
        let message = "The session has expired" + ".<br/> You will be redirect to the login page ...";
        if (typeof noty !== 'undefined') {
            noty({
                text: message,
                type: 'error',
                dismissQueue: true,
                modal: true,
                progressBar: true,
                timeout: 5000,
                layout: 'top',
                theme: 'relax',
                maxVisible: 10,
                callback: {
                    afterClose: function() {
                        window.location = loginPageUri;
                    },
                }
            });
        } else {
            throw error_response;
        }
    }

    /**
     * An user object is return in present of user logged in user info in browser local storage, at the same time checks for partialToken in the cookie as well.
     * This may give a partial indication(passive check not actually check the token validity via an API) of whether the user has logged in or not, The actual API call may get denied
     * if the cookie stored access token is invalid/expired
     * @param {string} environmentName: label of the environment, the user to be retrieved from
     * @returns {User | null} Is any user has logged in or not
     */
    static getUser() {
        const userData = localStorage.getItem(`${User.CONST.LOCALSTORAGE_USER}_${Utils.getEnvironment().label}`);
        const environmentName = "_" + Utils.getEnvironment().label;
        const partialToken = Utils.getCookie(User.CONST.WSO2_AM_TOKEN_1 + environmentName);
        if (!(userData && partialToken)) {
            return null;
        }
        return User.fromJson(JSON.parse(userData));
    }

    /**
     * Persist an user in browser local storage and in-memory, Since only one use can be logged into the application at a time,
     * This method will override any previously persist user data.
     * @param {User} user : An instance of the {User} class
     * @param {string} environmentName: label of the environment to be set the user
     */
    static setUser(user, environmentName) {
        environmentName = environmentName || Utils.getEnvironment().label;
        if (!user instanceof User) {
            throw new Error("Invalid user object");
        }
        if (user) {
            localStorage.setItem(`${User.CONST.LOCALSTORAGE_USER}_${Utils.getEnvironment().label}`,
                JSON.stringify(user.toJson()));
        }
    }

    static hasScopes(resourcePath, resourceMethod) {
        let userscopes = this.getUser().scopes;
        let validScope = APIClient.getScopeForResource(resourcePath, resourceMethod);
        return validScope.then(scope => {
            return userscopes.includes(scope)
        });
    }

    /**
     * By given username and password Authenticate the user, Since this REST API has no swagger definition,
     * Can't use swaggerjs to generate client.Hence using Axios to make AJAX calls
     * @param {String} username : Username of the user
     * @param {String} password : Plain text password
     * @param {Object} environment : environment object
     * @returns {AxiosPromise} : Promise object with the login request made
     */
    authenticateUser(username, password, environment) {
        const headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded'
        };
        const data = {
            username: username,
            password: password,
            grant_type: 'password',
            validity_period: -1,
            scopes: AuthManager.CONST.USER_SCOPES,
            remember_me: true // By default always remember user session
        };
        let promised_response = axios(Utils.getLoginTokenPath(environment), {
            method: "POST",
            data: qs.stringify(data),
            headers: headers,
            withCredentials: true
        });
        //Set the environment that user tried to authenticate
        let previous_environment = Utils.getEnvironment();
        Utils.setEnvironment(environment);

        promised_response.then(response => {
            const user = AuthManager.loginUserMapper(response);
            AuthManager.setUser(user);
            this.setupAutoRefresh();
        }).catch(error => {
            console.error("Authentication Error:\n", error);
            Utils.setEnvironment(previous_environment);
        });
        return promised_response;
    }

    /**
     * Return an user object given the login request response object
     * @param {Object} response Response object received from either Axios or Fetch libraries
     * @returns {User} Instance of an user who is currently logged in (for the selected environment)
     */
    static loginUserMapper(response) {
        let data = response.data;
        const validityPeriod = data.validityPeriod; // In seconds
        const WSO2_AM_TOKEN_1 = data.partialToken;
        const user = new User(Utils.getEnvironment().label, data.authUser, data.idToken);
        user.setPartialToken(WSO2_AM_TOKEN_1, validityPeriod, Utils.CONST.CONTEXT_PATH);
        user.setExpiryTime(validityPeriod);
        user.scopes = data.scopes.split(" ");
        return user;
    }

    /**
     * Revoke the issued OAuth access token for currently logged in user and clear both cookie and localstorage data.
     */
    logout() {
        let authHeader = "Bearer " + AuthManager.getUser().getPartialToken();
        //TODO Will have to change the logout end point url to contain the app context(i.e. publisher/store, etc.)
        let url = Utils.getAppLogoutURL();
        let headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': authHeader
        };
        const promisedLogout = axios.post(url, null, { headers: headers });
        return promisedLogout.then(response => {
            Utils.delete_cookie(User.CONST.WSO2_AM_TOKEN_1, Utils.CONST.CONTEXT_PATH);
            localStorage.removeItem(User.CONST.LOCALSTORAGE_USER);
            new APIClientFactory().destroyAPIClient(Utils.getEnvironment().label); // Single client should be re initialize after log out
        });
    }

    setupAutoRefresh() {
        const user = AuthManager.getUser();
        const bufferTime = 1000 * 10; // Give 10 sec buffer time before token expire, considering the network delays and ect.
        let triggerIn = Utils.timeDifference(user.getExpiryTime() - bufferTime);
        if (user) {
            setTimeout(AuthManager.refreshTokenOnExpire, triggerIn * 1000);
        } else {
            throw new Error("No user exist for current session! Needs to login before setting up refresh");
        }
    }

    static refresh() {
        const authHeader = "Bearer " + AuthManager.getUser().getRefreshPartialToken();
        let params = {
            grant_type: 'refresh_token',
            validity_period: -1,
            scopes: AuthManager.CONST.USER_SCOPES
        };
        let referrer = (document.referrer.indexOf("https") !== -1) ? document.referrer : null;
        let url = '/login/token' + Utils.CONST.CONTEXT_PATH;
        /* TODO: Fetch this from configs ~tmkb*/
        let headers = {
            'Authorization': authHeader,
            'Accept': 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Alt-Referer': referrer
        };
        return axios.post(url, qs.stringify(params), { headers: headers });
    }

}

// TODO: derive this from swagger definitions ~tmkb
AuthManager.CONST = {
    USER_SCOPES: "apim:api_view apim:api_create apim:api_publish apim:tier_view apim:tier_manage " +
        "apim:subscription_view apim:subscription_block apim:subscribe apim:external_services_discover"
};
export default AuthManager;