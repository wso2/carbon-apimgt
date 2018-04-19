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
import Utils from './Utils';
import User from './User';
import APIClient from './APIClient';
import APIClientFactory from './APIClientFactory';

/**
 * Class managing authentication
 */
class AuthManager {
    constructor() {
        this.isLogged = false;
        this.username = null;
    }

    /**
     * Refresh the access token and set new access token to the intercepted request
     * @param {Request} request
     * @param {Object} environment
     */
    static refreshTokenOnExpire(request, environment = Utils.getCurrentEnvironment()) {
        const refreshPeriod = 60;
        const user = AuthManager.getUser(environment.label);
        const timeToExpire = Utils.timeDifference(user.getExpiryTime());
        if (timeToExpire >= refreshPeriod) {
            return request;
        }
        if (user.getExpiryTime() < 0) {
            return request;
        }
        const loginPromise = AuthManager.refresh(environment);
        loginPromise.then((response) => {
            const loggedInUser = AuthManager.loginUserMapper(response, environment.label);
            AuthManager.setUser(loggedInUser, environment.label);
        });
        loginPromise.catch((error) => {
            const errorData = JSON.parse(error.responseText);
            const message = 'Error while refreshing token You will be redirect to the login page ...';
            console.error(errorData);
            console.error(message);
        });
        return loginPromise;
    }

    /**
     * Static method to handle unauthorized user action error catch, It will look for response status
     *  code and skip !401 errors
     * @param {object} errorResponse
     */
    static unauthorizedErrorHandler(errorResponse) {
        if (errorResponse.status !== 401) {
            /* Skip unrelated response code to handle in unauthorizedErrorHandler */
            throw errorResponse;
            /* re throwing the error since we don't handle it here and propagate to downstream error
             handlers in catch chain */
        }
        const message = 'The session has expired. <br/> You will be redirect to the login page ...';

        throw new Error(errorResponse + message);
    }

    /**
     * An user object is return in present of user logged in user info in browser local storage, at the same
     *  time checks for partialToken in the cookie as well.
     * This may give a partial indication(passive check not actually check the token validity via an API) of
     *  whether the user has logged in or not, The actual API call may get denied
     * if the cookie stored access token is invalid/expired
     * @param {string} environmentName - label of the environment, the user to be retrieved from
     * @returns {User | null} Is any user has logged in or not
     */
    static getUser(environmentName = Utils.getCurrentEnvironment().label) {
        const userData = localStorage.getItem(`${User.CONST.LOCAL_STORAGE_USER}_${environmentName}`);
        const partialToken = Utils.getCookie(User.CONST.WSO2_AM_TOKEN_1, environmentName);
        if (!(userData && partialToken)) {
            return null;
        }

        return User.fromJson(JSON.parse(userData), environmentName);
    }

    /**
     * Persist an user in browser local storage and in-memory, Since only one use can be
     * logged into the application at a time,
     * This method will override any previously persist user data.
     * @param {User} user - An instance of the {User} class
     * @param {string} environmentName - label of the environment to be set the user
     */
    static setUser(user, environmentName = Utils.getCurrentEnvironment().label) {
        if (!(user instanceof User)) {
            throw new Error('Invalid user object');
        }
        if (user) {
            localStorage.setItem(`${User.CONST.LOCAL_STORAGE_USER}_${environmentName}`, JSON.stringify(user.toJson()));
        }
    }

    /**
     *
     * @param {String} environmentName - Name of the environment the user to be removed
     */
    static dismissUser(environmentName) {
        localStorage.removeItem(`${User.CONST.LOCAL_STORAGE_USER}_${environmentName}`);
        User.destroyInMemoryUser(environmentName);
    }

    static hasScopes(resourcePath, resourceMethod) {
        const userscopes = AuthManager.getUser().scopes;
        const validScope = APIClient.getScopeForResource(resourcePath, resourceMethod);
        return validScope.then((scope) => {
            return userscopes.includes(scope);
        });
    }

    /**
     * By given username and password Authenticate the user, Since this REST API has no swagger definition,
     * Can't use swaggerjs to generate client.Hence using Axios to make AJAX calls
     * @param {String} username - Username of the user
     * @param {String} password - Plain text password
     * @param {Object} environment - environment object
     * @returns {AxiosPromise} - Promise object with the login request made
     */
    authenticateUser(username, password, environment) {
        const headers = {
            Accept: 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded',
        };
        const data = {
            username,
            password,
            grant_type: 'password',
            validity_period: -1,
            scopes: AuthManager.CONST.USER_SCOPES,
            remember_me: true, // By default always remember user session
        };
        // Set the environment that user tried to authenticate
        const previousEnvironment = Utils.getCurrentEnvironment();
        Utils.setEnvironment(environment);

        const promisedResponse = this.postAuthenticationRequest(headers, data, environment);
        promisedResponse.catch(() => {
            Utils.setEnvironment(previousEnvironment);
        });

        return promisedResponse;
    }

    /**
     * Return an user object given the login request response object
     * @param {Object} response - Response object received from either Axios or Fetch libraries
     * @param {String} environmentName - Name of the environment
     * @returns {User} Instance of an user who is currently logged in (for the selected environment)
     */
    static loginUserMapper(response, environmentName) {
        const { data } = response;
        const { validityPeriod } = data; // In seconds
        const WSO2_AM_TOKEN_1 = data.partialToken;
        const user = new User(environmentName, data.authUser);
        user.setPartialToken(WSO2_AM_TOKEN_1, validityPeriod, Utils.CONST.CONTEXT_PATH);
        user.setExpiryTime(validityPeriod);
        user.scopes = data.scopes.split(' ');
        return user;
    }

    /**
     * Revoke the issued OAuth access token for currently logged in user and clear both cookie and local-storage data.
     * @param {String} environmentName - Name of the environment to be logged out. Default current environment.
     * @returns {AxiosPromise}
     */
    logout(environmentName = Utils.getCurrentEnvironment().label) {
        const authHeader = 'Bearer ' + AuthManager.getUser(environmentName).getPartialToken();
        // TODO Will have to change the logout end point url to contain the app context(i.e. publisher/store, etc.)
        const url = Utils.getAppLogoutURL();
        const headers = {
            Accept: 'application/json',
            'Content-Type': 'application/json',
            Authorization: authHeader,
        };
        const promisedLogout = axios.post(url, null, { headers });
        promisedLogout
            .then(() => {
                Utils.delete_cookie(User.CONST.WSO2_AM_TOKEN_1, Utils.CONST.CONTEXT_PATH, environmentName);
                AuthManager.dismissUser(environmentName);
                APIClientFactory.getInstance().destroyAPIClient(environmentName);
                // Single client should be re initialize after log out
                console.log(`Successfully logout from environment: ${environmentName}`);
            })
            .catch((error) => {
                console.error(`Failed to logout from environment: ${environmentName}`, error);
            });

        return promisedLogout;
    }

    /**
     * Logout current user from all specified environments
     * @param {array} environments - Array of environments
     * @returns {Promise} Promised Logout object of current environment
     */
    logoutFromEnvironments(environments) {
        const currentEnvironmentName = Utils.getCurrentEnvironment().label;
        const currentUser = AuthManager.getUser(currentEnvironmentName).name;

        environments.forEach((environment) => {
            const user = AuthManager.getUser(environment.label);
            if (user && currentUser === user.name && currentEnvironmentName !== environment.label) {
                this.logout(environment.label);
            }
        });

        return this.logout(currentEnvironmentName);
    }

    setupAutoRefresh(environmentName) {
        const user = AuthManager.getUser(environmentName);
        const bufferTime = 1000 * 10; // Give 10 sec buffer time before token expire,
        // considering the network delays and ect.
        const triggerIn = Utils.timeDifference(user.getExpiryTime() - bufferTime);
        if (user) {
            setTimeout(AuthManager.refreshTokenOnExpire, triggerIn * 1000);
        } else {
            throw new Error('No user exist for current session! Needs to login before setting up refresh');
        }
    }

    /**
     * Call Token API with refresh token grant type
     * @param {Object} environment - Name of the environment
     * @return {AxiosPromise}
     */
    static refresh(environment) {
        const authHeader = 'Bearer ' + AuthManager.getUser(environment.label).getRefreshPartialToken();
        const params = {
            grant_type: 'refresh_token',
            validity_period: -1,
            scopes: AuthManager.CONST.USER_SCOPES,
        };
        const referrer = document.referrer.indexOf('https') !== -1 ? document.referrer : null;
        const url = environment.loginTokenPath + Utils.CONST.CONTEXT_PATH;
        /* TODO: Fetch this from configs ~tmkb */
        const headers = {
            Authorization: authHeader,
            Accept: 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Alt-Referer': referrer,
        };
        return axios.post(url, qs.stringify(params), { headers });
    }

    /**
     * Login to environments specified using the JWT token
     * @param {string} idToken - JWT token
     * @param {array} environments - Array of environments
     * @param {array} configs - Array of configurations of each environments to validate the feature is enabled
     * @returns {Array} Array of Promise objects of each request
     */
    handleAutoLoginEnvironments(idToken, environments, configs) {
        const promiseArray = [];

        const headers = {
            Accept: 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded',
        };
        const data = {
            assertion: idToken,
            validity_period: -1,
            scopes: AuthManager.CONST.USER_SCOPES,
        };
        const currentEnvName = Utils.getCurrentEnvironment().label;

        environments.forEach((environment, environmentID) => {
            const isAutoLoginEnabled = configs[environmentID].is_multi_environment_overview_enabled;
            const isAlreadyLoggedIn = AuthManager.getUser(environment.label); // Already logged in by any user
            const isCurrentEnvironment = environment.label === currentEnvName;

            if (isAutoLoginEnabled && !isAlreadyLoggedIn && !isCurrentEnvironment) {
                promiseArray.push(this.postAuthenticationRequest(headers, data, environment));
            }
        });
        return promiseArray;
    }

    /**
     * Send the POST request to the using Axios
     * @param {Object} headers - Header object
     * @param {Object} data - Data object with credentials
     * @param {Object} environment - environment object
     * @returns {Promise} Axios Promise object with the login request made
     */
    postAuthenticationRequest(headers, data, environment) {
        const promisedResponse = axios(Utils.getLoginTokenPath(environment), {
            method: 'POST',
            data: qs.stringify(data),
            headers,
            withCredentials: true,
        });

        promisedResponse
            .then((response) => {
                const user = AuthManager.loginUserMapper(response, environment.label);
                AuthManager.setUser(user, environment.label);
                this.setupAutoRefresh(environment.label);
                console.log(`Authentication Success in '${environment.label}' environment.`);
            })
            .catch((error) => {
                console.error(`Authentication Error in '${environment.label}' environment :\n`, error);
            });

        return promisedResponse;
    }
}

// TODO: derive this from swagger definitions ~tmkb
AuthManager.CONST = {
    USER_SCOPES:
        'apim:api_view apim:api_create apim:api_publish apim:tier_view apim:tier_manage ' +
        'apim:subscription_view apim:subscription_block apim:subscribe apim:external_services_discover',
};
export default AuthManager;
