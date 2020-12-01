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
/* eslint-disable */
import API from 'AppData/api';
import APIClientFactory from './APIClientFactory';
import Utils from './Utils';
import Resource from './Resource';

/**
 * An abstract representation of an API Product
 */
class APIProduct extends Resource {
    constructor(name, context, kwargs) {
        super();
        let properties = kwargs;
        if (name instanceof Object) {
            properties = name;
        } else {
            this.name = name;
            this.version = '1.0.0';
            this.context = context;
            this.isDefaultVersion = false;
            this.gatewayEnvironments = ['Production and Sandbox'];
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
        this._data = properties;
        for (const key in properties) {
            if (Object.prototype.hasOwnProperty.call(properties, key)) {
                this[key] = properties[key];
            }
        }
        this.apiType = API.CONSTS.APIProduct;
        this.getType = this.getType.bind(this);
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

    getType() {
        return this.type;
    }

    getSettings() {
        const promisedSettings = this.client.then(client => {
            return client.apis['Settings'].get_settings();
        });
        return promisedSettings.then(response => response.body);
    }

    getMonetization(id, callback = null) {
        const promiseMonetization = this.client.then(client => {
            return client.apis['API Monetization'].get_apis__apiId__monetization(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
        return promiseMonetization.then(response => response.body);
    }

    configureMonetizationToApiProduct(id, body) {
        const promised_status = this.client.then(client => {
            return client.apis['API Monetization'].post_apis__apiId__monetize({
                apiId: id,
                body,
            });
        });
        return promised_status;
    }

    getSubscriptionPolicies(id, callback = null) {
        const promisePolicies = this.client.then(client => {
            return client.apis['APIs'].get_apis__apiId__subscription_policies(
                {
                    apiId: id,
                },
                this._requestMetaData(),
            );
        });
        return promisePolicies.then(response => response.body);
    }

    getMonetizationInvoice(id, callback = null) {
        const promiseInvoice = this.client.then(client => {
            return client.apis['API Monetization'].get_subscriptions__subscriptionId__usage(
                {
                    subscriptionId: id
                },
                this._requestMetaData(),
            );
        });
        return promiseInvoice.then(response => response.body);
    }

    /**
     * Create an API Product with the given parameters in template
     * @param {Object} api_product_data - APIProduct data which need to fill the placeholder values in the @get_template
     * @returns {Promise} Promise after creating API Product
     */
    saveProduct() {
        const promisedAPIResponse = this.client.then(client => {
            const properties = client.spec.components.schemas.APIProduct.properties;
            const data = {};

            Object.keys(this).forEach(apiAttribute => {
                if (apiAttribute in properties) {
                    if (apiAttribute != 'apiType') {
                        data[apiAttribute] = this[apiAttribute];
                    }
                }
            });
            const payload = {
                'Content-Type': 'application/json',
            };
            const requestBody = {
                requestBody: data
            }
            return client.apis['API Products'].post_api_products(payload, requestBody, this._requestMetaData());
        });
        return promisedAPIResponse.then(response => {
            return new API(response.body);
        });
    }
    /**
     * Create an API Product with the given parameters in template and call the callback method given optional.
     * @param {Object} apiData - API data which need to fill the placeholder values in the @get_template
     * @returns {Promise} Promise after creating and optionally calling the callback method.
     */
    create(apiData) {
        let payload;
        let promise_create;

        payload = {
            body: apiData,
            'Content-Type': 'application/json',
        };
        promise_create = this.client.then(client => {
            return client.apis['API Products'].post_api_products(payload, this._requestMetaData());
        });

        return promise_create;
    }
    /**
     *
     * Instance method of the API class to provide raw JSON object
     * which is API body friendly to use with REST api requests
     * Use this method instead of accessing the private _data object for
     * converting to a JSON representation of an API object.
     * Note: This is shallow coping
     * Basically this is the revers operation in constructor.
     * This method simply iterate through all the object properties
     * and copy their values to new object excluding the properties in excludes list.
     * So use this method sparingly!!
     * @memberof API
     * @param {Array} [userExcludes=[]] List of properties that are need to be excluded from the generated JSON object
     * @returns {JSON} JSON representation of the API
     */
    toJSON(resource = this, userExcludes = []) {
        var copy = {},
            excludes = ['_data', 'client', 'type', ...userExcludes];
        for (var prop in resource) {
            if (!excludes.includes(prop)) {
                copy[prop] = resource[prop];
            }
        }
        return copy;
    }

    /**
     * Get details of a given APIProduct
     * @param id {string} UUID of the api-product.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    static get(id) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment(), Utils.CONST.API_CLIENT).client;
        const promisedAPIProduct = apiClient
            .then(client => {
                return client.apis['API Products'].get_api_products__apiProductId_(
                    {
                        apiProductId: id,
                    },
                    this._requestMetaData(),
                );
            })
            .catch(error => {
                console.error(error);
            });
        return promisedAPIProduct.then(response => {
            return new APIProduct(response.body);
        });
    }

    /**
     *
     * Static method for get all API Products for current environment user.
     * @param {Object} params API Products filtering parameters i:e { "name": "MyBank Product"}
     * @returns {Promise} promise object return from SwaggerClient-js
     */
    static all(params) {
        let query = '';
        if (params && 'query' in params) {
            for (const [key, value] of Object.entries(params.query)) {
                query += `${key}:${value},`;
            }
            params.query = query;
        }
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment(), Utils.CONST.API_CLIENT).client;
        const promisedProducts = apiClient
            .then(client => {
                return client.apis['API Products'].get_api_products(params, Resource._requestMetaData());
            })
            .catch(error => {
                console.error(error);
            });
        return promisedProducts.then(response => {
            response.obj.apiType = API.CONSTS.APIProduct;
            return response;
        });
    }

    /**
     * Update an api Product via PUT HTTP method, Need to give the updated API Product object as the argument.
     * @param apiProduct {Object} Updated API Product object(JSON) which needs to be updated
     */
    update(updatedProperties) {
        const updatedAPI = { ...this.toJSON(), ...this.toJSON(updatedProperties) };
        const promisedUpdate = this.client.then(client => {
            const payload = {
                apiProductId: updatedAPI.id,
            };
            const requestBody = {
                requestBody: updatedAPI,
            }
            return client.apis['API Products'].put_api_products__apiProductId_(payload, requestBody);
        });
        return promisedUpdate.then(response => {
            return new APIProduct(response.body);
        });
    }

    /**
     * Get the thumnail of an API Product
     *
     * @param id {string} UUID of the api product
     */
    getAPIProductThumbnail(id) {
        const promisedAPIThumbnail = this.client
            .then(client => {
                return client.apis['API Products'].get_api_products__apiProductId__thumbnail(
                    {
                        apiProductId: id,
                    },
                    this._requestMetaData(),
                );
            })
            .catch(error => {
                console.error(error);
            });

        return promisedAPIThumbnail;
    }

    /**
     * Add new thumbnail image to an API Product
     *
     * @param {String} id id of the API Product
     * @param {File} imageFile thumbnail image to be uploaded
     */
    addAPIProductThumbnail(id, imageFile) {
        const promisedAddAPIThumbnail = this.client
            .then(client => {
                const payload = {
                    apiProductId: id,
                    'Content-Type': imageFile.type,
                };
                return client.apis['API Products'].put_api_products__apiProductId__thumbnail(
                    payload,
                    {
                        requestBody: {
                            file: imageFile,
                        }
                    },
                    this._requestMetaData({
                        'Content-Type': 'multipart/form-data',
                    }),
                );
            })
            .catch(error => {
                console.error(error);
            });

        return promisedAddAPIThumbnail;
    }

    /**
     *
     * Delete an API Product given its UUID
     * @static
     * @param {String} id API Product UUID
     */
    static delete(id) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment(), Utils.CONST.API_CLIENT).client;
        return apiClient
            .then(client => {
                return client.apis['API Products'].delete_api_products__apiProductId_(
                    {
                        apiProductId: id,
                    },
                    this._requestMetaData(),
                );
            })
            .catch(error => {
                console.error(error);
            });
    }

    /**
     * Add document to API Product
     * @param {*} id API Product ID to which the document should be attached
     * @param {*} body
     */
    addDocument(id, body) {
        const promisedAddDocument = this.client
            .then(client => {
                const payload = {
                    apiProductId: id,
                    'Content-Type': 'application/json',
                };
                return client.apis['API Product Documents'].post_api_products__apiProductId__documents(
                    payload,
                    {
                        requestBody: body,
                    },
                    this._requestMetaData(),
                );
            })
            .catch(error => {
                console.error(error);
            });
        return promisedAddDocument;
    }

    /**
     * Returns documents attached to a given API Product
     *
     * @param {String} id API Product UUID
     */
    getDocuments(id) {
        const promisedDocuments = this.client
            .then(client => {
                return client.apis['API Product Documents'].get_api_products__apiProductId__documents(
                    {
                        apiProductId: id,
                    },
                    this._requestMetaData(),
                );
            })
            .catch(error => {
                console.error(error);
            });
        return promisedDocuments;
    }

    /**
     * Updates a product document
     * @param {*} productId
     * @param {*} docId
     * @param {*} body
     */
    updateDocument(productId, docId, body) {
        const promisedUpdateDocument = this.client.then(client => {
            const payload = {
                apiProductId: productId,
                documentId: docId,
                'Content-Type': 'application/json',
            };
            return client.apis['API Product Documents']
                .put_api_products__apiProductId__documents__documentId_(
                    payload,
                    {
                        requestBody: body,
                    },
                    this._requestMetaData()
                )
                .catch(error => {
                    console.error(error);
                });
        });
        return promisedUpdateDocument;
    }

    /**
     * Get specified document attached to specified product
     * @param {*} productId
     * @param {*} docId
     */
    getDocument(productId, docId) {
        const promisedDocument = this.client
            .then(client => {
                return client.apis['API Product Documents'].get_api_products__apiProductId__documents__documentId_(
                    {
                        apiProductId: productId,
                        documentId: docId,
                    },
                    this._requestMetaData(),
                );
            })
            .catch(error => {
                console.error(error);
            });
        return promisedDocument;
    }

    /**
     * Add inline content to a INLINE type document
     * @param {*} apiProductId API Product ID
     * @param {*} documentId Document ID
     * @param {*} sourceType
     * @param {*} inlineContent Content to be added to document
     */
    addInlineContentToDocument(apiProductId, documentId, sourceType, inlineContent) {
        const promise = this.client
            .then(client => {
                const payload = {
                    apiProductId,
                    documentId,
                    sourceType,
                    'Content-Type': 'application/json',
                };
                return client.apis[
                    'API Product Documents'
                ].post_api_products__apiProductId__documents__documentId__content(
                    payload,
                    {
                        requestBody: {
                            inlineContent: inlineContent
                        }
                    },
                    this._requestMetaData({
                        'Content-Type': 'multipart/form-data',
                    }),
                );
            })
            .catch(error => {
                console.error(error);
            });
        return promise;
    }

    /**
     * Get the inline content of a given document
     * @param {*} apiProductId
     * @param {*} docId
     */
    getInlineContentOfDocument(apiProductId, docId) {
        const promisedDocContent = this.client
            .then(client => {
                const payload = {
                    apiProductId,
                    documentId: docId,
                };
                return client.apis[
                    'API Product Documents'
                ].get_api_products__apiProductId__documents__documentId__content(payload);
            })
            .catch(error => {
                console.error(error);
            });
        return promisedDocContent;
    }

    /**
     * Delete specified document
     * @param {*} productId
     * @param {*} docId
     */
    deleteDocument(productId, docId) {
        const promiseDeleteDocument = this.client
            .then(client => {
                return client.apis['API Product Documents'].delete_api_products__apiProductId__documents__documentId_(
                    {
                        apiProductId: productId,
                        documentId: docId,
                    },
                    this._requestMetaData(),
                );
            })
            .catch(error => {
                console.error(error);
            });
        return promiseDeleteDocument;
    }

    /**
     * Add a File resource to a document
     * @param {*} productId
     * @param {*} docId
     * @param {*} fileToDocument
     */
    addFileToDocument(productId, docId, fileToDocument) {
        const promiseAddFileToDocument = this.client.then(client => {
            const payload = {
                apiProductId: productId,
                documentId: docId,
                'Content-Type': 'application/json',
            };
            return client.apis['API Product Documents'].post_api_products__apiProductId__documents__documentId__content(
                payload,
                {
                    requestBody: {
                        file: fileToDocument
                    }
                },
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data',
                }),
            );
        });

        return promiseAddFileToDocument;
    }

    /**
     * Update an api swagger via PUT HTTP method
     * @param {*} swagger
     */
    updateSwagger(swagger) {
        const promisedUpdate = this.client.then(client => {
            const payload = {
                apiProductId: this.id,
                apiDefinition: JSON.stringify(swagger),
                'Content-Type': 'multipart/form-data',
            };
            return client.apis['API Products']
                .put_api_products__apiProductId__swagger(
                    payload,
                    this._requestMetaData({
                        'Content-Type': 'multipart/form-data',
                    }),
                )
                .catch(error => {
                    console.error(error);
                });
        });
        return promisedUpdate.then(response => {
            return this;
        });
    }

    /**
     * Get the swagger of an API Product
     * @param id {String} UUID of the API Product in which the swagger is needed
     */
    getSwagger(id = this.id) {
        const promiseGet = this.client
            .then(client => {
                return client.apis['API Products'].get_api_products__apiProductId__swagger(
                    {
                        apiProductId: id,
                    },
                    this._requestMetaData(),
                );
            })
            .catch(error => {
                console.error(error);
            });
        return promiseGet;
    }
}

export default APIProduct;
