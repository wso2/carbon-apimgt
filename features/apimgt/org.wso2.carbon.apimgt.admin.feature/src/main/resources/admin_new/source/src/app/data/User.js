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

import Utils from './Utils';

/**
 * Represent an user logged in to the application, There will be allays one user per session and
 * this user details will be persist in browser local-storage.
 */
export default class User {
    /**
     * Create a user for the given environment
     * @param {string} environmentName - name of the environment
     * @param {string} name - name of the cookie
     * @param {boolean} remember
     * @returns {User|null} user object
     */
    constructor(environmentName, name, remember = false) {
        /* eslint-disable no-underscore-dangle */
        // indicate “private” members of APIClientFactory that is why underscore has used here
        const user = User._userMap.get(environmentName);
        if (user) {
            return user;
        }
        this.name = name;
        this._scopes = [];
        this._remember = remember;
        this._environmentName = environmentName;
        User._userMap.set(environmentName, this);
    }

    /**
     * OAuth scopes which are available for use by this user
     * @returns {Array} - An array of scopes
     */
    get scopes() {
        return this._scopes;
    }

    /**
     * Set OAuth scopes available to be used by this user
     * @param {Array} newScopes - An array of scopes
     */
    set scopes(newScopes) {
        Object.assign(this.scopes, newScopes);
    }

    /**
     * User utility method to create an user from JSON object.
     * @param {JSON} userJson - Need to provide user information in JSON structure to create an user object
     * @param {String} environmentName - Name of the environment to be assigned to the user
     * @returns {User} - An instance of User(this) class.
     */
    static fromJson(userJson, environmentName = Utils.getCurrentEnvironment().label) {
        if (!userJson.name) {
            throw new Error('Need to provide user `name` key in the JSON object, to create an user');
        }

        const _user = new User(environmentName, userJson.name);
        _user.scopes = userJson.scopes;
        _user.rememberMe = userJson.remember;
        return _user;
    }

    /**
     * Get the JS accessible access token fragment from cookie storage.
     * @returns {String|null}
     */
    getPartialToken() {
        return Utils.getCookie(User.CONST.WSO2_AM_TOKEN_1, this._environmentName);
    }

    /**
     * Get the JS accessible refresh token fragment from cookie storage.
     * @returns {String|null}
     */
    getRefreshPartialToken() {
        return Utils.getCookie(User.CONST.WSO2_AM_REFRESH_TOKEN_1, this._environmentName);
    }

    /**
     * Store the JavaScript accessible access token segment in cookie storage
     * @param {String} newToken - Part of the access token which needs when accessing REST API
     * @param {Number} validityPeriod - Validity period of the cookie in seconds
     * @param {String} path - Path which need to be set to cookie
     */
    setPartialToken(newToken, validityPeriod, path) {
        Utils.deleteCookie(User.CONST.WSO2_AM_TOKEN_1, path, this._environmentName);
        Utils.setCookie(User.CONST.WSO2_AM_TOKEN_1, newToken, validityPeriod, path, this._environmentName);
    }

    /**
     * Get the expiry time of the user
     * @returns {Date} JS Date object of the expiring time of the user
     */
    getExpiryTime() {
        const expireTime = +localStorage.getItem(User.CONST.USER_EXPIRY_TIME);
        return new Date(expireTime);
    }

    /**
     * Set user expiry time, User validity expires with the expiry of user's access token
     * Negative value will imply removal of existing expiry time
     * @param {Integer} expireTime of seconds till the expire time from the current time
     * @returns {Integer} expire time
     */
    setExpiryTime(expireTime) {
        if (expireTime < 0) {
            localStorage.removeItem(User.CONST.USER_EXPIRY_TIME);
            return expireTime;
        }
        const currentTime = Date.now();
        const timeDiff = 1000 * expireTime;
        localStorage.setItem(User.CONST.USER_EXPIRY_TIME, currentTime + timeDiff);
        this.expiryTime = new Date(currentTime + timeDiff);
        return this.expiryTime;
    }

    /**
     *
     * @param type
     */
    checkPermission() {
        throw new Error('Not implemented!');
    }

    /**
     *  Get tenant domain from username
     * sc1 - normal time : td is always carbon.super
     * sc2 - tenanted : td can be something
     * sc3 - tenanted : td can be carbon.super
     */
    getTenantDomain() {
        const domains = this.name.split('@');
        if (domains.length > 1) {
            return domains[domains.length - 1];
        }
        return null;
    }

    /**
     * Provide user data in JSON structure.
     * @returns {JSON} - JSON representation of the user object
     */
    toJson() {
        return {
            name: this.name,
            scopes: this._scopes,
            remember: this._remember,
            expiryTime: this.getExpiryTime(),
        };
    }
}

User.CONST = {
    WSO2_AM_TOKEN_MSF4J: 'WSO2_AM_TOKEN_MSF4J',
    WSO2_AM_TOKEN_1: 'WSO2_AM_TOKEN_1',
    WSO2_AM_REFRESH_TOKEN_1: 'WSO2_AM_REFRESH_TOKEN_1',
    ADMIN_CLIENT_ID: 'CLIENT_ID',
    LOCAL_STORAGE_USER: 'wso2_user_admin',
    USER_EXPIRY_TIME: 'user_expiry_time',
    ADMIN_SESSION_STATE: 'admin_session_state',
};
/**
 * Map of users (key = environmentLabel, value = User instance)
 * @type {Map}
 * @private
 */
User._userMap = new Map();
/* eslint-enable no-underscore-dangle */
