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

import Os from 'os';
import { app } from 'Settings';
import AuthManager from '../src/app/data/AuthManager.jsx';
import Utils from '../src/app/data/Utils';
import User from '../src/app/data/User';

class TestUtils {
    static setupMockEnvironment() {
        const hostname = Os.hostname(); // for IP address Object.entries(os.networkInterfaces())[0][1][0].address
        global.window = {
            location: {
                hash: '',
                host: hostname + ':9292',
                hostname,
                origin: 'https://' + hostname + ':9292',
                pathname: '/',
                port: '9292',
                protocol: 'https:',
            },
        };
        global.document = {
            value_: '',

            get cookie() {
                return this.value_;
            },

            set cookie(value) {
                this.value_ += value + '; ';
            },

            clearCookies() {
                this.value_ = '';
            },
        };
    }

    /**
     * Logged a user(get an OAuth access token) for a given user and set the *FULL* access tokens in WSO2_AM_TOKEN_1,
     * Since test request is made in node environment HTTP only cookies are also accessible, Hence merging HTTP only and
     * other to build the complete access token
     * @param username
     * @param password
     * @returns {AxiosPromise}
     */
    static userLogin(username = 'admin', password = 'admin') {
        const authenticator = new AuthManager();
        const promisedAuth = authenticator.authenticateUser(username, password);
        promisedAuth.then(
            (response) => {
                let WSO2_AM_TOKEN_MSF4J;
                for (const cookie of response.headers['set-cookie']) {
                    const parts = cookie.split('=');
                    if (parts[0] === User.CONST.WSO2_AM_TOKEN_MSF4J) {
                        WSO2_AM_TOKEN_MSF4J = parts[1].split(';')[0];
                        break;
                    }
                }
                const { partialToken, validityPeriod } = response.data;
                document.clearCookies();
                Utils.setCookie(
                    User.CONST.WSO2_AM_TOKEN_1,
                    partialToken + WSO2_AM_TOKEN_MSF4J,
                    validityPeriod, app.context,
                );
            },
        );
        return promisedAuth;
    }
}

export default TestUtils;
