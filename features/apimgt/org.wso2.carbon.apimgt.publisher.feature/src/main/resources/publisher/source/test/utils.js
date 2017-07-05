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

import AuthManager from "../src/app/data/AuthManager.js";
class TestUtils {
    static setupMockEnviroment() {
        global.window = {
            location: {
                hash: "",
                host: "localhost:9292",
                hostname: "localhost",
                origin: "https://localhost:9292",
                pathname: "/",
                port: "9292",
                protocol: "https:"
            }
        };
        global.document = {
            value_: '',

            get cookie() {
                return this.value_;
            },

            set cookie(value) {
                this.value_ += value + '; ';
            }
        };
    }

    static userLogin(username = 'admin', password = 'admin') {
        let authenticator = new AuthManager();
        return authenticator.authenticateUser(username, password);
    }
}

export default TestUtils