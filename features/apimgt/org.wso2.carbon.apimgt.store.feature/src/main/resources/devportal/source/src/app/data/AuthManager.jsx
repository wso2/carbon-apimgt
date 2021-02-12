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
import qs from 'qs';
import Settings from 'Settings';
import Utils from './Utils';
import User from './User';
import APIClient from './APIClient';
import APIClientFactory from './APIClientFactory';
import CONSTS from './Constants';


/**
 * Manage the application authentication and authorization requirements.
 *
 * @class AuthManager
 */
class AuthManager {
    constructor() {
        this.isLogged = false;
        this.username = null;
    }

    /**
     * Static method to handle unauthorized user action error catch, It will look for response status code and skip !401 errors
     * @param {object} error_response
     */
    static unauthorizedErrorHandler(error_response) {
        if (error_response.status !== 401) {
            /* Skip unrelated response code to handle in unauthorizedErrorHandler */
            throw error_response;
            /* re throwing the error since we don't handle it here and propagate to downstream error handlers in catch chain */
        }
        const message = 'The session has expired' + '.<br/> You will be redirect to the login page ...';
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
                    afterClose() {
                        window.location = loginPageUri;
                    },
                },
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
    static getUser(environmentName = Utils.getCurrentEnvironment().label) {
        const userData = localStorage.getItem(`${User.CONST.LOCALSTORAGE_USER}_${environmentName}`);
        const partialToken = Utils.getCookie(User.CONST.WSO2_AM_TOKEN_1, environmentName);
        const refreshToken = Utils.getCookie(User.CONST.WSO2_AM_REFRESH_TOKEN_1, environmentName);

        const isLoginCookie = Utils.getCookie('IS_LOGIN', 'DEFAULT');
        if (isLoginCookie) {
            Utils.deleteCookie('IS_LOGIN', Settings.app.context, 'DEFAULT');
            localStorage.removeItem(`${User.CONST.LOCALSTORAGE_USER}_${environmentName}`);
            return null;
        }
        if (!(userData && (partialToken || refreshToken))) {
            return null;
        }

        return User.fromJson(JSON.parse(userData), environmentName);
    }

    static hasBasicLoginPermission(scopes) {
        return scopes.includes('apim:subscribe');
    }

    /**
     * Do token introspection and Get the currently logged in user's details
     * When user authentication happens via redirection flow, This method might get used to
     * retrieve the user information
     * after setting the access token parts in cookies, Because access token parts are stored in /publisher path ,
     * just making an external request in same path will submit both cookies, allowing the service to build the
     * complete access token and do the introspection.
     * Return a promise resolving to user object iff introspect calls return active user else null
     * @static
     * @returns {Promise} fetch response promise resolving to introspect response JSON or null otherwise
     * @memberof AuthManager
     */
    static getUserFromToken() {
        const partialToken = Utils.getCookie(User.CONST.WSO2_AM_TOKEN_1);
        if (!partialToken) {
            return new Promise((resolve, reject) => reject(new Error('No partial token found')));
        }
        const promisedResponse = fetch(
            Settings.app.context + '/services/auth/introspect',
            { credentials: 'same-origin' },
        );
        return promisedResponse
            .then((response) => response.json())
            .then((data) => {
                let user = null;
                let username;
                if (data.active) {
                    const currentEnv = Utils.getCurrentEnvironment();
                    username = data.username;
                    user = new User(currentEnv.label, username);
                    const scopes = data.scope.split(' ');
                    if (this.hasBasicLoginPermission(scopes)) {
                        user.scopes = scopes;
                        AuthManager.setUser(user, currentEnv.label);
                    } else {
                        console.warn('The user with ' + partialToken + ' doesn\'t have enough permission!');
                        throw new Error(CONSTS.errorCodes.INSUFFICIENT_PREVILEGES);
                    }
                } else {
                    console.warn('User with ' + partialToken + ' is not active!');
                    throw new Error(CONSTS.errorCodes.INVALID_TOKEN);
                }
                return user;
            });
    }

    /**
     * Persist an user in browser local storage and in-memory, Since only one use can be logged
     * into the application at a time,This method will override any previously persist user data.
     * @param {User} user : An instance of the {User} class
     * @param {string} environmentName: label of the environment to be set the user
     */
    static setUser(user, environmentName) {
        environmentName = environmentName || Utils.getEnvironment().label;
        if (!(user instanceof User)) {
            throw new Error('Invalid user object');
        }

        if (user) {
            localStorage.setItem(`${User.CONST.LOCALSTORAGE_USER}_${environmentName}`, JSON.stringify(user.toJson()));
        }
    }

    /**
     *
     * Get scope for resources
     * @static
     * @param {String} resourcePath
     * @param {String} resourceMethod
     * @returns Boolean
     * @memberof AuthManager
     */
    static hasScopes(resourcePath, resourceMethod) {
        const user = AuthManager.getUser();
        if (user) {
            const userScopes = user.scopes;
            const validScope = APIClient.getScopeForResource(resourcePath, resourceMethod);
            return validScope.then((scope) => {
                return userScopes.includes(scope);
            });
        }
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
            Authorization: 'Basic deidwe',
            Accept: 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded',
        };
        const data = {
            username,
            password,
            grant_type: 'password',
            validity_period: 3600,
            scopes: 'apim:subscribe apim:signup apim:workflow_approve apim:sub_alert_manage',
        };
        const promised_response = axios(Utils.getLoginTokenPath(environment), {
            method: 'POST',
            data: qs.stringify(data),
            headers,
            withCredentials: true,
        });
        // Set the environment that user tried to authenticate
        const previous_environment = Utils.getEnvironment();
        Utils.setEnvironment(environment);

        promised_response
            .then((response) => {
                const { validityPeriod } = response.data; // In seconds
                const WSO2_AM_TOKEN_1 = response.data.partialToken;
                const user = new User(Utils.getEnvironment().label, response.data.authUser, response.data.idToken);
                user.setPartialToken(WSO2_AM_TOKEN_1, validityPeriod, Settings.app.context);
                user.scopes = response.data.scopes.split(' ');
                AuthManager.setUser(user);
            })
            .catch((error) => {
                console.error('Authentication Error:\n', error);
                Utils.setEnvironment(previous_environment);
            });
        return promised_response;
    }

    /**
     * Revoke the issued OAuth access token for currently logged in user and clear both cookie and localstorage data.
     */
    logout() {
        const authHeader = 'Bearer ' + AuthManager.getUser().getPartialToken();
        // TODO Will have to change the logout end point url to contain the app context(i.e. publisher/devportal, etc.)
        const url = Utils.getAppLogoutURL();
        const headers = {
            Accept: 'application/json',
            'Content-Type': 'application/json',
            Authorization: authHeader,
        };
        const promisedLogout = axios.post(url, null, { headers });
        return promisedLogout.then((response) => {
            Utils.delete_cookie(User.CONST.WSO2_AM_TOKEN_1, Settings.app.context);
            localStorage.removeItem(User.CONST.LOCALSTORAGE_USER);
            new APIClientFactory().destroyAPIClient(Utils.getEnvironment().label); // Single client should be re initialize after log out
        });
    }

    /**
     * Call Token API with refresh token grant type
     * @param {Object} environment - Name of the environment
     * @return {AxiosPromise}
     */
    static refresh(environment) {
        const params = {
            refresh_token: AuthManager.getUser(environment.label).getRefreshPartialToken(),
            validity_period: -1,
            scopes: AuthManager.CONST.USER_SCOPES,
        };
        const referrer = document.referrer.indexOf('https') !== -1 ? document.referrer : null;
        const url = Settings.app.context + environment.refreshTokenPath;
        const headers = {
            Accept: 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Alt-Referer': referrer,
        };
        return fetch(url, {
            method: 'POST',
            body: qs.stringify(params),
            headers,
        });
    }

    /**
     * Register anonymous user by generating token using client_credentials grant type
     * @param {Object} environment : environment object
     * @returns {AxiosPromise} : Promise object with the request made
     */
    registerUser(environment) {
        const headers = {
            Accept: 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded',
        };
        const data = {
            grant_type: 'client_credentials',
            validity_period: 3600,
            scopes: 'apim:self-signup',
        };
        const promised_response = axios(Utils.getSignUpTokenPath(environment), {
            method: 'POST',
            data: qs.stringify(data),
            headers,
            withCredentials: false,
        });

        promised_response
            .then((response) => {
                const { validityPeriod } = response.data;
                const WSO2_AM_TOKEN_1 = response.data.partialToken;
                const user = new User(Utils.getEnvironment().label, response.data.authUser, response.data.idToken);
                user.setPartialToken(WSO2_AM_TOKEN_1, validityPeriod, Settings.app.context);
                user.scopes = response.data.scopes;
                AuthManager.setUser(user);
            })
            .catch((error) => {
                console.error('Authentication Error: ', error);
            });
        return promised_response;
    }
}

// TODO: derive this from swagger definitions ~tmkb
AuthManager.CONST = {
    USER_SCOPES:
        'apim:api_key apim:app_manage apim:app_update apim:dedicated_gateway apim:self-signup '
        + 'apim:store_settings apim:sub_alert_manage apim:sub_manage apim:subscribe openid',
};

export default AuthManager;
