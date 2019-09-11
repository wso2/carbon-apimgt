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

import CONSTS from 'AppData/Constants';
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
     * @param {function} callback A callback function to invoke after receiving successful response.
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
     * @param {string} id UUID of the api product.
     * @param {function} callback A callback function to invoke after receiving successful response.
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
            return client.apis['API Products'].get_api_products__apiProductId__thumbnail({
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
            return client.apis['API Products'].get_api_products__apiProductId__swagger(
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
     * @param {function} callback Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     * @deprecated Use Application.all method instead
     */
    getAllApplications(callback = null) {
        const promiseGet = this.client.then(
            (client) => {
                return client.apis.Applications.get_applications(
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
     * @param {String} apiId UUID of the API Product in which the swagger is needed
     * @param {String} applicationId id of the application that needs to get the keys
     * @param {function} callback  Function which needs to be called upon success
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getSubscriptions(apiId, applicationId, callback = null) {
        const payload = { apiId, apiType: CONSTS.API_PRODUCT_TYPE };
        if (applicationId) {
            payload[applicationId] = applicationId;
        }
        const promisedGet = this.client.then((client) => {
            return client.apis.Subscriptions.get_subscriptions(payload, this._requestMetaData());
        });
        if (callback) {
            return promisedGet.then(callback);
        } else {
            return promisedGet;
        }
    }

    /**
     * Get the swagger of an API Product
     * @param {String} apiProductId UUID of the API Product in which the swagger is needed
     * @param {String} environmentName API environment name
     * @param {function} callback Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getSwaggerByAPIIdAndEnvironment(apiProductId, environmentName, callback = null) {
        const promiseGet = this.client.then((client) => {
            return client.apis['API Products'].get_api_products__apiProductId__swagger(
                { apiProductId, environmentName }, this._requestMetaData(),
            );
        });
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }

    /**
     * Get the swagger of an API Product
     * @param {String} apiProductId UUID of the API in which the swagger is needed
     * @param {String} labelName Micro gateway label
     * @param {function} callback Function which needs to be called upon success of the API deletion
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    getSwaggerByAPIIdAndLabel(apiProductId, labelName, callback = null) {
        const promiseGet = this.client.then((client) => {
            return client.apis['API Products'].get_api_products__apiProductId__swagger(
                { apiProductId, labelName }, this._requestMetaData(),
            );
        });
        if (callback) {
            return promiseGet.then(callback);
        } else {
            return promiseGet;
        }
    }
}
