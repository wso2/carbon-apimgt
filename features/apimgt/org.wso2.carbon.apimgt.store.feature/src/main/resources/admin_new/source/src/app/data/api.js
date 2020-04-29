/**
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

/* eslint-disable */
import Utils from './Utils';
import Resource from './Resource';
import cloneDeep from 'lodash.clonedeep';
import APIClientFactory from 'AppData/APIClientFactory';

/**
 * An abstract representation of an API
 */
class API extends Resource {
    constructor(name, version, context, kwargs) {
        super();
        let properties = kwargs;
        if (name instanceof Object) {
            properties = name;
            Utils.deepFreeze(properties);
        } else {
            this.name = name;
            this.version = version;
            this.context = context;
            this.isDefaultVersion = false;
            this.gatewayEnvironments = ['Production and Sandbox']; //todo: load the environments from settings API
            this.transport = ['http', 'https'];
            this.visibility = 'PUBLIC';
            this.endpointConfig = {
                endpoint_type: 'http',
                sandbox_endpoints: {
                    url: '',
                },
                production_endpoints: {
                    url: '',
                },
            };
        }
        this.apiType = API.CONSTS.API;
        this._data = properties;
        for (const key in properties) {
            if (Object.prototype.hasOwnProperty.call(properties, key)) {
                this[key] = properties[key];
            }
        }
    }

    /**
     *
     * @param data
     * @returns {object} Metadata for API request
     * @private
     */
    _requestMetaData() {
        Resource._requestMetaData();
    }

    /**
     *
     * Instance method of the API class to provide raw JSON object
     * which is API body friendly to use with REST api requests
     * Use this method instead of accessing the private _data object for
     * converting to a JSON representation of an API object.
     * Note: This is deep coping, Use sparingly, Else will have a bad impact on performance
     * Basically this is the revers operation in constructor.
     * This method simply iterate through all the object properties (excluding the properties in `excludes` list)
     * and copy their values to new object.
     * So use this method with care!!
     * @memberof API
     * @param {Array} [userExcludes=[]] List of properties that are need to be excluded from the generated JSON object
     * @returns {JSON} JSON representation of the API
     */
    toJSON(userExcludes = []) {
        var copy = {},
            excludes = ['_data', 'client', 'apiType', ...userExcludes];
        for (var prop in this) {
            if (!excludes.includes(prop)) {
                copy[prop] = cloneDeep(this[prop]);
            }
        }
        return copy;
    }

    /**
     * Importing a WSDL and creating an API by a .wsdl file or a WSDL archive zip file
     *
     * @static
     * @param {*} url WSDL url
     * @param {*} additionalProperties additional properties of the API eg: name, version, context
     * @param {*} implementationType SOAPTOREST or SOAP
     * @returns {API} API object which was created
     * @memberof Wsdl
     */
    static importByUrl(url, additionalProperties, implementationType = 'SOAP') {
        const apiClient = new APIClientFactory().getAPIClient(
            Utils.getCurrentEnvironment(),
        ).client;
        return apiClient.then((client) => {
            client.apis.APIs;
            const promisedResponse = client.apis.APIs.importWSDLDefinition({
                url,
                additionalProperties: JSON.stringify(additionalProperties),
                implementationType,
            });

            return promisedResponse.then((response) => new API(response.body));
        });
    }

    /**
     * Get list of api categories
     */
    apiCategoriesListGet() {
        return this.client.then((client) => {
            return client.apis['API Category (Collection)'].get_api_categories(
                this._requestMetaData(),
            );
        });
    }

    updateAPICategory(id, name, description, callback = null) {
        const promiseUpdateApiCategory = this.client.then((client) => {
            const data = {
                name: name,
                description: description,
            };
            return client.apis[
                'API Category (Individual)'
            ].put_api_categories__apiCategoryId_(
                { apiCategoryId: id, body: data },
                this._requestMetaData(),
            );
        });

        if (callback) {
            return promiseUpdateApiCategory.then(callback);
        } else {
            return promiseUpdateApiCategory;
        }
    }

    deleteAPICategory(id, callback = null) {
        const promiseDeleteApiCategory = this.client.then((client) => {
            return client.apis[
                'API Category (Individual)'
            ].delete_api_categories__apiCategoryId_(
                { apiCategoryId: id },
                this._requestMetaData(),
            );
        });

        if (callback) {
            return promiseDeleteApiCategory.then(callback);
        } else {
            return promiseDeleteApiCategory;
        }
    }

    createAPICategory(name, description, callback = null) {
        const promiseCreateApiCategory = this.client.then((client) => {
            const data = {
                name: name,
                description: description,
            };
            const payload = {
                body: data,
                'Content-Type': 'application/json',
            };
            return client.apis['API Category (Individual)'].post_api_categories(
                payload,
                this._requestMetaData(),
            );
        });

        if (callback) {
            return promiseCreateApiCategory.then(callback);
        } else {
            return promiseCreateApiCategory;
        }
    }

    /**
     * Get Application Throttling Policies
     */
    applicationThrottlingPoliciesGet() {
        return this.client.then((client) => {
            return client.apis['Application Policy (Collection)'].get_throttling_policies_application(
                this._requestMetaData(),
            );
        });
    }

    /**
     * Delete an Application Throttling Policy
     */
    deleteApplicationThrottlingPolicy(policyId) {
        return this.client.then((client) => {
            return client.apis['Application Policy (Individual)'].delete_throttling_policies_application__policyId_(
                { policyId: policyId },
                this._requestMetaData(),
            );
        });
    }

    /**
     * Add an Application Throttling Policy
     */
    addApplicationThrottlingPolicy(body) {
        return this.client.then((client) => {
            const payload = {
                body,
                'Content-Type': 'application/json',
            };
            return client.apis['Application Policy (Collection)'].post_throttling_policies_application(
                payload,
                this._requestMetaData(),
            );
        });
    }

    /**
     * Get details of an Application Throttling Policy
     */
    applicationThrottlingPolicyGet(policyId) {
        return this.client.then((client) => {
            return client.apis['Application Policy (Individual)'].get_throttling_policies_application__policyId_(
                { policyId: policyId },
                this._requestMetaData(),
            );
        });
    }

     /**
     * Update an Application Throttling Policy
     */
    updateApplicationThrottlingPolicy(policyId, body) {
        return this.client.then((client) => {
            const payload = {
                policyId: policyId,
                body,
                'Content-Type': 'application/json',
            };
            return client.apis['Application Policy (Individual)'].put_throttling_policies_application__policyId_(
                payload,
                this._requestMetaData(),
            );
        });
    }


    getApplicationList() {
        return this.client.then((client) => {
            return client.apis['Application (Collection)'].get_applications(
                this._requestMetaData(),
            );
        });
    }

    updateApplicationOwner(id, owner, callback = null) {
        const promiseUpdateApplicationOwner = this.client.then((client) => {
            return client.apis[
                'Application'
            ].post_applications__applicationId__change_owner(
                { owner: owner, applicationId: id },
                this._requestMetaData(),
            );
        });

        if (callback) {
            return promiseUpdateApplicationOwner.then(callback);
        } else {
            return promiseUpdateApplicationOwner;
        }
    }

    getMicrogatewayLabelList() {
        return this.client.then((client) => {
            return client.apis['Label Collection'].get_labels(
                this._requestMetaData(),
            );
        });
    }

    deleteMicrogatewayLabel(id, callback = null) {
        const promiseDeleteLabel = this.client.then((client) => {
            return client.apis['Label'].delete_labels__labelId_(
                { labelId: id },
                this._requestMetaData(),
            );
        });

        if (callback) {
            return promiseDeleteLabel.then(callback);
        } else {
            return promiseDeleteLabel;
        }
    }
}

API.CONSTS = {
    API: 'API',
    APIProduct: 'APIProduct',
};

Object.freeze(API.CONSTS);

export default API;
