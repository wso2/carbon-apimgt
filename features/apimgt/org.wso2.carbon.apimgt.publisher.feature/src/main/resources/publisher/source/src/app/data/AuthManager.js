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
import Configs from './ConfigManager'
import Utils from './utils'
import User from './User'
import SingleClient from './SingleClient'

class AuthManager {
    constructor() {
        this.host = window.location.protocol + "//" + window.location.host;
        this.isLogged = false;
        this.username = null;
        this.userscope = null;
        this.contextPath = "/publisher";
    }

    static refreshTokenOnExpire() {
        let timestampSkew = 100;
        let currentTimestamp = Math.floor(Date.now() / 1000);
        let tokenTimestamp = localStorage.getItem("expiresIn");
        let rememberMe = (localStorage.getItem("rememberMe") === 'true');
        if (rememberMe && (tokenTimestamp - currentTimestamp < timestampSkew)) {
            let bearerToken = "Bearer " + Utils.getCookie("WSO2_AM_REFRESH_TOKEN_1");
            let loginPromise = authManager.refresh(bearerToken);
            loginPromise.then(function (data, status, xhr) {
                authManager.setUser(true);
                let expiresIn = data.validityPeriod + Math.floor(Date.now() / 1000);
                window.localStorage.setItem("expiresIn", expiresIn);
            });
            loginPromise.error(
                function (error) {
                    let error_data = JSON.parse(error.responseText);
                    let message = "Error while refreshing token" + "<br/> You will be redirect to the login page ...";
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
                            afterClose: function () {
                                window.location = loginPageUri;
                            },
                        }
                    });

                }
            );
        }
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
                    afterClose: function () {
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
     *
     * @returns {User | null} Is any user has logged in or not
     */
    static getUser() {
        const userData = localStorage.getItem(User.CONST.LOCALSTORAGE_USER);
        const partialToken = Utils.getCookie(User.CONST.WSO2_AM_TOKEN_1);
        if (!(userData && partialToken)) {
            return null;
        }
        return User.fromJson(JSON.parse(userData));
    }

    /**
     * Persist an user in browser local storage, Since only one use can be logged into the application at a time,
     * This method will override any previously persist user data.
     * @param {User} user : An instance of the {User} class
     */
    static setUser(user) {
        if (!user instanceof User) {
            throw new Error("Invalid user object");
        }
        localStorage.setItem(User.CONST.LOCALSTORAGE_USER, JSON.stringify(user.toJson()));
        /* TODO: IMHO it's better to get this key (`wso2_user`) from configs */
    }

    /**
     * Get login token path from given environment or get default login token path
     * @param {Object} environment: environment object
     * @returns {String} loginTokenPath: login token path of the given environment
     */
    getTokenEndpoint(environment) {
        let loginTokenPath;
        if (environment) {
            //The default value of `host` in back-end java code is an empty string.
            let host = (environment.host) ? environment.host : this.host;
            loginTokenPath = host + environment.loginTokenPath + this.contextPath;
        } else {
            //If no environment return default loginTokenPath
            loginTokenPath = this.host + "/login/token" + this.contextPath;
        }
        return loginTokenPath;
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
            'Authorization': 'Basic deidwe',
            'Accept': 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded'
        };
        const data = {
            username: username,
            password: password,
            grant_type: 'password',
            validity_period: 3600,
            scopes: 'apim:api_view apim:api_create apim:api_publish apim:tier_view apim:tier_manage apim:subscription_view apim:subscription_block apim:subscribe'
        };
        let promised_response = axios.post(this.getTokenEndpoint(environment), qs.stringify(data), {headers: headers});
        promised_response.then(response => {
            const validityPeriod = response.data.validityPeriod; // In seconds
            const WSO2_AM_TOKEN_1 = response.data.partialToken;
            const user = new User(response.data.authUser, response.data.idToken);
            user.setPartialToken(WSO2_AM_TOKEN_1, validityPeriod, this.contextPath);
            user.scopes = response.data.scopes.split(" ");
            AuthManager.setUser(user);
        });
        return promised_response;
    }

    /**
     * Revoke the issued OAuth access token for currently logged in user and clear both cookie and localstorage data.
     */
    logout() {
        let authHeader = "Bearer " + AuthManager.getUser().getPartialToken();
        //TODO Will have to change the logout end point url to contain the app context(i.e. publisher/store, etc.)
        let url = this.host + "/login/logout" + this.contextPath;
        let headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': authHeader
        };
        const promisedLogout = axios.post(url, null, {headers: headers});
        return promisedLogout.then(response => {
            Utils.delete_cookie("WSO2_AM_TOKEN_1");
            localStorage.removeItem("wso2_user");
        });
    }

    refresh(authzHeader) {
        let params = {
            grant_type: 'refresh_token',
            validity_period: '3600',
            scopes: 'apim:api_view apim:api_create apim:api_publish apim:tier_view apim:tier_manage' +
            ' apim:subscription_view apim:subscription_block apim:subscribe'
        };
        let referrer = (document.referrer.indexOf("https") !== -1) ? document.referrer : null;
        let url = this.contextPath + '/auth/apis/login/token';
        /* TODO: Fetch this from configs ~tmkb*/
        let headers = {
            'Authorization': authzHeader,
            'Accept': 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Alt-Referer': referrer
        };
        return axios.post(url, qs.stringify(params), {headers: headers});
    }

    static hasScopes(resourcePath, resourceMethod) {
        let userscopes = this.getUser().scopes;
        let validScope = SingleClient.getScopeForResource(resourcePath, resourceMethod);
        return validScope.then(scope => {
            return userscopes.includes(scope)
        });
    }

}

export default AuthManager;
