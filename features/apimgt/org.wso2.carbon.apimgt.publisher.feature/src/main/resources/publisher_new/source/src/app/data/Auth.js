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

class Auth {
    constructor() {
        /* TODO: Move this to configuration ~tmkb*/
        this.host = "https://localhost:9292";
        this.token = "/publisher/auth/apis/login/token";
    }

    static getCookie(name) {
        var value = "; " + document.cookie;
        var parts = value.split("; " + name + "=");
        if (parts.length === 2) return parts.pop().split(";").shift();
    }


    static setCookie(name, value, days) {
        let expires = "";
        if (days) {
            const date = new Date();
            date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
            expires = "; expires=" + date.toUTCString();
        }
        document.cookie = name + "=" + value + expires + "; path=/";
    }

    /**
     * TODO: Implement this method to return the user logged state by considering the cookies stored in the browser,
     * This may give a partial indication of whether the user has logged in or not, The actual API call may get denied
     * if the cookie stored access token is invalid/expired
     *
     * @returns {boolean} Is any user has logged in or not
     */
    static isLogged() {
        return true;
    }

    getTokenEndpoint() {
        return this.host + this.token;
    }

    authenticateUser(username, password) {
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
        let promised_response = axios.post(this.getTokenEndpoint(), qs.stringify(data), {headers: headers});
        promised_response.then(response => {
            let WSO2_AM_TOKEN_1 = response.data.partialToken;
            Auth.setCookie('WSO2_AM_TOKEN_1', WSO2_AM_TOKEN_1);
        });
        return promised_response;
    }
}

export default Auth;
