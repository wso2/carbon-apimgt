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

    constructor(name, version, context, kwargs) {
        super();
        let properties = kwargs;
        if (name instanceof Object) {
            properties = name;
        } else {
            this.name = name;
            this.version = '1.0.0';
            this.context = context;
            this.gatewayEnvironments = ['Production and Sandbox'];
            this.transport = [
                'http',
                'https',
            ];
            this.visibility = 'PUBLIC';
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

    /**
     * Create an API Product with the given parameters in template
     * @param {Object} api_product_data - APIProduct data which need to fill the placeholder values in the @get_template
     * @returns {Promise} Promise after creating API Product
     */
    create(api_product_data) {
        let payload;
        let promise_create;
        payload = {
            body: api_product_data,
            'Content-Type': 'application/json'
        };
        promise_create = this.client.then((client) => {
            client.apis['API Product (Individual)'].post_api_products(payload, this._requestMetaData());
        }).catch(
            error => {
                console.error(error);
            }
        );

        promise_create.then(response => {
            return new APIProduct(response.body)
        });
    }


    /**
     * Get details of a given APIProduct
     * @param id {string} UUID of the api-product.
     * @returns {promise} With given callback attached to the success chain else API invoke promise.
     */
    static get(id) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        const promisedAPIProduct = apiClient.then((client) => {
            return client.apis['API Product (Individual)'].get_api_products__apiProductId_({
                apiProductId: id
            }, Resource._requestMetaData());
        }).catch(
            error => {
                console.error(error);
            }
        );
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
        let query = "";
        if (params && 'query' in params) {
            for (const [key, value] of Object.entries(params.query)) {
                query += `${key}:${value},`;
            }
            params.query = query;
        }
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        const promisedProducts = apiClient.then((client) => {
            return client.apis['API Product (Collection)'].get_api_products(params, Resource._requestMetaData());
        }).catch(
            error => {
                console.error(error);
            }
        );
        return promisedProducts.then((response) => {
            response.obj.apiType = API.CONSTS.APIProduct;
            return response;
        });
    }

    /**
     * Get the thumnail of an API Product
     *
     * @param id {string} UUID of the api product
     */
    getAPIProductThumbnail(id) {
        const promisedAPIThumbnail = this.client.then((client) => {
            return client.apis['API Product (Individual)'].get_api_products__apiProductId__thumbnail({
                    apiProductId: id
                },
                this._requestMetaData(),
            );
        }).catch(
            error => {
                console.error(error);
            }
        );

        return promisedAPIThumbnail;
    }

    /**
     * Add new thumbnail image to an API Product
     *
     * @param {String} id id of the API Product
     * @param {File} imageFile thumbnail image to be uploaded
     */
    addAPIProductThumbnail(id, imageFile) {
        const promisedAddAPIThumbnail = this.client.then((client) => {
            const payload = {
                apiProductId: id,
                file: imageFile,
                'Content-Type': imageFile.type,
            };
            return client.apis['API Product (Individual)'].put_api_products__apiProductId__thumbnail(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data'
                }),
            );
        }).catch(
            error => {
                console.error(error);
            }
        );

        return promised_addAPIThumbnail;
    }

    /**
     *
     * Delete an API Product given its UUID
     * @static
     * @param {String} id API Product UUID
     */
    static delete(id) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getCurrentEnvironment()).client;
        return apiClient.then((client) => {
            return client.apis['API Product (Individual)'].delete_api_products__apiProductId_({
                apiProductId: id
            }, this._requestMetaData());
        }).catch(
            error => {
                console.error(error);
            }
        );
    }

    /**
     * Add document to API Product
     * @param {*} id API Product ID to which the document should be attached 
     * @param {*} body 
     */
    addDocument(id, body) {
        const promisedAddDocument = this.client.then((client) => {
            const payload = {
                apiProductId: id,
                body,
                'Content-Type': 'application/json'
            };
            return client.apis['Document (Collection)'].post_api_products__apiProductId__documents(payload, this._requestMetaData());
        }).catch(
            error => {
                console.error(error);
            }
        );
        return promisedAddDocument;
    }

    /**
     * Returns documents attached to a given API Product
     *
     * @param {String} id API Product UUID
     */
    getDocuments(id) {
        const promisedDocuments = this.client.then((client) => {
            return client.apis['Document (Collection)'].get_api_products__apiProductId__documents({
                    apiProductId: id
                },
                this._requestMetaData(),
            );
        }).catch(
            error => {
                console.error(error);
            }
        );
        return promisedDocuments;
    }

    /**
     * Updates a product document
     * @param {*} productId 
     * @param {*} docId 
     * @param {*} body 
     */
    updateDocument(productId, docId, body) {
        const promisedUpdateDocument = this.client.then((client) => {
            const payload = {
                apiProductId: productId,
                body,
                documentId: docId,
                'Content-Type': 'application/json',
            };
            return client.apis['Document (Individual)'].put_api_products__apiProductId__documents__documentId_(
                payload,
                this._requestMetaData(),
            ).catch(
                error => {
                    console.error(error);
                }
            );
        });
        return promisedUpdateDocument;
    }


    /**
     * Get specified document attached to specified product
     * @param {*} productId 
     * @param {*} docId 
     */
    getDocument(productId, docId) {
        const promisedDocument = this.client.then((client) => {
            return client.apis['Document (Individual)'].get_api_products__apiProductId__documents__documentId_({
                apiProductId: productId,
                    documentId: docId,
                },
                this._requestMetaData(),
            );
        }).catch(
            error => {
                console.error(error);
            }
        );
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
        const promise = this.client.then((client) => {
            const payload = {
                apiProductId,
                documentId,
                sourceType,
                inlineContent,
                'Content-Type': 'application/json',
            };
            return client.apis['Document (Individual)'].post_api_products__apiProductId__documents__documentId__content(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data'
                }),
            );
        }).catch(
            error => {
                console.error(error);
            }
        );
        return promise;
    }

    /**
     * Get the inline content of a given document
     * @param {*} apiProductId 
     * @param {*} docId 
     */
    getInlineContentOfDocument(apiProductId, docId) {
        const promisedDocContent = this.client.then((client) => {
            const payload = {
                apiProductId: apiProductId,
                documentId: docId
            };
            return client.apis['Document (Individual)'].get_api_products__apiProductId__documents__documentId__content(payload);
        }).catch(
            error => {
                console.error(error);
            }
        );
        return promisedDocContent;
    }

    /**
     * Delete specified document
     * @param {*} productId 
     * @param {*} docId 
     */
    deleteDocument(productId, docId) {
        const promiseDeleteDocument = this.client.then((client) => {
            return client.apis['Document (Individual)'].delete_api_products__apiProductId__documents__documentId_({
                    apiProductId: productId,
                    documentId: docId,
                },
                this._requestMetaData(),
            );
        }).catch(
            error => {
                console.error(error);
            }
        );
        return promiseDeleteDocument;
    }

    /**
     * Add a File resource to a document
     * @param {*} productId 
     * @param {*} docId 
     * @param {*} fileToDocument 
     */
    addFileToDocument(productId, docId, fileToDocument) {
        const promiseAddFileToDocument = this.client.then((client) => {
            const payload = {
                apiProductId: productId,
                documentId: docId,
                file: fileToDocument,
                'Content-Type': 'application/json',
            };
            return client.apis['Document (Individual)'].post_api_products__apiProductId__documents__documentId__content(
                payload,
                this._requestMetaData({
                    'Content-Type': 'multipart/form-data'
                }),
            );
        });

        return promiseAddFileToDocument;
    }
}

export default APIProduct;
