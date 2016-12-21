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
/**
 * An abstract representation of an API
 */
class API {
    /**
     * @constructor
     * @param {string} access_key - Access key for invoking the backend REST API call.
     */
    constructor(access_key) {
        this.client = new SwaggerClient({
            url: '/publisher/public/components/root/base/js/swagger.json',
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

    /**
     * Get the template of new API placeholder
     * @returns {*}
     */
    get_template() {
        return this.template;
    }

    /**
     * Update the API template with given parameter values.
     * @param {Object} api_data - API data which need to fill the placeholder values in the @get_template
     */
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

    /**
     * Create an API with the given parameters in template and call the callback method given optional.
     * @param {function} callback - An optional callback method
     * @returns {Promise} Promise after creating and optionally calling the callback method.
     */
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
                    name: $("#new-api-name").val(),
                    context: $('#new-api-context').val(),
                    version: $('#new-api-version').val()
                };
                var new_api = new API($.cookie('token'));
                new_api.update_template(api_data);
                new_api.create();
            }
        );
    }
);