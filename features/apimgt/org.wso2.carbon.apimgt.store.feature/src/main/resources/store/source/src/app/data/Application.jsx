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
 *
 *
 * @export
 * @class Application Class to expose Application {Resource} related operations i:e: Get all Application , Delete, Generate Keys ect..
 * @extends {Resource}
 */
export default class Application extends Resource {
    constructor(name, description, throttlingTier, kwargs) {
        super();
        this.id = kwargs ? kwargs.applicationId : null;
        this.client = new APIClientFactory().getAPIClient(Utils.getEnvironment().label).client;
        this.keys = new Map();
        this.tokens = new Map();
        for (const key in kwargs) {
            if (Object.prototype.hasOwnProperty.call(kwargs, key)) {
                if (key === 'keys') {
                    this._setKeys(kwargs[key]);
                } else {
                    this[key] = kwargs[key];
                }
            }
        }
    }

    /** *
     * Set this.keys object by iterating the keys array received from REST API
     * @param keys {Array} An array of keys object containing either PRODUCTION or/and SANDBOX key information
     * @private
     */
    _setKeys(keys) {
        for (const keyObj of keys) {
            this.keys.set(keyObj.keyType, keyObj);
        }
    }

    /** *
     * Get keys of the current instance of an application
     * @param keyType {string} Key type either `Production` or `SandBox`
     * @returns {promise} Set the fetched CS/CK into current instance and return keys array as Promise object
     */
    getKeys() {
        const promiseKeys = this.client.then((client) => {
            return client.apis['Application (Individual)'].get_applications__applicationId__keys({ applicationId: this.id });
        });
        return promiseKeys.then((keysResponse) => {
            this._setKeys(keysResponse.obj);
            return this.keys;
        });
    }

    /** *
     * Generate token for this application instance
     * @returns {promise} Set the generated token into current instance and return tokenObject received as Promise object
     */
    generateToken(type) {
        const promiseToken = this.client.then((client) => {
            const keys = this.keys.get(type);
            const requestContent = {
                consumerKey: keys.consumerKey,
                consumerSecret: keys.consumerSecret,
                validityPeriod: 3600,
                scopes: '',
            };
            const payload = { applicationId: this.id, body: requestContent };
            return client.apis['Application (Individual)'].post_applications__applicationId__generate_token(payload);
        });
        return promiseToken.then((tokenResponse) => {
            const token = tokenResponse.obj;
            this.tokens.set(type, token);
            return token;
        });
    }

    /** *
     * Generate Consumer Secret and Consumer Key for this application instance
     * @param keyType {string} Key type either `Production` or `SandBox`
     * @param supportedGrantTypes {string[]}
     * @param callbackUrl {string}
     * @param tokenType {string} Token type either `OAUTH` or `JWT`
     * @returns {promise} Set the generated token into current instance and return tokenObject received as Promise object
     */
    generateKeys(keyType, supportedGrantTypes, callbackUrl, tokenType) {
        const promisedKeys = this.client.then((client) => {
            const requestContent = {
                keyType /* TODO: need to support dynamic key types ~tmkb */,
                grantTypesToBeSupported: supportedGrantTypes,
                callbackUrl,
                tokenType,
            };
            const payload = { applicationId: this.id, body: requestContent };
            return client.apis['Application (Individual)'].post_applications__applicationId__generate_keys(payload);
        });
        return promisedKeys.then((keysResponse) => {
            this.keys.set(keyType, keysResponse.obj);
            return this.keys.get(keyType);
        });
    }

    /** *
     * Generate Consumer Secret and Consumer Key for this application instance
     * @param tokenType {string} Token Type either `OAUTH` or `JWT`
     * @param keyType {string} Key type either `Production` or `SandBox`
     * @param supportedGrantTypes {String []}
     * @param callbackUrl {String}
     * @param consumerKey {String}
     * @param consumerSecret {String}
     * @returns {promise} Update the callbackURL and/or supportedGrantTypes
     */
    updateKeys(tokenType, keyType, supportedGrantTypes, callbackUrl, consumerKey, consumerSecret) {
        const promisedPut = this.client.then((client) => {
            const requestContent = {
                consumerKey,
                consumerSecret,
                supportedGrantTypes,
                callbackUrl,
                keyType,
                tokenType,
            };
            const payload = { applicationId: this.id, keyType, body: requestContent };
            return client.apis['Application (Individual)'].put_applications__applicationId__keys__keyType_(payload);
        });
        return promisedPut.then((keysResponse) => {
            this.keys.set(keyType, keysResponse.obj);
            return this;
        });
    }

    /**
     *
     *
     * @static
     * @param {*} id
     * @returns
     * @memberof Application
     */
    static get(id) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getEnvironment());
        const promisedGet = apiClient.client.then((client) => {
            return client.apis['Application (Individual)'].get_applications__applicationId_({ applicationId: id }, this._requestMetaData());
        });
        return promisedGet.then((response) => {
            const appJson = response.obj;
            return new Application(appJson.name, appJson.description, appJson.throttlingTier, appJson);
        });
    }

    /**
     *
     *
     * @static
     * @returns
     * @memberof Application
     */
    static all() {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getEnvironment());
        const promisedAll = apiClient.client.then((client) => {
            return client.apis['Application (Collection)'].get_applications({}, this._requestMetaData());
        });
        return promisedAll.then(response => response.obj);
    }

    /**
     *
     *
     * @static
     * @param {*} id
     * @returns
     * @memberof Application
     */
    static deleteApp(id) {
        const apiClient = new APIClientFactory().getAPIClient(Utils.getEnvironment());
        const promisedDelete = apiClient.client.then((client) => {
            return client.apis['Application (Individual)'].delete_applications__applicationId_({ applicationId: id }, this._requestMetaData());
        });
        return promisedDelete.then(response => response.ok);
    }
}

Application.KEY_TYPES = {
    PRODUCTION: 'PRODUCTION',
    SANDBOX: 'SANDBOX',
};
