/**
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import APIClientFactory from './APIClientFactory';
import Resource from './Resource';
import Utils from './Utils';

/**
 * Class to expose API Product {Resource} related operations
 */
export default class APIProduct extends Resource {
    constructor() {
        super();
        this.client = new APIClientFactory().getAPIClient(Utils.getEnvironment().label).client;
        this._requestMetaData = Resource._requestMetaData;
    }

    /**
     * Get all API Products
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API Product invoke promise.
     */
    getAllAPIProducts(callback = null) {
        const promiseGetAll = this.client.then(
            (client) => {
                return client.apis['API Products (Collection)'].get_api_products({}, this._requestMetaData());
            },
        );
        if (callback) {
            return promiseGetAll.then(callback);
        } else {
            return promiseGetAll;
        }
    }

    /**
     * Get details of a given API product
     * @param id {string} UUID of the api product.
     * @param callback {function} A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API product invoke promise.
     */
    getAPIProductById(id, callback = null) {
        const promiseGet = this.client.then(
            (client) => {
                return client.apis['API Product (Individual)'].get_api_products__apiProductId__(
                    { apiProductId: id }, this._requestMetaData(),
                );
            },
        );
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }

    /**
     * Get application by id
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     * @deprecated Use Application.all method instead
     */
    getAllApplications(callback = null) {
        const promiseGet = this.client.then(
            (client) => {
                return client.apis['Application (Collection)'].get_applications(
                    {}, this._requestMetaData(),
                );
            },
        );
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }

    /**
     * Get keys of an application
     * @param applicationId id of the application that needs to get the keys
     * @param callback {function} Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getSubscriptions(apiId, applicationId, callback = null) {
        const promiseGet = this.client.then(
            (client) => {
                return client.apis['Subscription (Collection)'].get_subscriptions(
                    { apiId, applicationId }, this._requestMetaData(),
                );
            },
        );
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }
}
