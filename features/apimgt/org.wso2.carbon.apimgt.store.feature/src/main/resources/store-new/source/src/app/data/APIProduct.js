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
    getAllAPIProducts(params = {}, callback = null) {
        const promiseGetAll = this.client.then(
            (client) => {
                return client.apis['API Products'].get_api_products(params, this._requestMetaData());
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
    getAPIById(id, callback = null) {
        const promiseGet = this.client.then(
            (client) => {
                return client.apis['API Products'].get_api_products__apiProductId__(
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
     * Get the thumnail of an API
     *
     * @param {string} id  UUID of the api
     * @param {function} callback  A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API product invoke promise.
     */
    getAPIThumbnail(id, callback = null) {
        const promisedThumbnail = this.client.then((client) => {
            return client.apis['API Product (Individual)'].get_api_products__apiProductId__thumbnail({
                apiProductId: id,
            },
            this._requestMetaData());
        });

        if (callback) {
            return promisedThumbnail.then(callback);
        } else {
            return promisedThumbnail;
        }
    }

    /**
     * Get the thumnail of an API
     *
     * @param {string} apiProductId  UUID of the api
     * @param {function} callback  A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API product invoke promise.
     */
    getRatingFromUser(apiProductId, callback = null) {
        const promiseGet = this.client.then((client) => {
            return client.apis.Ratings.get_api_products__apiProductId__ratings({ apiProductId },
                this._requestMetaData());
        });
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }

    /**
     * Get all comments for a particular API Product
     *
     * @param {string}  apiProductId api id of the API Product to which the comment is added
     * @param {function} callback  A callback function to invoke after receiving successful response.
     * @returns {promise} With given callback attached to the success chain else API product invoke promise.
     */
    getAllComments(apiProductId, callback = null) {
        const promiseGet = this.client.then((client) => {
            return client.apis.Comments.get_api_products__apiProductId__comments({ apiProductId },
                this._requestMetaData());
        });
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }

    /**
     * Get the swagger of an API
     * @param {String} id  UUID of the API in which the swagger is needed
     * @param {function} callback Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getSwaggerByAPIId(id, callback = null) {
        const promiseGet = this.client.then((client) => {
            return client.apis['API Product (Individual)'].get_api_products__apiProductId__swagger(
                { apiProductId: id }, this._requestMetaData(),
            );
        });
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
                return client.apis['Applications'].get_applications(
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
                return client.apis.Subscription.get_subscriptions(
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
