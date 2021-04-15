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

import qs from 'qs';
import CONSTS from 'AppData/Constants';
import Configurations from 'Config';
import Utils from './Utils';
import User from './User';

/**
 * Class managing authentication
 */
class AuthManager {
    constructor() {
        this.isLogged = false;
        this.username = null;
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
     * An user object is return in present of logged in user info in browser local storage, at the same
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
        const refreshToken = Utils.getCookie(User.CONST.WSO2_AM_REFRESH_TOKEN_1, environmentName);
        if (!(userData && (partialToken || refreshToken))) {
            return null;
        }
        return User.fromJson(JSON.parse(userData), environmentName);
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
            return new Promise((resolve, reject) => reject(new Error(CONSTS.errorCodes.NO_TOKEN_FOUND)));
        }
        const introspectUrl = Configurations.app.context + Utils.CONST.INTROSPECT;
        const promisedResponse = fetch(introspectUrl, { credentials: 'same-origin' });
        return promisedResponse
            .then((response) => response.json())
            .then((data) => {
                let user = null;
                if (data.active) {
                    const currentEnv = Utils.getCurrentEnvironment();
                    user = new User(currentEnv.label, data.username);
                    const scopes = data.scope.split(' ');
                    if (this.hasBasicLoginPermission(scopes)) {
                        user.scopes = scopes;
                        AuthManager.setUser(user, currentEnv.label);
                    } else {
                        console.warn('The user with ' + partialToken + " doesn't enough have permission!");
                        throw new Error(CONSTS.errorCodes.INSUFFICIENT_PREVILEGES);
                    }
                } else {
                    console.warn('The user with ' + partialToken + ' is not active!');
                    throw new Error(CONSTS.errorCodes.INVALID_TOKEN);
                }
                return user;
            });
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
     * Clear all user records from the browser (opposite of `getUser`).
     * partial token, Local storage user object etc
     * consequent `getUser` user will fallback to `getUserFromToken`.
     * @memberof User
     * @returns {void}
     */
    static discardUser() {
        // Since we don't have multi environments currentEnv will always get `default`
        const currentEnv = Utils.getCurrentEnvironment().label;
        localStorage.removeItem(User.CONST.USER_EXPIRY_TIME);
        localStorage.removeItem(`${User.CONST.LOCAL_STORAGE_USER}_${currentEnv}`);
        for (const name of Object.values(User.PROPERTIES)) {
            localStorage.removeItem(`${User.CONST.PROPERTY_PREFIX}${name}`);
        }
        Utils.getCookie(User.CONST.WSO2_AM_TOKEN_1, currentEnv);
        Utils.getCookie(User.CONST.WSO2_AM_REFRESH_TOKEN_1, currentEnv);
    }

    static isNotCreator() {
        return !AuthManager.getUser().scopes.includes('apim:api_create');
    }

    /**
     *
     * Check whether the current user has Internal/publisher role or not
     * @static
     * @returns {Boolean} isNotPublisher
     * @memberof AuthManager
     */
    static isNotPublisher() {
        if (AuthManager.getUser() === null) {
            return false;
        } else {
            return !AuthManager.getUser().scopes.includes('apim:api_publish');
            // TODO: make this scope name configurable
        }
    }

    /**
     *
     * @param {*} scopesAllowedToEdit
     * @param {*} api
     */
    static isRestricted(scopesAllowedToEdit, api = {}) {
        // determines whether the apiType is API PRODUCT and user has publisher role, then allow access.
        if (api.apiType === 'APIPRODUCT') {
            if (AuthManager.getUser().scopes.includes('apim:api_publish')) {
                return false;
            } else {
                return true;
            }
        }

        // determines whether the user is a publisher or creator (based on what is passed from the element)
        // if (scopesAllowedToEdit.filter(element => AuthManager.getUser().scopes.includes(element)).length > 0) {
        if (scopesAllowedToEdit.find((element) => AuthManager.getUser().scopes.includes(element))) {
            // if the user has publisher role, no need to consider the api LifeCycleStatus
            if ((Object.keys(api).length === 0 && api.constructor === Object)
            || AuthManager.getUser().scopes.includes('apim:api_publish')) {
                return false;
            } else if (
                // if the user has creator role, but not the publisher role
                api.lifeCycleStatus === 'CREATED'
                || api.lifeCycleStatus === 'PROTOTYPED'
            ) {
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    static hasBasicLoginPermission(scopes) {
        return scopes.includes('apim:api_view');
    }

    /**
     * Return an user object given the login request response object
     * @param {Object} response - Response object received from either Axios or Fetch libraries
     * @param {String} environmentName - Name of the environment
     * @returns {User} Instance of an user who is currently logged in (for the selected environment)
     */
    static loginUserMapper(response, environmentName) {
        const { data } = response;
        const { AM_ACC_TOKEN_DEFAULT_P1, expires_in: expiresIn } = data;
        const user = new User(environmentName, data.authUser);
        user.setPartialToken(AM_ACC_TOKEN_DEFAULT_P1, expiresIn, Configurations.app.context);
        user.setExpiryTime(expiresIn);
        user.scopes = data.scopes.split(' ');
        return user;
    }

    /**
     * Call Token API with refresh token grant type
     * @param {Object} environment - Name of the environment
     * @return {Promise}
     */
    static refresh(environment) {
        const params = {
            refresh_token: AuthManager.getUser(environment.label).getRefreshPartialToken(),
            validity_period: -1,
            scopes: AuthManager.CONST.USER_SCOPES,
        };
        const referrer = document.referrer.indexOf('https') !== -1 ? document.referrer : null;
        const url = Configurations.app.context + environment.refreshTokenPath;
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
}

// TODO: derive this from swagger definitions ~tmkb
AuthManager.CONST = {
    USER_SCOPES:
        'apim:api_view apim:api_create apim:api_publish apim:tier_view apim:tier_manage '
        + 'apim:subscription_view apim:subscription_block apim:subscribe apim:external_services_discover',
};
const { isRestricted } = AuthManager;

export { isRestricted };

export default AuthManager;
