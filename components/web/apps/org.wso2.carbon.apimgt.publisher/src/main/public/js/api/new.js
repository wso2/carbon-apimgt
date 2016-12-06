/**
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

class API {

    constructor(access_key) {
        console.log("tmkasun debug: Initializing constructor");
        this.client = new SwaggerClient({
            url: 'https://apis.wso2.com/api/am/publisher/v0.10/swagger.json',
            usePromise: true
        });
        this.access_key_header = "Bearer " + access_key;
        this.request_meta = {
            clientAuthorizations: {
                api_key: new SwaggerClient.ApiKeyAuthorization("Authorization", this.access_key_header, "header")
            },
            requestContentType: "application/json"
        };
        this.template = {
            "name": null,
            "context": null,
            "version": null,
            "apiDefinition": "{}",
            "isDefaultVersion": false,
            "transport": [
                "http",
                "https"
            ],
            "tiers": ["Gold"],
            "visibility": "PUBLIC",
            "endpointConfig": ""
        };
    }

    get_template() {
        return this.template;
    }

    update_template(api_data) {
        var current = this.get_template();
        var user_keys = Object.keys(api_data);
        for (var index in user_keys) {
            if (!(user_keys[index] in current)) {
                throw 'Invalid key provided, Valid keys are `' + Object.keys(current) + '`';
            }
        }
        this.template = Object.assign(current, api_data);
    }

    create(callback) {
        var promise_create = this.client.then(
            (client) => {
                return client.APIs.post_apis(
                    {body: this.get_template(), 'Content-Type': "application/json"}, this.request_meta);
            }
        );
        if (callback) {
            return promise_create.then(callback);
        } else {
            return promise_create;
        }
    }

}
$(
    function () {
        $('#api-create-submit').on('click',
            function (event) {
                event.preventDefault();
                var api_data = {
                    namea: $("#new-api-name").val(),
                    context: $('#new-api-context').val(),
                    version: $('#new-api-version').val()
                };
                var new_api = new API('a58ec0d7-7075-3970-95e7-0532f8763d5a');
                new_api.update_template(api_data);
                new_api.create();
            }
        );
    }
);