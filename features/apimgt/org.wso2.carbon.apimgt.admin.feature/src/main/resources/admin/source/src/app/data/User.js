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

import Utils from './utils'
/**
 * Represent an user logged in to the application, There will be allays one user per session and
 * this user details will be persist in browser localstorage.
 */
export default class User {
    constructor(name, id_token, remember = false) {
        if (User._instance) {
            return User._instance;
        }
        this.name = name;
        this._scopes = [];
        this._idToken = id_token;
        this._remember = remember;
        User._instance = this;
    }

    /**
     * OAuth scopes which are available for use by this user
     * @returns {Array} : An array of scopes
     */
    get scopes() {
        return this._scopes;
    }

    /**
     * Set OAuth scopes available to be used by this user
     * @param {Array} newScopes :  An array of scopes
     */
    set scopes(newScopes) {
        Object.assign(this.scopes, newScopes);
    }

    /**
     * Get the JS accessible access token fragment from cookie storage.
     * @returns {String|null}
     */
    getPartialToken() {
        return Utils.getCookie(User.CONST.WSO2_AM_TOKEN_1);
    }

    /**
     * Store the JavaScript accessible access token segment in cookie storage
     * @param {String} newToken : Part of the access token which needs when accessing REST API
     * @param {Number} validityPeriod : Validity period of the cookie in seconds
     * @param path Path which need to be set to cookie
     */
    setPartialToken(newToken, validityPeriod, path) {
        Utils.delete_cookie(User.CONST.WSO2_AM_TOKEN_1);
        Utils.setCookie(User.CONST.WSO2_AM_TOKEN_1, newToken, validityPeriod, path);
    }

    /**
     *
     * @param type
     */
    checkPermission(type) {
        throw ("Not implemented!");
    }

    /**
     * Provide user data in JSON structure.
     * @returns {JSON} : JSON representation of the user object
     */
    toJson() {
        return {
            name: this.name,
            scopes: this._scopes,
            idToken: this._idToken,
            remember: this._remember
        };
    }

    /**
     * User utility method to create an user from JSON object.
     * @param {JSON} userJson : Need to provide user information in JSON structure to create an user object
     * @returns {User} : An instance of User(this) class.
     */
    static fromJson(userJson) {
        if (!userJson.name) {
            throw "Need to provide user `name` key in the JSON object, to create an user";
        }
        const _user = new User(userJson.name);
        _user.scopes = userJson.scopes;
        _user.idToken = userJson.idToken;
        _user.rememberMe = userJson.remember;
        return _user;
    }
}

User.CONST = {WSO2_AM_TOKEN_MSF4J: "WSO2_AM_TOKEN_MSF4J", WSO2_AM_TOKEN_1: "WSO2_AM_TOKEN_1", LOCALSTORAGE_USER: "wso2_user"};
User._instance = null; // A private class variable to preserve the single instance of a swaggerClient
