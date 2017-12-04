/**
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
"use strict";
import SingleClient from "./SingleClient"

export default class Application {
    constructor(name, description, throttlingTier, kwargs) {
        this.id = kwargs ? kwargs.applicationId : null;
        this.client = new SingleClient().client;
        for (let key in kwargs) {
            if (kwargs.hasOwnProperty(key)) {
                this[key] = kwargs[key];
            }
        }
    }

    getKeys(type) {
        let promise_keys = this.client.then((client) => {
            return client.apis["Application (Individual)"].get_applications__applicationId__keys({applicationId: this.id});
        });
        return promise_keys.then(keys_response => {
            this.keys = keys_response.obj;
            return this.keys;
        });
    }

    static get(id) {
        let client = new SingleClient().client;
        let promise_get = client.then(
            (client) => {
                return client.apis["Application (Individual)"].get_applications__applicationId_({applicationId: id});
            });
        return promise_get.then(response => {
            let app_json = response.obj;
            return new Application(app_json.name, app_json.description, app_json.throttlingTier, app_json);
        });
    }
}